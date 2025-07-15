package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.common.AlarmType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    // 공지 리스트 (최신순)  -> 타입별 최신순으로 가져오는 것이기에 매개변수만 다르게 하면 다른 알람내용 하실 때 활용 가능하십니다!
    List<Alarm> findByAlarmTypeOrderByCreatedAtDesc(AlarmType alarmType);

    // 공지 상세 -> 이것도 현재 이름만 공지 상세지 타입과 이름으로 조회하는 것이기에 활용가능 합니다!
    Optional<Alarm> findByIdAndAlarmType(Long id, AlarmType alarmType);
}
