package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.AlarmType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    // 공지 리스트 (최신순)  -> 타입별 최신순으로 가져오는 것이기에 매개변수만 다르게 하면 다른 알람내용 하실 때 활용 가능하십니다!
    List<Alarm> findByAlarmTypeOrderByCreatedAtDesc(AlarmType alarmType);

    // 공지 상세 -> 이것도 현재 이름만 공지 상세지 타입과 이름으로 조회하는 것이기에 활용가능 합니다!
    Optional<Alarm> findByIdAndAlarmType(Long id, AlarmType alarmType);

    /**
     * 알람함 최신순 전체 조회
     * receiverId로 조회 or (receiverId=NULL && 공지)
     */
    @Query("""
        SELECT a FROM Alarm a
        WHERE a.receiver = :receiver
           OR (a.receiver IS NULL AND a.alarmType = 'NOTIFICATION')
        ORDER BY a.createdAt DESC
    """)
    List<Alarm> findAllIncludingNotifications(@Param("receiver") Member receiver);

    /**
     * 사용자가 받은 알람중 알람 타입에 따라 읽기 전인게 있나 없나? Boolean 반환.
     */
    Boolean existsByReceiverAndAlarmTypeAndReadFalse(Member receiver, AlarmType alarmType);
}
