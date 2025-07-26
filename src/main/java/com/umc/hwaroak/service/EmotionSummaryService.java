package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.EmotionSummaryResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EmotionSummaryService {

    // 감정 카테고리별 개수와 비율을 반환
    EmotionSummaryResponseDto.PreviewDto getPreviewEmotionSummary(String yearMonth);

    // 일기 개수, 감정 통계, 분석 멘트 반환
    EmotionSummaryResponseDto.DetailDto getDetailEmotionSummary(String yearMonth);

    // 감정 통계를 업데이트하고, 이를 기반으로 분석 멘트도 함께 업데이트
    void updateMonthlyEmotionSummary(LocalDateTime targetDate);
}
