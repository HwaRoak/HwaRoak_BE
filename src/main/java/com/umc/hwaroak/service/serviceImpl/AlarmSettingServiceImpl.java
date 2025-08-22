package com.umc.hwaroak.service.serviceImpl;

import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.scheduler.ReminderTaskScheduler;
import com.umc.hwaroak.infrastructure.authentication.MemberLoader;
import com.umc.hwaroak.domain.AlarmSetting;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.request.AlarmSettingRequestDto;
import com.umc.hwaroak.dto.response.AlarmSettingResponseDto;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.repository.AlarmSettingRepository;
import com.umc.hwaroak.service.AlarmSettingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmSettingServiceImpl implements AlarmSettingService {

    private final MemberLoader memberLoader;
    private final AlarmSettingRepository alarmSettingRepository;
    private final AlarmRepository alarmRepository;

    private final ReminderTaskScheduler reminderTaskScheduler;

    @Transactional
    public AlarmSettingResponseDto.InfoDto getAlarmSettingInfo() {

        Member member = memberLoader.getMemberByContextHolder();
        Long memberId = memberLoader.getCurrentMemberId();

        AlarmSetting setting = alarmSettingRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    log.info("기본 알람 설정 생성 - memberId: {}", memberId);
                    AlarmSetting defaultSetting = AlarmSetting.builder()
                            .member(member)
                            .reminderEnabled(true)
                            .reminderTime(LocalTime.of(21, 30))
                            .fireEnabled(true)
                            .allOffEnabled(false)
                            .build();

                    Alarm alarm = Alarm.builder()
                            .alarmType(AlarmType.REMINDER)
                            .receiver(member)
                            .title("오늘의 이야기로 화록을 불태우세요!")
                            .content("오늘 하루는 어땠나요? 저에게 들려주세요!")
                            .message("오늘 하루는 어땠나요? 저에게 들려주세요!")
                            .build();
                    alarmRepository.save(alarm);
                    reminderTaskScheduler.cancel(memberId);
                    reminderTaskScheduler.addSchedule(alarm);

                    return alarmSettingRepository.save(defaultSetting);
                });

        if (setting.isReminderEnabled() && !setting.isAllOffEnabled()) {
            if (!alarmRepository.findByMemberIdAndAlarmType(memberId).isPresent()) {
                log.info("알람이 존재하지 않아 새로 생성 중... {}", memberId);
                Alarm defaultAlarm = Alarm.builder()
                        .alarmType(AlarmType.REMINDER)
                        .receiver(member)
                        .title("오늘의 이야기로 화록을 불태우세요!")
                        .content("오늘 하루는 어땠나요? 저에게 들려주세요!")
                        .message("오늘 하루는 어땠나요? 저에게 들려주세요!")
                        .build();
                alarmRepository.save(defaultAlarm);
                // 취소 후 추가; 중복 방지
                reminderTaskScheduler.cancel(memberId);
                reminderTaskScheduler.addSchedule(defaultAlarm);
            }
        }

        return AlarmSettingResponseDto.InfoDto.builder()
                .reminderEnabled(setting.isReminderEnabled())
                .reminderTime(setting.getReminderTime())
                .fireAlarmEnabled(setting.isFireEnabled())
                .allOffEnabled(setting.isAllOffEnabled())
                .build();
    }

    @Override
    @Transactional
    public AlarmSettingResponseDto.InfoDto editAlarmSettingInfo(AlarmSettingRequestDto.EditDto requestDto) {

        Member member = memberLoader.getMemberByContextHolder();
        Long memberId = memberLoader.getCurrentMemberId();

        AlarmSetting setting = alarmSettingRepository.findByMemberId(memberId)
                .orElseGet(() -> {
                    log.warn("알람 설정을 찾을 수 없습니다... 기본 설정으로 설정합니다. memberId = {}", memberId);
                    AlarmSetting defaultSetting = AlarmSetting.builder()
                            .member(member)
                            .reminderEnabled(true)
                            .reminderTime(LocalTime.of(21, 30))
                            .fireEnabled(true)
                            .allOffEnabled(false)
                            .build();

                    return alarmSettingRepository.save(defaultSetting);
                });

        if (requestDto.getReminderEnabled() != null)
            setting.setReminderEnabled(requestDto.getReminderEnabled());

        if (requestDto.getReminderTime() != null)
            setting.setReminderTime(requestDto.getReminderTime());

        if (requestDto.getFireAlarmEnabled() != null)
            setting.setFireEnabled(requestDto.getFireAlarmEnabled());

        if (requestDto.getAllOffEnabled() != null){
            // 나머지 두 알림 관련 설정 데이터를 DB 상에서 유지하고, '모든 알람 끄기' 관련 변환은 프론트에서 처리
            setting.setAllOffEnabled(requestDto.getAllOffEnabled());
        }

        alarmSettingRepository.save(setting); // 변경사항 저장

        if (!setting.isReminderEnabled() || setting.isAllOffEnabled()) { // 알람 설정 off(리마인더 off, 전체 off 모두 반영)
            reminderTaskScheduler.cancel(memberId);
        } else { // 알람 설정 on
            Alarm alarm = alarmRepository.findByMemberIdAndAlarmType(memberId)
                    .orElseGet(() -> {
                        Alarm defaultAlarm = Alarm.builder()
                                .alarmType(AlarmType.REMINDER)
                                .receiver(member)
                                .title("오늘의 이야기로 화록을 불태우세요!")
                                .content("오늘 하루는 어땠나요? 저에게 들려주세요!")
                                .message("오늘 하루는 어땠나요? 저에게 들려주세요!")
                                .build();
                        return alarmRepository.save(defaultAlarm);});
            log.debug("알람 정보 확인... {}", memberId);

            reminderTaskScheduler.cancel(memberId);
            reminderTaskScheduler.addSchedule(alarm);
        }

        return AlarmSettingResponseDto.InfoDto.builder()
                .reminderEnabled(setting.isReminderEnabled())
                .reminderTime(setting.getReminderTime())
                .fireAlarmEnabled(setting.isFireEnabled())
                .allOffEnabled(setting.isAllOffEnabled())
                .build();
    }
}
