package com.umc.hwaroak.scheduler;

import com.umc.hwaroak.repository.EmotionSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmotionScheduler {

    private final EmotionSummaryRepository emotionSummaryRepository;
    /**
     * 매월 1일 0시 정각에 3개월 전까지의 감정분석 데이터를 삭제합니다.
     * ex. 2025-08-01 실행 → 2025-05까지의 데이터 삭제
     */
    @Scheduled(cron = "0 0 0 1 * *")  // 매월 1일 정각
    public void deleteOldEmotionSummaries() {
        String date = LocalDate.now().minusMonths(3).format(DateTimeFormatter.ofPattern("yyyy-MM"));    // ex. 2025-08

        emotionSummaryRepository.deleteEmotionSummaryBefore(date);
        log.info("{} 이전의 감정분석 데이터가 삭제되었습니다.", date);
    }
}
