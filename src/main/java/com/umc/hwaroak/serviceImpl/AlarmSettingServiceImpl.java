package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.AlarmSetting;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.request.AlarmSettingRequestDto;
import com.umc.hwaroak.dto.response.AlarmSettingResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmSettingRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.AlarmSettingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmSettingServiceImpl implements AlarmSettingService {

    private final MemberLoader memberLoader;
    private final AlarmSettingRepository alarmSettingRepository;

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
                    return alarmSettingRepository.save(defaultSetting);
                });

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

        Long memberId = memberLoader.getCurrentMemberId();

        AlarmSetting setting = alarmSettingRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("알람 설정을 찾을 수 없습니다. memberId = {}", memberId);
                    return new GeneralException(ErrorCode.SETTING_NOT_FOUND);
                });

        if (requestDto.getReminderEnabled() != null)
            setting.setReminderEnabled(requestDto.getReminderEnabled());

        if (requestDto.getReminderTime() != null)
            setting.setReminderTime(requestDto.getReminderTime());

        if (requestDto.getFireAlarmEnabled() != null)
            setting.setFireEnabled(requestDto.getFireAlarmEnabled());

        if (requestDto.getAllOffEnabled() != null){

            setting.setAllOffEnabled(requestDto.getAllOffEnabled());

            // 모든 알림을 끌 경우, 알림 off를 리마인더와 불씨 알림에도 적용
            if (requestDto.getAllOffEnabled() == false){
                setting.setReminderEnabled(false);
                setting.setFireEnabled(false);
            }
        }

        return AlarmSettingResponseDto.InfoDto.builder()
                .reminderEnabled(setting.isReminderEnabled())
                .reminderTime(setting.getReminderTime())
                .fireAlarmEnabled(setting.isFireEnabled())
                .allOffEnabled(setting.isAllOffEnabled())
                .build();
    }
}
