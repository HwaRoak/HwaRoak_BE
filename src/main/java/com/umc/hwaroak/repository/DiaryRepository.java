package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long>, DiaryRepositoryCustom {

    @Query("SELECT d FROM Diary d WHERE d.recordDate = :recordDate and d.member.id = :memberId")
    Optional<Diary> findByRecordDate(@Param("memberId") Long memberId, @Param("recordDate") LocalDate recordDate);

    /**
     *  오늘 일기쓴 사람이 존재하는가 안하는가.
     */
    Boolean existsByMemberIdAndRecordDate(@Param("memberId") Long memberId, LocalDate recordDate);



}
