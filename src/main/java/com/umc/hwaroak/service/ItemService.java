package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.dto.response.ItemResponseDto;

import java.util.List;

public interface ItemService {
    ItemResponseDto.ReceivedDto upgradeNextItem(Member member);
    void receiveItem();
    List<MemberItem> findNextAvailableItem();
    ItemResponseDto.NextDto getNextItemName();
    void backToStatus(Member member);
}
