package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom {

    //@Lock(LockModeType.PESSIMISTIC_WRITE) // 잠금
    @Query("SELECT d FROM Diary d WHERE d.recordDate = :recordDate and d.member.id = :memberId")
    Optional<Diary> findByRecordDate(@Param("memberId") Long memberId, @Param("recordDate") LocalDate recordDate);

    @Query("SELECT count(d.id) FROM Diary d where d.member.id = :memberId")
    long countByMemberId(@Param("memberId") Long memberId);

    //"최근 3일 내 작성된 다이어리 중 가장 최신 것 하나"만 가져오기
    Optional<Diary> findTop1ByMemberAndRecordDateGreaterThanEqualOrderByRecordDateDesc(Member member, LocalDate date);

}
