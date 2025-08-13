package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.infrastructure.authentication.MemberLoader;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.EmotionSummary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.domain.common.EmotionCategory;
import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.EmotionSummaryRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.EmotionSummaryService;
import com.umc.hwaroak.util.OpenAiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    public Map<EmotionCategory, MemberResponseDto.EmotionCount> getPreviewEmotionSummary(String yearMonth) {

        // 멤버ID 받아와서 감정분석 데이터 조회
        Long memberId = memberLoader.getCurrentMemberId();
        log.info("감정 요약 Preview 조회 - memberId: {}, month: {}", memberId, yearMonth);

        EmotionSummary summary = emotionSummaryRepository.findByMemberIdAndSummaryMonth(memberId, yearMonth).orElse(null);

        if (summary == null) {
            log.info("감정 요약이 존재하지 않습니다. memberId = {}, yearMonth = {}", memberId, yearMonth);
            return new EnumMap<>(EmotionCategory.class); // 비어 있는 감정 통계 응답
        }

        return createPreviewFromSummary(summary);
    }

    // 감정 카테고리별 통계 정보 PreviewDto에 담아 반환
    private Map<EmotionCategory, MemberResponseDto.EmotionCount> createPreviewFromSummary(EmotionSummary summary) {

        // 감정 전체 개수 카운트
        int total = summary.getCalmCount() + summary.getHappyCount()
                + summary.getSadCount() + summary.getAngryCount();

        if (total == 0) {
            log.warn("감정이 0개 입니다. memberId = {}, yearMonth = {}", summary.getMember().getId(), summary.getSummaryMonth());
        }

        // 개수 기반으로 비율 계산하고 두 필드 모두 리턴
        Map<EmotionCategory, MemberResponseDto.EmotionCount> emotionMap = new EnumMap<>(EmotionCategory.class);
        emotionMap.put(EmotionCategory.CALM, new MemberResponseDto.EmotionCount(summary.getCalmCount(), calculatePercent(summary.getCalmCount(), total)));
        emotionMap.put(EmotionCategory.HAPPY, new MemberResponseDto.EmotionCount(summary.getHappyCount(), calculatePercent(summary.getHappyCount(), total)));
        emotionMap.put(EmotionCategory.SAD, new MemberResponseDto.EmotionCount(summary.getSadCount(), calculatePercent(summary.getSadCount(), total)));
        emotionMap.put(EmotionCategory.ANGRY, new MemberResponseDto.EmotionCount(summary.getAngryCount(), calculatePercent(summary.getAngryCount(), total)));

        return emotionMap;
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

    // 독립적인 트랜잭션에서 감정 분석 삭제 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteSummary(Long memberId, String month) {
        emotionSummaryRepository.findByMemberIdAndSummaryMonth(memberId, month)
                .ifPresent(summary -> {
                    emotionSummaryRepository.delete(summary);
                    emotionSummaryRepository.flush();
                });
    }

    // 감정 분석 삭제에 대한 폴백-전체 0으로 초기화
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setZeroSummary(Long memberId, String month) {
        emotionSummaryRepository.setZero(month, memberId);
    }

    @Override
    @Transactional
    public void updateMonthlyEmotionSummary(LocalDate targetDate) {

        Member member = memberLoader.getMemberByContextHolder();
        Long memberId = member.getId();
        String summaryMonth = YearMonth.from(targetDate).toString();
        log.info("감정 요약 업데이트 시작 - memberId: {}, month: {}", memberId, summaryMonth);

        // 멤버ID와 업데이트 하려는 달의 정보로 해당 월의 모든 일기를 가져옴
        List<Diary> diaries = diaryRepository.findAllDiariesByYearMonth(memberId, targetDate.getYear(), targetDate.getMonthValue());    // 데이터가 없더라도 빈 리스트 반환
        int diaryCount = diaries.size();    // 전체 일기 개수
        log.info("{}월 일기 개수: {}", targetDate.getMonthValue(), diaryCount);

        // 일기가 없으면 EmotionSummary 삭제 처리
        if (diaryCount == 0) {
            try {
                deleteSummary(memberId, summaryMonth);
                log.info("감정 요약 삭제 완료 - memberId: {}, month: {}", memberId, summaryMonth);
            } catch (DataIntegrityViolationException e) {
                log.warn("감정 요약 삭제 실패 - memberId: {}, month: {}", memberId, summaryMonth, e);
                setZeroSummary(memberId, summaryMonth);
                log.info("감정 요약 0 초기화 완료 - memberId: {}, month: {}", memberId, summaryMonth);
            }
            return;
        }

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

        EmotionSummary summary;
        try {
            // 해당 일기가 속한 달의 분석 데이터를 업데이트 하기 위해 조회
            summary = emotionSummaryRepository.findByMemberIdAndSummaryMonth(memberId, summaryMonth)
                    .orElseGet(() -> {
                        // 없을 경우(그 달의 첫 일기가 작성되지 않은 경우) 새로 생성
                        EmotionSummary newSummary = EmotionSummary.builder()
                                .member(member)
                                .summaryMonth(summaryMonth)
                                .build();
                        return emotionSummaryRepository.save(newSummary);
                    });
        } catch (DataIntegrityViolationException e) {
            // 생성 요청 동시 발생 → EmotionSummary 최초 생성 후의 트랜잭션은 롤백, 다시 조회
            log.warn("중복 생성 감지 - memberId: {}, month: {}. 기존 데이터 재조회 시도.", memberId, summaryMonth);
            summary = emotionSummaryRepository.findByMemberIdAndSummaryMonth(memberId, summaryMonth)
                    .orElseThrow(() -> new GeneralException(ErrorCode.DUPLICATE_EMOTION_SUMMARY));
        }

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
