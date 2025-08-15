package com.umc.hwaroak.service.serviceImpl;

import com.umc.hwaroak.infrastructure.authentication.MemberLoader;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.domain.Question;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.dto.response.QuestionResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.QuestionRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.ItemService;
import com.umc.hwaroak.service.QuestionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final MemberLoader memberLoader;
    private final DiaryRepository diaryRepository;
    private final AlarmRepository alarmRepository;
    private final QuestionRepository questionRepository;
    private final ItemService itemService;

    private final Random random = new Random();

    private static final Map<Integer, String> LEVEL_NAME_KR = Map.ofEntries(
            Map.entry(1,  "두루마리 휴지"),
            Map.entry(2,  "빳빳한 종이컵"),
            Map.entry(3,  "장작용 목재"),
            Map.entry(4,  "다 타버린 연탄"),
            Map.entry(5,  "구워먹는 타이어"),
            Map.entry(6,  "애착 쓰레기 봉투"),
            Map.entry(7,  "딱 반으로 쪼갠 젓가락"),
            Map.entry(8,  "고기맛이 나는 감자"),
            Map.entry(9,  "구워먹는 치즈"),
            Map.entry(10, "노릇노릇 후라이"),
            Map.entry(11, "쫀쫀한 쿠키"),
            Map.entry(12, "산적 고기"),
            Map.entry(13, "기름이 쫙 빠진 치킨"),
            Map.entry(14, "따끈한 스프"),
            Map.entry(15, "다음 보상을 기대해 주세요!") // 안내 문구지만 스펙상 15레벨명으로 매핑
    );


    /**
     * 홈 화면에서 보여줄 Question 메시지 조회
     * 우선순위에 따라 적절한 tag의 Question을 랜덤으로 반환합니다.
     */
    @Override
    @Transactional
    public QuestionResponseDto getMainMessage() {
        Member member = memberLoader.getMemberByContextHolder();
        log.info("메인 메시지 조회 시작 - memberId: {}", member.getId());

        if (isRewardReceivable()) {
            log.info("[1순위] 보상 수령 가능 상태 감지 → REWARD 메시지 노출");
            return getRandomQuestionByTag("REWARD");
        }

        if (hasUnreadFireAlarm(member)) {
            log.info("[2순위] 읽지 않은 불씨 알람 감지 → FIRE 메시지 노출");
            return getRandomQuestionByTag("FIRE");
        }

        if (!hasWrittenToday(member)) {
            log.info("[3순위] 오늘 일기 미작성 상태 감지 → DIARY_EMPTY 메시지 노출");
            return getRandomQuestionByTag("DIARY_EMPTY");
        }

        log.info("[4순위] 오늘 일기 작성됨 → 감정 기반 메시지 노출 시도");
        return getEmotionBasedQuestion(member);
    }

    private QuestionResponseDto getRandomQuestionByTag(String tag) {
        log.info("태그 기반 메시지 조회 시도 - tag: {}", tag);
        if (!questionRepository.existsByTag(tag)) {
            throw new GeneralException(ErrorCode.INVALID_TAG);
        }

        Pageable limitOne = PageRequest.of(0, 1);
        List<Question> questions = questionRepository.findRandomOneByTag(tag, limitOne);

        if (questions.isEmpty()) {
            log.warn("해당 태그에 해당하는 메시지가 존재하지 않음 - fallback 반환");
            return QuestionResponseDto.of("오늘 하루를 돌아보는 건 어때요?", tag);
        }

        Question q = questions.get(0);
        log.info("메시지 선택 완료 - content: {}", q.getContent());

        if ("REWARD".equals(tag)) {
            log.info("REWARD 태그 감지 → 보상 아이템 정보 조회");
            Member member = memberLoader.getMemberByContextHolder();

            // isReceived = false 중 itemId 가장 작은 것
            MemberItem a = member.getMemberItemList().stream()
                    .filter(mi -> Boolean.FALSE.equals(mi.getIsReceived()))
                    .min(Comparator.comparing(mi -> mi.getItem().getId()))
                    .orElse(null);

            if (a != null) {
                int level = a.getItem().getLevel();
                String koreanName = getKoreanNameByLevel(level);
                String itemInfo = "Lv " + level + ". " + koreanName;
                String name = a.getItem().getName();

                return QuestionResponseDto.ofReward(q.getContent(), tag, itemInfo, name);
            }
        }

        return QuestionResponseDto.of(q.getContent(), tag);
    }



    private QuestionResponseDto getEmotionBasedQuestion(Member member) {
        Optional<Diary> diaryOpt = diaryRepository.findByMemberIdAndRecordDate(member.getId(), LocalDate.now());

        if (diaryOpt.isEmpty()) {
            log.warn("오늘 작성된 일기가 존재하지 않음");
            return QuestionResponseDto.of("오늘 하루를 돌아보는 건 어때요?", "NONE");
        }

        List<Emotion> emotionList = diaryOpt.get().getEmotionList();
        if (emotionList == null || emotionList.isEmpty()) {
            log.warn("감정 리스트가 비어 있음 → 기본 멘트 반환");
            return QuestionResponseDto.of("당신의 하루가 궁금해요.", "NONE");
        }

        // 긍정 / 부정 감정 세트 정의
        Set<Emotion> POSITIVE_EMOTIONS = Set.of(
                Emotion.CALM, Emotion.PROUD, Emotion.THANKFUL,
                Emotion.HAPPY, Emotion.EXPECTED, Emotion.HEART_FLUTTER, Emotion.EXCITING
        );
        Set<Emotion> NEGATIVE_EMOTIONS = Set.of(
                Emotion.BORED, Emotion.LONELY, Emotion.GLOOMY,
                Emotion.SADNESS, Emotion.ANGRY, Emotion.ANNOYED, Emotion.STRESSFUL, Emotion.TIRED
        );

        boolean hasPositive = emotionList.stream().anyMatch(POSITIVE_EMOTIONS::contains);
        boolean hasNegative = emotionList.stream().anyMatch(NEGATIVE_EMOTIONS::contains);

        String tag;
        if (hasPositive && hasNegative) {
            // 혼합 감정 → EMOTION_DEFAULT
            tag = "EMOTION_DEFAULT";
            log.info("혼합 감정 감지 → tag: {}", tag);
            return getRandomQuestionByTag(tag);
        }

        // 전부 긍정 또는 전부 부정 → 감정 하나 랜덤 선택 후 해당 태그에서 뽑기
        Emotion selectedEmotion = emotionList.get(random.nextInt(emotionList.size()));
        tag = "EMOTION_" + selectedEmotion.name();
        log.info("단일 극성 감정 → selected: {} → tag: {}", selectedEmotion, tag);
        return getRandomQuestionByTag(tag);
    }


    // =======================
    // 내부 상태 판별 메서드
    // =======================

    private boolean isRewardReceivable() {
        List<MemberItem> memberItems = itemService.findNotReceivedItem();

        // MemberItem 중 isReceived가 false 인 것이 있는가? -> 있으면 보상 받을 수 있는 상황.
        boolean hasUnreceivedItem = memberItems.stream()
                .anyMatch(item -> !item.getIsReceived());

        memberItems.forEach(item ->
                log.info("MemberItem: itemId={}, isReceived={}", item.getItem().getId(), item.getIsReceived()));


        log.info("보상 수령 가능 여부: hasUnreceivedItem == true → {}", hasUnreceivedItem);
        return hasUnreceivedItem;
    }


    private boolean hasUnreadFireAlarm(Member member) {
        boolean result = alarmRepository.existsByReceiverAndAlarmTypeAndIsReadFalse(member, AlarmType.FIRE);
        log.info("읽지 않은 FIRE 알람 존재 여부: {}", result);
        return result;
    }

    private boolean hasWrittenToday(Member member) {
        boolean result = diaryRepository.existsByMemberIdAndRecordDate(member.getId(), LocalDate.now());
        log.info("오늘 일기 작성 여부: {}", result);
        return result;
    }

    @Transactional
    @Override
    public QuestionResponseDto getItemClickMessage() {
        Member member = memberLoader.getMemberByContextHolder();
        log.info("아이템 클릭 메시지 조회 시작 - memberId: {}", member.getId());

        // 1. 선택된 아이템
        MemberItem selectedItem = member.getMemberItemList().stream()
                .filter(MemberItem::getIsSelected)
                .findFirst()
                .orElseThrow(() -> new GeneralException(ErrorCode.SELECTED_ITEM_NOT_FOUND));
        int level = selectedItem.getItem().getLevel();
        log.info("선택된 아이템 레벨: {}", level);

        // 2. 아이템 고유 멘트 1개
        String itemTag = "ITEM_" + level;
        List<Question> itemMentList = questionRepository.findRandomOneByTag(itemTag, PageRequest.of(0, 1));
        if (itemMentList.isEmpty()) {
            throw new GeneralException(ErrorCode.QUESTION_NOT_FOUND);
        }
        String itemMessage = itemMentList.get(0).getContent();

        // 3. 디폴트 멘트 1개
        List<Question> defaultMentList = questionRepository.findRandomOneByTag("ITEM_DEFAULT", PageRequest.of(0, 1));
        if (defaultMentList.isEmpty()) {
            throw new GeneralException(ErrorCode.QUESTION_NOT_FOUND);
        }
        String defaultMessage = defaultMentList.get(0).getContent();

        // 4. 50% 확률로 하나 선택
        String finalMessage = new Random().nextBoolean() ? itemMessage : defaultMessage;
        log.info("최종 선택된 메시지: {}", finalMessage);

        return QuestionResponseDto.of(finalMessage, itemTag);
    }


    private String getKoreanNameByLevel(int level) {
        return LEVEL_NAME_KR.getOrDefault(level, "미정 아이템");
    }

}

