package com.umc.hwaroak.event.listener;

import com.umc.hwaroak.domain.Item;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.event.ItemRollbackEvent;
import com.umc.hwaroak.event.ItemUpdateEvent;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.ItemRepository;
import com.umc.hwaroak.repository.MemberItemRepository;
import com.umc.hwaroak.repository.MemberRepository;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class ItemEventListener {

    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final MemberItemRepository memberItemRepository;

    // 수령 가능 아이템 추가하기
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upgradeNextItem(ItemUpdateEvent event) {

        Member member = memberRepository.findById(event.getMemberId())
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
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

    // 삭제 시 이전으로 돌아가기
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void backToStatus(ItemRollbackEvent event) {

        Member member = memberRepository.findById(event.getMemberId())
                .orElseThrow(() -> new GeneralException(ErrorCode.MEMBER_NOT_FOUND));
        log.info("마지막 보상 삭제하기...");
        memberItemRepository.backToStatus(member);
    }
}
