package com.umc.hwaroak.converter;

import com.umc.hwaroak.domain.Item;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.dto.response.ItemResponseDto;

public class ItemConverter {


    public static ItemResponseDto.ItemDto toItemDto(MemberItem memberItem) {
        return ItemResponseDto.ItemDto.builder()
                .itemId(memberItem.getId())
                .name(memberItem.getItem().getName())
                .level(memberItem.getItem().getLevel())
                .isSelected(memberItem.getIsSelected())
                .isReceived(memberItem.getIsReceived())
                .build();
    }

    public static ItemResponseDto.NextDto toNextDto(Item item, Member member) {
        return ItemResponseDto.NextDto.builder()
                .name(item.getName())
                .dDay(member.getReward())
                .build();
    }
}
