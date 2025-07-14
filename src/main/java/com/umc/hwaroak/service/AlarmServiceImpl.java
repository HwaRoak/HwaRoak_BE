package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.umc.hwaroak.response.ErrorCode.NOTICE_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class AlarmServiceImpl implements AlarmService {

    private final AlarmRepository alarmRepository;

    /**
     * 공지 최신순으로 가져오기
     */
    @Override
    public List<Alarm> getNotices() {
        return alarmRepository.findByAlarmTypeOrderByCreatedAtDesc(AlarmType.NOTIFICATION);
    }

    /**
     * 공지 상세 조회
     */
    @Override
    public Alarm getNoticeById(Long id) {
        return alarmRepository.findByIdAndAlarmType(id, AlarmType.NOTIFICATION)
                .orElseThrow(() -> new GeneralException(NOTICE_NOT_FOUND));
    }
}
