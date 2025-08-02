package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.converter.MemberConverter;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.EmotionCategory;
import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.EmotionSummaryService;
import com.umc.hwaroak.service.ItemService;
import com.umc.hwaroak.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberLoader memberLoader;
    private final MemberRepository memberRepository;

    private final EmotionSummaryService emotionSummaryService;
    private final ItemService itemService;
    private final DiaryRepository diaryRepository;

    @Override
    public MemberResponseDto.InfoDto getInfo() {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info("회원 정보 조회 요청 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("회원 정보를 찾을 수 없습니다 - memberId: {}", memberId);
                    return new GeneralException(ErrorCode.MEMBER_NOT_FOUND);
                });

        return MemberResponseDto.InfoDto.builder()
                .memberId(member.getId())
                .userId(member.getUserId())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImage())
                .introduction(member.getIntroduction())
                .build();
    }

    @Override
    public MemberResponseDto.InfoDto editInfo(MemberRequestDto.editDto requestDto) {

        Member member = memberLoader.getMemberByContextHolder();
        log.info("회원 정보 수정 요청 - memberId: {}", member.getId());

        member.update(requestDto.getNickname(), requestDto.getProfileImageUrl(), requestDto.getIntroduction());
        memberRepository.save(member);

        return MemberConverter.toDto(member);
    }

    @Override
    @Transactional
    public MemberResponseDto.PreviewDto getMyPagePreview() {
        // 사용자 정보
        Member member = memberLoader.getMemberByContextHolder();
        Long memberId = member.getId();
        log.info("마이페이지 프리뷰 조회 시작 - memberId: {}", memberId);

        // 닉네임
        String nickname = member.getNickname();

        // 프로필 사진 url - 없을 경우 빈 문자열로 반환
        String profileImageUrl = Optional.ofNullable(member.getProfileImage())
                .orElse("");

        // 감정 통계 간단 ver.
        String yearMonth = YearMonth.now().toString(); // 오늘 날짜 기준으로 이번 달 구하기 ex. "2025-07"
        Map<EmotionCategory, MemberResponseDto.EmotionCount> summary = emotionSummaryService.getPreviewEmotionSummary(yearMonth);

        // 기록한 화록
        Long totalDiary = diaryRepository.countByMemberId(memberId);

        // 다음 아이템 d-day
        Integer reward = member.getReward();    // 남은 일자
        String nextItemName = itemService.getNextItemName().getName();

        log.info("마이페이지 프리뷰 응답 완료 - memberId: {}", memberId);

        return MemberResponseDto.PreviewDto.builder()
                .nickname(nickname)
                .profileImgUrl(profileImageUrl)
                .emotionSummary(summary)
                .totalDiary(totalDiary)
                .reward(reward)
                .nextItemName(nextItemName)
                .build();
    }
}
