package com.umc.hwaroak.event.listener;

import com.umc.hwaroak.event.SummaryMessageGenerateEvent;
import com.umc.hwaroak.repository.EmotionSummaryRepository;
import com.umc.hwaroak.util.OpenAiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryMessageListener {

    private final OpenAiUtil openAiUtil;
    private final EmotionSummaryRepository emotionSummaryRepository;

    // 독립 스레드에서 id로 분석 메시지만 수정
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateMessage(SummaryMessageGenerateEvent event) {

        log.info("Listener thread: {}", Thread.currentThread().getName());
        //Thread.sleep(2000);

        try {
            String msg = openAiUtil.analysisEmotions(
                    event.targetMonth(), event.counts(), event.contents()
            );
            emotionSummaryRepository.updateMessageById(event.summaryId(), msg);
            log.info("gpt 리스너: 감정분석 메시지 업데이트 완료 - summaryId={}", event.summaryId());
        } catch (Exception e) {
            log.warn("gpt 리스너 실패 - summaryId={}", event.summaryId(), e);
        }

    }
}