package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.EmotionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface EmotionSummaryRepository extends JpaRepository<EmotionSummary,Long> {

    // 멤버ID, 연도,월로 분석 데이터 조회
    Optional<EmotionSummary> findByMemberIdAndSummaryMonth(Long memberId, String summaryMonth);

    /**
     * @param date 감정분석 데이터 삭제할 기준 일자
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM EmotionSummary es WHERE es.summaryMonth <= :date")
    void deleteEmotionSummaryBefore(@Param("date") String date);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
    update EmotionSummary s
       set s.diaryCount = 0,
           s.calmCount  = 0,
           s.happyCount = 0,
           s.sadCount   = 0,
           s.angryCount = 0,
           s.summaryMessage = ''
     where s.summaryMonth = :month
       and s.member.id   = :memberId
""")
    void setZero(@Param("month") String month, @Param("memberId") Long memberId);

}
