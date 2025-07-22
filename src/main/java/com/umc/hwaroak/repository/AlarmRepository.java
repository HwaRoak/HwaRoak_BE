package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.AlarmType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    // 공지 리스트 (최신순)  -> 타입별 최신순으로 가져오는 것이기에 매개변수만 다르게 하면 다른 알람내용 하실 때 활용 가능하십니다!
    List<Alarm> findByAlarmTypeOrderByCreatedAtDesc(AlarmType alarmType);

    // 공지 상세 -> 이것도 현재 이름만 공지 상세지 타입과 이름으로 조회하는 것이기에 활용가능 합니다!
    Optional<Alarm> findByIdAndAlarmType(Long id, AlarmType alarmType);

    // 모든 알람 조회 -> 알람함 누르면 최신순으로 쫘라락 뜨게끔.
    List<Alarm> findAllByReceiverOrderByCreatedAtDesc(Member receiver);

    // 불씨 알람 중 특정 sender -> receiver 조합의 가장 최신 알림
    @Query("SELECT a FROM Alarm a " +
            "WHERE a.alarmType = :alarmType AND a.sender = :sender AND a.receiver = :receiver " +
            "ORDER BY a.fired_at DESC")
    List<Alarm> findTopBySenderAndReceiverAndAlarmTypeOrderBy(
            Member sender,
            Member receiver,
            AlarmType alarmType
    );
}
