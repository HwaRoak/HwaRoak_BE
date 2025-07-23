package com.umc.hwaroak.service;

import com.umc.hwaroak.dto.response.EmotionSummaryResponseDto;

public interface EmotionSummaryService {
    EmotionSummaryResponseDto.PreviewDto getPreviewEmotionSummary(String yearMonth);

    EmotionSummaryResponseDto.DetailDto getDetailEmotionSummary(String yearMonth);
}
