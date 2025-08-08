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

    /**
     * 알람함 최신순 전체 조회
     * receiverId로 조회 or (receiverId=NULL && 공지)
     */
    @Query("""
    SELECT a FROM Alarm a
    LEFT JOIN FETCH a.sender
    WHERE a.receiver = :receiver
       OR (a.receiver IS NULL AND a.alarmType IN ('NOTIFICATION', 'DAILY'))
    ORDER BY a.createdAt DESC
""")
    List<Alarm> findAllIncludingGlobalAlarms(@Param("receiver") Member receiver);


    // sender + receiver + alarmType 기준 최신 알람 (여러 개 가능)
    List<Alarm> findBySenderAndReceiverAndAlarmTypeOrderByCreatedAtDesc(
            Member sender,
            Member receiver,
            AlarmType alarmType
    );

    // 읽지 않은 FIRE 알람이 존재하는지 여부 확인
    boolean existsByReceiverAndAlarmTypeAndIsReadFalse(Member receiver, AlarmType alarmType);
}
