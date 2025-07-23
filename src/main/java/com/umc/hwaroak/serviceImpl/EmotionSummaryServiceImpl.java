package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.EmotionSummary;
import com.umc.hwaroak.domain.common.EmotionCategory;
import com.umc.hwaroak.dto.response.EmotionSummaryResponseDto;
import com.umc.hwaroak.repository.EmotionSummaryRepository;
import com.umc.hwaroak.service.EmotionSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmotionSummaryServiceImpl implements EmotionSummaryService {
    private final MemberLoader memberLoader;
    private final EmotionSummaryRepository emotionSummaryRepository;

    @Override
    @Transactional(readOnly=true)
    public EmotionSummaryResponseDto.PreviewDto getPreviewEmotionSummary(String yearMonth) {

        // 멤버ID 받아와서 감정분석 데이터 조회
        Long memberId = memberLoader.getCurrentMemberId();

        EmotionSummary summary = emotionSummaryRepository.findByMemberIdAndSummaryMonth(memberId, yearMonth).orElse(null);

        if (summary == null) {
            log.info("감정 요약이 존재하지 않습니다. memberId = {}, yearMonth = {}", memberId, yearMonth);
            return new EmotionSummaryResponseDto.PreviewDto(); // 비어 있는 감정 통계 응답
        }

        return createPreviewFromSummary(summary);
    }

    // 감정 카테고리별 통계 정보 PreviewDto에 담아 반환
    private EmotionSummaryResponseDto.PreviewDto createPreviewFromSummary(EmotionSummary summary) {

        // 감정 전체 개수 카운트
        int total = summary.getCalmCount() + summary.getHappyCount()
                + summary.getSadCount() + summary.getAngryCount();

        if (total == 0) {
            log.warn("감정이 0개 입니다. memberId = {}, yearMonth = {}", summary.getMember().getId(), summary.getSummaryMonth());
        }

        // 개수 기반으로 비율 계산하고 두 필드 모두 리턴
        Map<EmotionCategory, EmotionSummaryResponseDto.EmotionCount> map = new EnumMap<>(EmotionCategory.class);
        map.put(EmotionCategory.CALM, new EmotionSummaryResponseDto.EmotionCount(summary.getCalmCount(), calculatePercent(summary.getCalmCount(), total)));
        map.put(EmotionCategory.HAPPY, new EmotionSummaryResponseDto.EmotionCount(summary.getHappyCount(), calculatePercent(summary.getHappyCount(), total)));
        map.put(EmotionCategory.SAD, new EmotionSummaryResponseDto.EmotionCount(summary.getSadCount(), calculatePercent(summary.getSadCount(), total)));
        map.put(EmotionCategory.ANGRY, new EmotionSummaryResponseDto.EmotionCount(summary.getAngryCount(), calculatePercent(summary.getAngryCount(), total)));

        return new EmotionSummaryResponseDto.PreviewDto(map);
    }

    // 비율 계산 메소드 - 소수점 첫째 자리까지 반올림
    private static double calculatePercent(int number, int total) {
        return total == 0 ? 0.0 : Math.round((number * 1000.0 / total)) / 10.0;
    }

    @Override
    public EmotionSummaryResponseDto.DetailDto getDetailEmotionSummary(String yearMonth) {

        // 멤버ID 받아와서 감정분석 데이터 조회
        Long memberId = memberLoader.getCurrentMemberId();

        EmotionSummary summary = emotionSummaryRepository.findByMemberIdAndSummaryMonth(memberId, yearMonth).orElse(null);

        if (summary == null) {
            log.info("감정 요약이 존재하지 않습니다. memberId = {}, yearMonth = {}", memberId, yearMonth);
            return new EmotionSummaryResponseDto.DetailDto(); // 비어 있는 감정 통계 응답
        }

        return EmotionSummaryResponseDto.DetailDto.builder()
                .diaryCount(summary.getDiaryCount())
                .emotionSummary(createPreviewFromSummary(summary))
                .message(summary.getSummaryMessage())
                .build();
    }


}
