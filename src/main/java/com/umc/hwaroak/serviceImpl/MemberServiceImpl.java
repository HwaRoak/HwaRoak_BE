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
import com.umc.hwaroak.util.ImageFormatter;
import com.umc.hwaroak.service.EmotionSummaryService;
import com.umc.hwaroak.service.ItemService;
import com.umc.hwaroak.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.umc.hwaroak.service.S3Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    @Value("${app.s3.region}")
    private String s3Region;

    @Value("${app.s3.bucket}")
    private String s3Bucket;

    @Value("${app.s3.key-prefix:profiles}")
    private String keyPrefix;

    @Value("${app.s3.presign-ttl-seconds:300}")
    private int presignTtlSeconds;

    @Value("${app.s3.allowed-content-types}")
    private List<String> allowedContentTypes;

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
    public MemberResponseDto.PresignedUrlResponseDto createPresignedUrl(MemberRequestDto.PresignedUrlRequestDto request){
        Member member = memberLoader.getMemberByContextHolder();

        String contentType = Optional.ofNullable(request.getContentType())
                .map(String::toLowerCase)
                .orElseThrow(() -> new GeneralException(ErrorCode.INVALID_FILE_TYPE));

        if (!isAllowedImageContentType(contentType)) {
            throw new GeneralException(ErrorCode.INVALID_FILE_TYPE);
        }

        String ext = switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png"  -> "png";
            case "image/webp" -> "webp";
            default -> throw new GeneralException(ErrorCode.INVALID_FILE_TYPE);
        };

        String objectKey = keyPrefix + "/" + member.getId() + "/" + UUID.randomUUID() + "." + ext;

        URL uploadUrl = s3Service.createPutPresignedUrl(objectKey, contentType, Duration.ofSeconds(presignTtlSeconds));

        Map<String, String> requiredHeaders = Map.of("Content-Type", contentType);

        return MemberResponseDto.PresignedUrlResponseDto.builder()
                .uploadUrl(uploadUrl.toString())
                .objectKey(objectKey)
                .expiresInSec(presignTtlSeconds)
                .requiredHeaders(requiredHeaders)
                .build();
    }

    @Override
    @Transactional
    public MemberResponseDto.ProfileImageConfirmResponseDto confirmProfileImage(MemberRequestDto.ProfileImageConfirmRequestDto request){
        Member member = memberLoader.getMemberByContextHolder();

        String objectKey = Optional.ofNullable(request.getObjectKey())
                .orElseThrow(() -> new GeneralException(ErrorCode.INVALID_REQUEST));

        // 내 소유 prefix인지 확인
        String expectedPrefix = keyPrefix + "/" + member.getId() + "/";
        if (!objectKey.startsWith(expectedPrefix)) {
            throw new GeneralException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        // 실제 업로드 존재 확인
        s3Service.headObjectOrThrow(objectKey);

        // 기존 이미지 삭제 (URL에서 key 파싱)
        String oldUrl = member.getProfileImage();
        String oldKey = extractKeyFromUrl(oldUrl);
        if (oldKey != null && !oldKey.equals(objectKey)) {
            try {
                s3Service.deleteObjectByKey(oldKey);
            } catch (Exception e) {
                log.warn("이전 프로필 이미지 삭제 실패. key={}", oldKey, e);
            }
        }

        // 새 URL 구성 (S3 퍼블릭 URL)
        String imageUrl = buildS3PublicUrl(objectKey);

        // DB 반영
        member.setProfileImage(imageUrl);
        memberRepository.save(member);

        return MemberResponseDto.ProfileImageConfirmResponseDto.builder()
                .profileImageUrl(imageUrl)
                .build();
    }

    @Override
    @Transactional
    public MemberResponseDto.ProfileImageConfirmResponseDto deleteProfileImage() {
        Member member = memberLoader.getMemberByContextHolder();

        String url = member.getProfileImage();

        String key = extractKeyFromUrl(url);
        if (key != null) {
            try {
                s3Service.deleteObjectByKey(key);
            } catch (Exception e) {
                log.error("프로필 이미지 삭제 실패. key={}", key, e);
                throw new GeneralException(ErrorCode.FILE_DELETE_FAILED);
            }
        } else {
            log.warn("프로필 URL 형식이 예상과 다름. url={}", url);
        }

        member.setProfileImage(null);
        memberRepository.save(member);

        return MemberResponseDto.ProfileImageConfirmResponseDto.builder()
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

    //// 프로필 사진 관련 부가 함수
    private boolean isAllowedImageContentType(String ct) {
        if (ct == null || allowedContentTypes == null || allowedContentTypes.isEmpty()) return false;
        return allowedContentTypes.stream().anyMatch(allowed -> allowed.equalsIgnoreCase(ct));
    }

    private String buildS3PublicUrl(String objectKey) {
        return "https://" + s3Bucket + ".s3." + s3Region + ".amazonaws.com/" + objectKey;
    }

    private String extractKeyFromUrl(String url) {
        if (url == null || url.isBlank()) return null;
        String host = "https://" + s3Bucket + ".s3." + s3Region + ".amazonaws.com/";
        if (!url.startsWith(host)) return null; // 형식 달라지면 안전하게 중단
        return url.substring(host.length());
    }
}
