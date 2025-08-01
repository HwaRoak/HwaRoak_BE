package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.EmotionSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmotionSummaryRepository extends JpaRepository<EmotionSummary,Long> {

    // 멤버ID, 연도,월로 분석 데이터 조회
    Optional<EmotionSummary> findByMemberIdAndSummaryMonth(Long memberId, String summaryMonth);
}
