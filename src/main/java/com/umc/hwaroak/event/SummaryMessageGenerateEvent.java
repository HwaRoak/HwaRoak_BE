package com.umc.hwaroak.event;

import com.umc.hwaroak.domain.common.EmotionCategory;

import java.util.List;
import java.util.Map;

public record SummaryMessageGenerateEvent(
        Long summaryId,
        int targetMonth,
        Map<EmotionCategory, Integer> counts,
        List<String> contents
) {}