package com.umc.hwaroak.converter;

import com.umc.hwaroak.domain.Item;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.dto.response.ItemResponseDto;

public class ItemConverter {

    public static ItemResponseDto.ReceivedDto toReceivedDto(MemberItem item) {
        return ItemResponseDto.ReceivedDto.builder()
                .id(item.getId())
                .name(item.getItem().getName())
                .dDay(item.getMember().getReward())
                .build();
    }

    public static ItemResponseDto.NextDto toNextDto(Item item, Member member) {
        return ItemResponseDto.NextDto.builder()
                .name(item.getName())
                .dDay(member.getReward())
                .build();
    }
}
