package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.request.AlarmSettingRequestDto;
import com.umc.hwaroak.dto.response.AlarmSettingResponseDto;

public interface AlarmSettingService {

    AlarmSettingResponseDto.InfoDto getAlarmSettingInfo();

    AlarmSettingResponseDto.InfoDto editAlarmSettingInfo(AlarmSettingRequestDto.EditDto requestDto);
}
