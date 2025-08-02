package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.converter.ItemConverter;
import com.umc.hwaroak.domain.Item;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.dto.response.ItemResponseDto;
import com.umc.hwaroak.repository.ItemRepository;
import com.umc.hwaroak.repository.MemberItemRepository;
import com.umc.hwaroak.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final MemberItemRepository memberItemRepository;
    private final ItemRepository itemRepository;

    private final MemberLoader memberLoader;

    // 수령 가능 아이템 추가하기
    public ItemResponseDto.ReceivedDto upgradeNextItem(Member member) {

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
            return ItemConverter.toReceivedDto(memberItem);
        } else {
            return ItemResponseDto.ReceivedDto.builder()
                    .id(null)
                    .name("다음 업데이트를 기다려주세요.")
                    .dDay(0)
                    .build();
        }
    }

    // 아이템 보상 받기(수령하기)
    public void receiveItem() {

        Member member = memberLoader.getMemberByContextHolder();
        memberItemRepository.changeToReceive(member);
   }

    // 수령 가능한 아이템 보기 ( = 현재 리워드 기반으로 계산하기)
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
    public void backToStatus(Member member) {
        log.info("마지막 보상 삭제하기...");
        memberItemRepository.backToStatus(member);
    }
}
