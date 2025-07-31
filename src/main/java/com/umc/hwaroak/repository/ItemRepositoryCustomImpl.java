package com.umc.hwaroak.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.MemberItem;
import com.umc.hwaroak.domain.QMemberItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryCustomImpl implements ItemRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QMemberItem memberItem = QMemberItem.memberItem;

    @Override
    public List<MemberItem> getAllNotReceivedItems(Member member) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(memberItem.member.eq(member));
        builder.and(memberItem.isReceived.isTrue());

        return jpaQueryFactory
                .selectFrom(memberItem)
                .where(builder)
                .orderBy(memberItem.item.level.asc())
                .fetch();
    }

    @Override
    public void changeToReceive(Member member) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(memberItem.member.eq(member));
        builder.and(memberItem.isReceived.isFalse());

        Long targetId = jpaQueryFactory.select(memberItem.id)
                .from(memberItem)
                .where(builder)
                .orderBy(memberItem.item.level.asc())
                .limit(1)
                .fetchOne();

        if (targetId != null) {
            jpaQueryFactory
                    .update(memberItem)
                    .set(memberItem.isReceived, true)
                    .where(memberItem.id.eq(targetId))
                    .execute();
        }
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
