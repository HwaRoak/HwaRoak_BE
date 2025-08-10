package com.umc.hwaroak.scheduler;

import com.umc.hwaroak.converter.AlarmConverter;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.infrastructure.publisher.RedisPublisher;
import com.umc.hwaroak.infrastructure.transaction.CustomTransactionSynchronization;
import com.umc.hwaroak.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.Month;

@RequiredArgsConstructor
@Component
public class AlarmScheduler {

    private final AlarmRepository alarmRepository;
    private final RedisPublisher redisPublisher;

    @Scheduled(cron = "0 0 0 1 * *") // 매달 1일 00:00
    public void createMonthlyDailyAlarm() {
        // 전달 월 구하기 (ex. 7월 1일이면 6월)
        LocalDate today = LocalDate.now();
        Month lastMonth = today.minusMonths(1).getMonth();

        // 랜덤 content 생성
        String content1 = "리포트 반영 완료! 내 감정을 돌아볼까요?";
        String content2 = String.format("리포트 도착! %d월의 화록은 어땠을까요?", lastMonth.getValue());
        String content = Math.random() < 0.5 ? content1 : content2;

        Alarm alarm = Alarm.builder()
                .alarmType(AlarmType.DAILY)
                .title("감정 리포트가 반영됐어요!")
                .content(content)
                .message("한 달 동안의 내 감정을 돌아볼 시간이에요.")
                .receiver(null)
                .sender(null)
                .isRead(false)
                .build();

        alarmRepository.save(alarm);
        TransactionSynchronizationManager.registerSynchronization(
                new CustomTransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisPublisher.publish(alarm.getAlarmType().getValue(), AlarmConverter.toPreviewDto(alarm));
                    }
                }
        );
    }
}
