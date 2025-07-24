package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.converter.DiaryConverter;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Item;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.domain.common.Emotion;
import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.ItemRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.DiaryService;
import com.umc.hwaroak.util.OpenAiUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryServiceImpl implements DiaryService {

    private final OpenAiUtil openAiUtil;

    private final MemberLoader memberLoader;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public DiaryResponseDto.CreateDto createDiary(DiaryRequestDto requestDto) {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info(requestDto.getContent());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        if (diaryRepository.findByRecordDate(memberId, requestDto.getRecordDate()).isPresent()) {
            log.info("{} 날짜의 일기 발견...", requestDto.getRecordDate());
            throw new GeneralException(ErrorCode.DIARY_ALREADY_RECORDED);
        }

        // 요청 감정의 개수 확인하기
        if (requestDto.getEmotionList().size() > 3) {
            throw new GeneralException(ErrorCode.TOO_MANY_EMOTIONS);
        }

        Diary diary = DiaryConverter.toDiary(member, requestDto);
        log.info("작성 일기 내용: " + requestDto.getContent());
        diary.setFeedback(openAiUtil.reviewDiary(diary.getContent()));
        diaryRepository.save(diary);

        String nextItemName = upgradeNextItem(); // 리워드 차감 및 다음 아이템 이름

        boolean isRewardAvailable = isRewardAvailable(member); // 현재 리워드 수령 가능 상태 판단

        return DiaryConverter.toCreateDto(diary, nextItemName, isRewardAvailable);
    }

    @Transactional(readOnly = true)
    public DiaryResponseDto.ThumbnailDto readDiary(LocalDate date) {

        Member member = memberLoader.getMemberByContextHolder();

        return DiaryConverter.toThumbnailDto(diaryRepository.findByRecordDate(member.getId(), date)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public DiaryResponseDto.DetailDto readDiaryWithDetail(Long diaryId) {

        memberLoader.getMemberByContextHolder();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryConverter.toDetailDto(diary);
    }

    @Transactional
    public DiaryResponseDto.CreateDto updateDiary(Long diaryId, DiaryRequestDto requestDto) {

        Member member = memberLoader.getMemberByContextHolder();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));

        List<Emotion> emotionList = requestDto.getEmotionList().stream()
                        .map(Emotion::fromDisplayName)
                                .collect(Collectors.toList());

        // 요청 감정의 개수 확인하기
        if (emotionList.size() > 3) {
            throw new GeneralException(ErrorCode.TOO_MANY_EMOTIONS);
        }

        diary.update(requestDto.getContent(), emotionList);
        diary.setFeedback(openAiUtil.reviewDiary(requestDto.getContent()));
        diaryRepository.save(diary);

        String nextItemName = getNextItemName(member);
        boolean isRewardAvailable = isRewardAvailable(member);
        return DiaryConverter.toCreateDto(diary, nextItemName, isRewardAvailable);
    }

    // 월별 일기 전체 조회하기
    @Transactional(readOnly = true)
    public List<DiaryResponseDto.ThumbnailDto> readMonthDiary(Integer year, Integer month) {

        Long memberId = memberLoader.getCurrentMemberId();
        return diaryRepository.findDiaryByMonth(memberId, year, month);
    }

    @Transactional
    public void deleteDiary(Long diaryId) {

        Member member = memberLoader.getMemberByContextHolder();

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new GeneralException(ErrorCode.DIARY_NOT_FOUND));
        member.setReward(member.getReward() + 1);

        // TODO: 삭제 후에 아이템 관련 처리 필요
        diaryRepository.delete(diary);
    }

    private String upgradeNextItem() {
        Member member = memberLoader.getMemberByContextHolder();

        long diaryCnt = diaryRepository.countByMemberId(member.getId());

        List<MemberItem> memberItemList = member.getMemberItemList();
        int lastItemLevel = memberItemList.stream()
                .map(memberItem -> memberItem.getItem().getLevel())
                .max(Integer::compareTo)
                .orElse(1);

        int nextLevel = lastItemLevel + 1;
        Optional<Item> nextItem = itemRepository.findByLevel(nextLevel);

        // 보상 지급은 하지 않고 상태만 갱신
        if (!(diaryCnt > 0 && diaryCnt % 7 == 0)) {
            int currentDday = member.getReward();
            member.setReward(Math.max(0, currentDday - 1));
        }

        memberRepository.save(member);

        // 보상 받을 아이템의 이름 리턴중
        return nextItem.map(Item::getName).orElse("다음 업데이트를 기다려주세요.");
    }


    // 현재 보유 아이템 기준으로 다음 아이템 이름 조회
    private String getNextItemName(Member member) {
        List<MemberItem> memberItemList = member.getMemberItemList();

        int lastItemLevel = memberItemList.stream()
                .map(mi -> mi.getItem().getLevel())
                .max(Integer::compareTo)
                .orElse(1);

        return itemRepository.findByLevel(lastItemLevel + 1)
                .map(Item::getName)
                .orElse("다음 업데이트를 기다려주세요.");
    }

    @Transactional
    public String claimReward() {
        Member member = memberLoader.getMemberByContextHolder();
        long diaryCnt = diaryRepository.countByMemberId(member.getId());

        if (diaryCnt == 0 || diaryCnt % 7 != 0) {
            throw new GeneralException(ErrorCode.REWARD_NOT_AVAILABLE);
        }

        List<MemberItem> memberItemList = member.getMemberItemList();
        int lastItemLevel = memberItemList.stream()
                .map(mi -> mi.getItem().getLevel())
                .max(Integer::compareTo)
                .orElse(1);

        Item item = itemRepository.findByLevel(lastItemLevel + 1)
                .orElseThrow(() -> new GeneralException(ErrorCode.ITEM_NOT_FOUND));

        memberItemList.add(new MemberItem(member, item));
        member.setReward(7); // 보상 받았으므로 D-Day 초기화
        memberRepository.save(member);

        // 보상 받은 아이템의 이름 리턴중
        return item.getName();
    }


    private boolean isRewardAvailable(Member member) {
        long diaryCount = diaryRepository.countByMemberId(member.getId());
        return diaryCount > 0 && diaryCount % 7 == 0;
    }
}
