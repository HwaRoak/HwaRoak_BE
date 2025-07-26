package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.EmotionSummary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.domain.common.EmotionCategory;
import com.umc.hwaroak.dto.response.EmotionSummaryResponseDto;
import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.EmotionSummaryRepository;
import com.umc.hwaroak.service.EmotionSummaryService;
import com.umc.hwaroak.util.OpenAiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmotionSummaryServiceImpl implements EmotionSummaryService {

    private final MemberLoader memberLoader;

    private final EmotionSummaryRepository emotionSummaryRepository;
    private final DiaryRepository diaryRepository;

    private final OpenAiUtil openAiUtil;

    @Override
    @Transactional(readOnly=true)
    public MemberResponseDto.PreviewDto getPreviewEmotionSummary(String yearMonth) {

        // 멤버ID 받아와서 감정분석 데이터 조회
        Long memberId = memberLoader.getCurrentMemberId();
        log.info("감정 요약 Preview 조회 - memberId: {}, month: {}", memberId, yearMonth);

        EmotionSummary summary = emotionSummaryRepository.findByMemberIdAndSummaryMonth(memberId, yearMonth).orElse(null);

        if (summary == null) {
            log.info("감정 요약이 존재하지 않습니다. memberId = {}, yearMonth = {}", memberId, yearMonth);
            return new MemberResponseDto.PreviewDto(); // 비어 있는 감정 통계 응답
        }

        return createPreviewFromSummary(summary);
    }

    // 감정 카테고리별 통계 정보 PreviewDto에 담아 반환
    private MemberResponseDto.PreviewDto createPreviewFromSummary(EmotionSummary summary) {

        // 감정 전체 개수 카운트
        int total = summary.getCalmCount() + summary.getHappyCount()
                + summary.getSadCount() + summary.getAngryCount();

        if (total == 0) {
            log.warn("감정이 0개 입니다. memberId = {}, yearMonth = {}", summary.getMember().getId(), summary.getSummaryMonth());
        }

        // 개수 기반으로 비율 계산하고 두 필드 모두 리턴
        Map<EmotionCategory, MemberResponseDto.EmotionCount> map = new EnumMap<>(EmotionCategory.class);
        map.put(EmotionCategory.CALM, new MemberResponseDto.EmotionCount(summary.getCalmCount(), calculatePercent(summary.getCalmCount(), total)));
        map.put(EmotionCategory.HAPPY, new MemberResponseDto.EmotionCount(summary.getHappyCount(), calculatePercent(summary.getHappyCount(), total)));
        map.put(EmotionCategory.SAD, new MemberResponseDto.EmotionCount(summary.getSadCount(), calculatePercent(summary.getSadCount(), total)));
        map.put(EmotionCategory.ANGRY, new MemberResponseDto.EmotionCount(summary.getAngryCount(), calculatePercent(summary.getAngryCount(), total)));

        return new MemberResponseDto.PreviewDto(map);
    }

    // 비율 계산 메소드 - 소수점 첫째 자리까지 반올림
    private static double calculatePercent(int number, int total) {
        return total == 0 ? 0.0 : Math.round((number * 1000.0 / total)) / 10.0;
    }

    @Override
    public MemberResponseDto.DetailDto getDetailEmotionSummary(String yearMonth) {

        // 멤버ID 받아와서 감정분석 데이터 조회
        Long memberId = memberLoader.getCurrentMemberId();
        log.info("감정 상세 조회 - memberId: {}, month: {}", memberId, yearMonth);

        EmotionSummary summary = emotionSummaryRepository.findByMemberIdAndSummaryMonth(memberId, yearMonth).orElse(null);

        if (summary == null) {
            log.info("감정 요약이 존재하지 않습니다. memberId = {}, yearMonth = {}", memberId, yearMonth);
            return new MemberResponseDto.DetailDto(); // 비어 있는 감정 통계 응답
        }

        return MemberResponseDto.DetailDto.builder()
                .diaryCount(summary.getDiaryCount())
                .emotionSummary(createPreviewFromSummary(summary))
                .message(summary.getSummaryMessage())
                .build();
    }

    @Override
    public void updateMonthlyEmotionSummary(LocalDate targetDate) {

        Member member = memberLoader.getMemberByContextHolder();
        Long memberId = member.getId();
        String summaryMonth = YearMonth.from(targetDate).toString();
        log.info("감정 요약 업데이트 시작 - memberId: {}, month: {}", memberId, summaryMonth);

        // 멤버ID와 업데이트 하려는 달의 정보로 해당 월의 모든 일기를 가져옴
        List<Diary> diaries = diaryRepository.findAllDiariesByYearMonth(memberId, targetDate.getYear(), targetDate.getMonthValue());
        int diaryCount = diaries.size();    // 전체 일기 개수
        log.info("{}월 일기 개수: {}", targetDate.getMonthValue(), diaryCount);

        Map<EmotionCategory, Integer> categoryCounts = new EnumMap<>(EmotionCategory.class);

        for (Diary diary : diaries) {
            for (Emotion emotion : diary.getEmotionList()) {
                categoryCounts.merge(emotion.getCategory(), 1, Integer::sum);
            }
        }
        log.debug("감정 카운트 통계: {}", categoryCounts);

        // ai 기반 감정분석 멘트 생성
        int targetMonth = targetDate.getMonthValue();
        String gptMessage = openAiUtil.analysisEmotions(targetMonth, categoryCounts, diaries);
        log.info("gpt 감정분석 메시지: {}", gptMessage);

        EmotionSummary summary = emotionSummaryRepository
                .findByMemberIdAndSummaryMonth(memberId, summaryMonth)
                .orElse(EmotionSummary.builder()
                        .member(member)
                        .summaryMonth(summaryMonth)
                        .build());

        summary.updateCounts(
                diaryCount,
                categoryCounts.getOrDefault(EmotionCategory.CALM, 0),
                categoryCounts.getOrDefault(EmotionCategory.HAPPY, 0),
                categoryCounts.getOrDefault(EmotionCategory.SAD, 0),
                categoryCounts.getOrDefault(EmotionCategory.ANGRY, 0),
                gptMessage
        );

        emotionSummaryRepository.save(summary);

        log.info("감정 요약 저장 완료 - memberId: {}, month: {}", memberId, summaryMonth);

    }


}
