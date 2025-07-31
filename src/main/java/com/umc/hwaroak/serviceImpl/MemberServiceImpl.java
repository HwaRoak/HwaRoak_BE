package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.converter.MemberConverter;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.MemberItemRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.umc.hwaroak.service.S3Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberLoader memberLoader;
    private final MemberRepository memberRepository;
    private final MemberItemRepository memberItemRepository;
    private final S3Service s3Service;

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
                .introduction(member.getIntroduction())
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
    public List<MemberResponseDto.ItemDto> getMyItems() {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info("내 아이템 목록 조회 요청 - memberId: {}", memberId);

        List<MemberItem> memberItems = memberRepository.findByMemberIdWithItemOrderedByLevel(memberId);

        return memberItems.stream()
                .map(mi -> MemberResponseDto.ItemDto.builder()
                        .item_id(mi.getItem().getId())
                        .name(mi.getItem().getName())
                        .level(mi.getItem().getLevel())
                        .isSelected(mi.getIsSelected())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public MemberResponseDto.ItemDto changeSelectedItem(Long itemId) {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info("대표 아이템 변경 요청 - memberId: {}, 요청 itemId: {}", memberId, itemId);

        // 기존 대표 아이템 해제
        MemberItem currentSelected = memberItemRepository.findByMemberIdAndIsSelectedTrue(memberId)
                .orElseThrow(() -> {
                    log.warn("변경하려는 아이템이 존재하지 않습니다 - memberId: {}, itemId: {}", memberId, itemId);
                    return new GeneralException(ErrorCode.SELECTED_ITEM_NOT_FOUND);
                });
        log.info("기존 대표 아이템 ID: {}", currentSelected.getItem().getId());

        // 이미 대표 아이템이라면 예외 발생
        if (currentSelected.getItem().getId().equals(itemId)) {
            log.warn("대표 아이템이 이미 선택된 상태입니다 - memberId: {}, itemId: {}", memberId, itemId);
            throw new GeneralException(ErrorCode.ALREADY_SELECTED_ITEM);
        }

        currentSelected.setIsSelected(false);


        // 변경하려는 아이템 확인
        MemberItem memberItem = memberItemRepository.findByMemberIdAndItemId(memberId, itemId)
                .orElseThrow(() -> {
                    log.warn("변경하려는 아이템이 존재하지 않습니다 - memberId: {}, itemId: {}", memberId, itemId);
                    return new GeneralException(ErrorCode.ITEM_NOT_FOUND);
                });
        log.info("신규 대표 아이템 ID: {}", memberItem.getItem().getId());

        // 대표 지정
        memberItem.setIsSelected(true);

        return MemberResponseDto.ItemDto.builder()
                .item_id(memberItem.getItem().getId())
                .name(memberItem.getItem().getName())
                .level(memberItem.getItem().getLevel())
                .isSelected(memberItem.getIsSelected())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponseDto.ItemDto findSelectedItem() {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info("대표 아이템 조회 요청 - memberId: {}", memberId);

        MemberItem currentSelected = memberItemRepository.findByMemberIdAndIsSelectedTrue(memberId)
                .orElseThrow(() -> {
                    log.warn("대표 아이템을 찾을 수 없습니다 - memberId: {}", memberId);
                    return new GeneralException(ErrorCode.SELECTED_ITEM_NOT_FOUND);
                });

        return MemberResponseDto.ItemDto.builder()
                .item_id(currentSelected.getItem().getId())
                .name(currentSelected.getItem().getName())
                .level(currentSelected.getItem().getLevel())
                .isSelected(currentSelected.getIsSelected())
                .build();
    }


    @Override
    @Transactional
    public String uploadProfileImage(MultipartFile file) {
        Member member = memberLoader.getMemberByContextHolder();
        String directory = "profiles/" + member.getId();

        // 기존 이미지 삭제 (기본 이미지가 아닐 때만)
        if (member.getProfileImage() != null) {
            s3Service.deleteFile(member.getProfileImage());
        }

        String uploadedUrl = s3Service.uploadProfileImage(file, directory);
        member.setProfileImage(uploadedUrl);
        memberRepository.save(member);

        return uploadedUrl;
    }


    @Override
    @Transactional
    public void deleteProfileImage() {
        Member member = memberLoader.getMemberByContextHolder();

        if (member.getProfileImage() != null ) {
            s3Service.deleteFile(member.getProfileImage());
        }
        member.setProfileImage(null);
        memberRepository.save(member);
    }

}
