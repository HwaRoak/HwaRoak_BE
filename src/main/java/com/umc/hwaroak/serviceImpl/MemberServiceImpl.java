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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberLoader memberLoader;
    private final MemberRepository memberRepository;
    private final MemberItemRepository memberItemRepository;

    @Override
    public MemberResponseDto.InfoDto getInfo() {

        Long memberId = memberLoader.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new GeneralException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberResponseDto.InfoDto.builder()
                .userId(member.getUserId())
                .nickname(member.getNickname())
                .introduction(member.getIntroduction())
                .build();
    }

    @Override
    public MemberResponseDto.InfoDto editInfo(MemberRequestDto.editDto requestDto) {

        Member member = memberLoader.getMemberByContextHolder();

        member.update(requestDto.getNickname(), requestDto.getProfileImageUrl(), requestDto.getIntroduction());
        memberRepository.save(member);

        return MemberConverter.toDto(member);
    }

    @Override
    public List<MemberResponseDto.ItemDto> getMyItems() {

        Long memberId = memberLoader.getCurrentMemberId();

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
    @Transactional(readOnly = true)
    public MemberResponseDto.ItemDto changeSelectedItem(Long itemId) {

        Long memberId = memberLoader.getCurrentMemberId();

        // 기존 대표 아이템 해제
        MemberItem currentSelected = memberItemRepository.findByMemberIdAndIsSelectedTrue(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.SELECTED_ITEM_NOT_FOUND));

        currentSelected.setIsSelected(false);


        // 변경하려는 아이템 확인
        MemberItem memberItem = memberItemRepository.findByMemberIdAndItemId(memberId, itemId)
                .orElseThrow(() -> new GeneralException(ErrorCode.ITEM_NOT_FOUND));

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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public MemberResponseDto.ItemDto findSelectedItem() {

        Long memberId = memberLoader.getCurrentMemberId();

        MemberItem currentSelected = memberItemRepository.findByMemberIdAndIsSelectedTrue(memberId)
                .orElseThrow(() -> new GeneralException(ErrorCode.SELECTED_ITEM_NOT_FOUND));

        return MemberResponseDto.ItemDto.builder()
                .item_id(currentSelected.getItem().getId())
                .name(currentSelected.getItem().getName())
                .level(currentSelected.getItem().getLevel())
                .isSelected(currentSelected.getIsSelected())
                .build();
    }
}
