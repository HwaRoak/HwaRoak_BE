package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.dto.response.ItemResponseDto;

import java.util.List;

public interface ItemService {

    ItemResponseDto.ItemDto receiveItem();
    List<MemberItem> findNextAvailableItem();
    ItemResponseDto.NextDto getNextItemName();
    ItemResponseDto.ItemDto changeSelectedItem(Long itemId);
    ItemResponseDto.ItemDto findSelectedItem();
    List<ItemResponseDto.ItemDto> getMyItems();
    public List<MemberItem> findNotReceivedItem();
}
