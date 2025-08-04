package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.converter.ItemConverter;
import com.umc.hwaroak.domain.Item;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.dto.response.ItemResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.ItemRepository;
import com.umc.hwaroak.repository.MemberItemRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final MemberItemRepository memberItemRepository;
    private final ItemRepository itemRepository;

    private final MemberLoader memberLoader;

    // 아이템 보상 받기(수령하기)
    @Override
    @Transactional
    public ItemResponseDto.ItemDto receiveItem() {

        Member member = memberLoader.getMemberByContextHolder();

        log.info("수령 가능 아이템 조회 시작...");
        List<MemberItem> receivableItems = findNextAvailableItem();
        if (receivableItems.size() == 0 || receivableItems.isEmpty()) {
            throw new GeneralException(ErrorCode.NOT_FOUND_AVAILABLE_ITEMS);
        }

        log.info("기존 대표 아이템 대표에서 해제 처리...");
        MemberItem selectedItem = memberItemRepository.findByMemberIdAndIsSelectedTrue(member.getId())
                .orElseThrow(() -> new GeneralException(ErrorCode.ITEM_NOT_FOUND));
        selectedItem.setIsSelected(false);
        memberItemRepository.save(selectedItem);

        log.info("새로운 아이템 수령 및 대표 아이템 변경...");

        try {
            MemberItem target = memberItemRepository.changeToReceive(member);
            memberItemRepository.save(selectedItem);

            return ItemConverter.toItemDto(target);
        } catch (Exception e) {
            log.error("에러 발생", e);
            throw new GeneralException(ErrorCode.FAILED_RECEIVE_ITEM);
        }
    }

    // 수령 가능 아이템 추가하기
    @Override
    @Transactional(readOnly = true)
    public void upgradeNextItem(Member member) {

        // 회원의 현재 받을 수 있는 아이템들 조회
        List<MemberItem> memberItemList = member.getMemberItemList();

        // 그 중 가장 레벨이 높은 것
        int lastItemLevel = memberItemList.stream()
                .map(memberItem -> memberItem.getItem().getLevel())
                .max(Integer::compareTo)
                .orElse(1);

        int nextLevel = lastItemLevel + 1;
        Optional<Item> nextItem = itemRepository.findByLevel(nextLevel);

        if (nextItem.isPresent()) {
            MemberItem memberItem = new MemberItem(member, nextItem.get());
            memberItemRepository.save(memberItem);
        } else {
            log.info("더이상 수령 가능한 Item 존재하지 않음");
        }
    }

    // 수령 가능한 아이템 보기 ( = 현재 리워드 기반으로 계산하기)
    @Override
    @Transactional(readOnly = true)
    public List<MemberItem> findNextAvailableItem() {

        Member member = memberLoader.getMemberByContextHolder();

        return memberItemRepository.getAllNotReceivedItems(member);
    }

    // 수령 가능한 다음 아이템 보기(아직 받을 수 없는 다음 단계)
    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto.NextDto getNextItemName() {
        Member member = memberLoader.getMemberByContextHolder();
        // 회원의 현재 받을 수 있는 아이템들 조회
        List<MemberItem> memberItemList = memberItemRepository.findByMemberIdWithItemOrderedByLevel(member.getId());

        // 그 중 가장 레벨이 높은 것
        int lastItemLevel = memberItemList.stream()
                .map(memberItem -> memberItem.getItem().getLevel())
                .max(Integer::compareTo)
                .orElse(1);

        int nextLevel = lastItemLevel + 1;
        Optional<Item> nextItem = itemRepository.findByLevel(nextLevel);
        if (nextItem.isEmpty()) {
            return ItemResponseDto.NextDto.builder()
                    .name("다음 업데이트를 기다려주세요")
                    .dDay(0)
                    .build();
        } else {
            return ItemConverter.toNextDto(nextItem.get(), member);
        }
    }

    // 삭제 시 이전으로 돌아가기
    @Override
    @Transactional
    public void backToStatus(Member member) {
        log.info("마지막 보상 삭제하기...");
        memberItemRepository.backToStatus(member);
    }

    @Override
    @Transactional
    public ItemResponseDto.ItemDto changeSelectedItem(Long itemId) {

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
        if (currentSelected.getId().equals(itemId)) {
            log.warn("대표 아이템이 이미 선택된 상태입니다 - memberId: {}, itemId: {}", memberId, itemId);
            throw new GeneralException(ErrorCode.ALREADY_SELECTED_ITEM);
        }

        currentSelected.setIsSelected(false);


        // 변경하려는 아이템 확인
        MemberItem memberItem = memberItemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("변경하려는 아이템이 존재하지 않습니다 - memberId: {}, itemId: {}", memberId, itemId);
                    return new GeneralException(ErrorCode.ITEM_NOT_FOUND);
                });
        log.info("신규 대표 아이템 ID: {}", memberItem.getId());

        // 대표 지정
        memberItem.setIsSelected(true);

        return ItemConverter.toItemDto(memberItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemResponseDto.ItemDto findSelectedItem() {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info("대표 아이템 조회 요청 - memberId: {}", memberId);

        MemberItem currentSelected = memberItemRepository.findByMemberIdAndIsSelectedTrue(memberId)
                .orElseThrow(() -> {
                    log.warn("대표 아이템을 찾을 수 없습니다 - memberId: {}", memberId);
                    return new GeneralException(ErrorCode.SELECTED_ITEM_NOT_FOUND);
                });

        return ItemConverter.toItemDto(currentSelected);
    }

    @Override
    public List<ItemResponseDto.ItemDto> getMyItems() {

        Long memberId = memberLoader.getCurrentMemberId();
        log.info("내 아이템 목록 조회 요청 - memberId: {}", memberId);

        List<MemberItem> memberItems = memberItemRepository.findByMemberIdWithItemOrderedByLevel(memberId);

        return memberItems.stream()
                .map(ItemConverter::toItemDto)
                .toList();
    }
}
