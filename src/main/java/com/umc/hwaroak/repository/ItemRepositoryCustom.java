package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;

import java.util.List;

public interface ItemRepositoryCustom {

    // 보상받지 않은 1개의 아이템
    MemberItem getNotReceivedItem(Member member);
    // 아직 수령하지 않은 아이템 계산하기
    List<MemberItem> getAllNotReceivedItems(Member member);
    // 수령 받기
    MemberItem changeToReceive(Member member);
    // 수령하지 못한 상태로 변경하기
    void backToStatus(Member member);
    // 다음 목표 아이템 이름 조회
    String nextItemName(Member member);
}
