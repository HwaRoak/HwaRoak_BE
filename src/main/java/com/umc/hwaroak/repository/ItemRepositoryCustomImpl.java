package com.umc.hwaroak.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.domain.QMemberItem;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QMemberItem memberItem = QMemberItem.memberItem;

    @Override
    public List<MemberItem> getAllNotReceivedItems(Member member) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(memberItem.member.eq(member));
        builder.and(memberItem.isReceived.isFalse());

        return jpaQueryFactory
                .selectFrom(memberItem)
                .where(builder)
                .orderBy(memberItem.item.level.asc())
                .fetch();
    }

    @Override
    public MemberItem changeToReceive(Member member) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(memberItem.member.eq(member));
        builder.and(memberItem.isReceived.isFalse());

        MemberItem target = jpaQueryFactory
                .selectFrom(memberItem)
                .where(
                        memberItem.member.eq(member),
                        memberItem.isReceived.isFalse()
                )
                .orderBy(memberItem.item.level.asc())
                .limit(1)
                .fetchOne();

        if (target != null) {
            target.setIsSelected(true);
            target.setIsReceived(true);
            // 변경 감지에 의해 자동으로 update 쿼리 발생
        } else {
            log.error("수령 가능한 아이템이 없습니다.");
            throw new GeneralException(ErrorCode.ITEM_NOT_FOUND);
        }
        return target;
    }

    public void backToStatus(Member member) {
        List<MemberItem> receivedItemList = jpaQueryFactory
                                    .selectFrom(memberItem)
                                    .where(memberItem.isReceived.isTrue())
                                    .orderBy(memberItem.item.level.desc())
                                    .fetch();
        if (receivedItemList.size() != 1) {
            MemberItem target = receivedItemList.get(0);
            jpaQueryFactory
                    .delete(memberItem)
                    .where(memberItem.id.eq(target.getId()))
                    .execute();
        }
    }
}
