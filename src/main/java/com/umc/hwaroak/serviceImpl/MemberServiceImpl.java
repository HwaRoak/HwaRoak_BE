package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.infrastructure.authentication.MemberLoader;
import com.umc.hwaroak.converter.MemberConverter;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.EmotionCategory;
import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.util.ImageFormatter;
import com.umc.hwaroak.service.EmotionSummaryService;
import com.umc.hwaroak.service.ItemService;
import com.umc.hwaroak.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.umc.hwaroak.service.S3Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberLoader memberLoader;
    private final MemberRepository memberRepository;

    private final S3Service s3Service;
  
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
                .profileImgUrl(member.getProfileImage())
                .build();
    }

    @Override
    public MemberResponseDto.InfoDto editInfo(MemberRequestDto.editDto requestDto) {

        Member member = memberLoader.getMemberByContextHolder();
        log.info("회원 정보 수정 요청 - memberId: {}", member.getId());

        member.update(requestDto.getNickname(), requestDto.getIntroduction());
        memberRepository.save(member);

        return MemberConverter.toDto(member);
    }

    @Override
    @Transactional
    public MemberResponseDto.ProfileImageDto uploadProfileImage(MultipartFile file) {
        Member member = memberLoader.getMemberByContextHolder();
        String directory = "profiles/" + member.getId();

        // 이미지 파일 여부 검사
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new GeneralException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 기존 이미지 삭제
        if (member.getProfileImage() != null) {
            s3Service.deleteFile(member.getProfileImage());
        }

        try {
            // 포맷팅 (리사이즈 + JPEG 변환)
            ByteArrayInputStream formattedImage = ImageFormatter.convertToWebP(
                    file.getInputStream(), 300, 300
            );

            String uploadedUrl = s3Service.uploadProfileImage(formattedImage, directory);
            member.setProfileImage(uploadedUrl);
            memberRepository.save(member);

            return MemberResponseDto.ProfileImageDto.builder()
                    .profileImageUrl(uploadedUrl)
                    .build();
        } catch (IOException e) {
            log.error("프로필 이미지 업로드 중 예외 발생", e);
            throw new GeneralException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }



    @Override
    @Transactional
    public MemberResponseDto.ProfileImageDto deleteProfileImage() {
        Member member = memberLoader.getMemberByContextHolder();

        if (member.getProfileImage() != null ) {
            s3Service.deleteFile(member.getProfileImage());
        }
        member.setProfileImage(null);
        memberRepository.save(member);

        return MemberResponseDto.ProfileImageDto.builder()
                .profileImageUrl(null)
                .build();
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
