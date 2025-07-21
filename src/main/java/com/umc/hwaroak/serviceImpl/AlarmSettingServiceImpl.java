package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.AlarmSetting;
import com.umc.hwaroak.dto.response.AlarmSettingResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmSettingRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.AlarmSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmSettingServiceImpl implements AlarmSettingService {

    private final MemberLoader memberLoader;
    private final AlarmSettingRepository alarmSettingRepository;

    public AlarmSettingResponseDto.InfoDto getAlarmSettingInfo() {

        Long memberId = memberLoader.getCurrentMemberId();

        AlarmSetting setting = alarmSettingRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("알람 설정을 찾을 수 없습니다. memberId = {}", memberId);
                    return new GeneralException(ErrorCode.SETTING_NOT_FOUND);
                });

        return AlarmSettingResponseDto.InfoDto.builder()
                .reminderEnabled(setting.isReminderEnabled())
                .reminderTime(setting.getReminderTime())
                .fireAlarmEnabled(setting.isFireEnabled())
                .allOffEnabled(setting.isAllOffEnabled())
                .build();
    }
}
