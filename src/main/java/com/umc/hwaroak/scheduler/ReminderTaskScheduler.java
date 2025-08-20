package com.umc.hwaroak.scheduler;

import com.umc.hwaroak.converter.AlarmConverter;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.AlarmSetting;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.infrastructure.publisher.RedisPublisher;
import com.umc.hwaroak.repository.AlarmRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@Slf4j
public class ReminderTaskScheduler {

    private final AlarmRepository alarmRepository;
    private final RedisPublisher redisPublisher;

    private ThreadPoolTaskScheduler taskScheduler;

    private final Clock clock = Clock.systemDefaultZone();

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public ReminderTaskScheduler(
            @Qualifier("taskScheduler") ThreadPoolTaskScheduler taskScheduler,
            AlarmRepository alarmRepository,
            RedisPublisher redisPublisher) {
        this.taskScheduler = taskScheduler;
        this.alarmRepository = alarmRepository;
        this.redisPublisher = redisPublisher;
    }

    /**
     * Spring Boot 시작과 함께 활성화
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initScheduledAlarm() {
        log.info("스케줄링 작업 시작");

        List<Alarm> alarmList = alarmRepository.findReminderByEnabledTrue();
        alarmList.forEach(this::addSchedule);

        log.info("초기 알림 스케줄링 완료");
    }

    @Transactional(readOnly = true)
    public void addSchedule(Alarm alarm) {

        AlarmSetting alarmSetting = (alarm.getReceiver() != null ) ? alarm.getReceiver().getAlarmSetting() : null;
        Member member = alarm.getReceiver();

        // 유효하지 않은 Alarm
        if (member == null) {return;}
        if (alarmSetting == null || !alarmSetting.isFireEnabled()) {
            cancel(member.getId());
            return;
        }

        // 유효한 알람에 대해서만 스케줄 작업 시작
        try {
            // scheduleAlarm 호출
            scheduleAlarm(alarm, member, alarmSetting);
        } catch (Exception e) {
            log.error("알람 스케줄링 중 오류 발생. memberId: {}, error: {}", member.getId(), e.getMessage(), e);
        }
    }

    private void scheduleAlarm(Alarm alarm, Member member, AlarmSetting alarmSetting) {
        // 다음 알람 시간 계산 (오늘이면 오늘, 이미 지났으면 내일)
        LocalDateTime nextAlarmTime = getNextAlarmTime(alarmSetting.getReminderTime());
        Instant targetInstant = nextAlarmTime.atZone(clock.getZone()).toInstant();
        Instant now = Instant.now();

        long delay = Duration.between(now, targetInstant).toMillis();

        if (delay <= 0) {
            log.info("알람 시간이 이미 지났습니다. memberId: {}, nextAlarmTime: {}", member.getId(), nextAlarmTime);
            return;
        }

        log.info("알람 스케줄링. memberId: {}, nextAlarmTime: {}, delay: {}ms",
                member.getId(), nextAlarmTime, delay);

        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                () -> executeAlarm(alarm),
                Date.from(targetInstant)
        );

        scheduledTasks.put(member.getId(), scheduledFuture);
        log.debug("알람 스케줄링 완료. memberId: {}, nextAlarmTime: {}", member.getId(), nextAlarmTime);
    }

    /**
     * 알람 실행
     */
    private void executeAlarm(Alarm alarm) {
        try {
            log.info("알람 실행. alarmId: {}, memberId: {}", alarm.getId(), alarm.getReceiver().getId());

            redisPublisher.publish(AlarmType.REMINDER.getValue(), AlarmConverter.toPreviewDto(alarm));

            // 다음날을 위한 재스케줄링(무한 재귀 방지)
            rescheduleForNextDay(alarm);

        } catch (Exception e) {
            log.error("알람 실행 중 오류 발생하였습니다. alarmId: {}, error: {}", alarm.getId(), e.getMessage(), e);
        }
    }

    /**
     * 다음날을 위한 재스케줄링 (매일 재스케줄링 과정 필요)
     */
    private void rescheduleForNextDay(Alarm alarm) {
        try {
            log.info("알람 설정 변경으로 인한 재스케줄링 실시. alarmId: {}", alarm.getId());
            addSchedule(alarm);

        } catch (Exception e) {
            log.error("재스케줄링 중 오류 발생. alarmId: {}, error: {}", alarm.getId(), e.getMessage(), e);
        }
    }

    /**
     * 알람 비활성화
     * @param memberId : 회원의 아이디
     */
    public void cancel(Long memberId) {
        ScheduledFuture<?> existingTask = scheduledTasks.get(memberId);
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel(false);
            scheduledTasks.remove(memberId);
        }
    }

    /**
     * 다음 예정 알림 시각을 계산 ( 다음 날로 초기화 )
     * @param reminderTime
     * @return
     */
    private LocalDateTime getNextAlarmTime(LocalTime reminderTime) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayAlarmTime = today.atTime(reminderTime);

        // 오늘의 알람 시간이 아직 지나지 않았으면 오늘 스케줄링, 지났으면 내일
        if (todayAlarmTime.isAfter(LocalDateTime.now())) {
            return todayAlarmTime;
        } else {
            return todayAlarmTime.plusDays(1);
        }
    }
}