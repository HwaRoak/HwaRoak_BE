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

    /**
     * 보상 받지 않은 1개의 아이템
     * @return
     */
    @Override
    public MemberItem getNotReceivedItem(Member member) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(memberItem.member.eq(member));
        builder.and(memberItem.isReceived.isFalse());

        return jpaQueryFactory
                .selectFrom(memberItem)
                .where(builder)
                .orderBy(memberItem.item.level.asc())
                .limit(1)
                .fetchOne();
    }

    /**
     * 보상 받지 않은 아이템 전체 조회하기
     * @param member
     * @return
     */
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

    /**
     * 받은 상태로 변경하기
     * @param member
     * @return
     */
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

    /**
     * 보상 받지 않은 상태로 변경 = memberItem에서 삭제
     * @param member
     */
    @Override
    public void backToStatus(Member member) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(memberItem.member.eq(member));
        builder.and(memberItem.isReceived.isTrue());

        MemberItem target = jpaQueryFactory
                .selectFrom(memberItem)
                .where(builder)
                .orderBy(memberItem.item.level.desc()) // 받은 아이템은 내림차순
                .limit(1)
                .fetchOne(); // 가장 최근에 보상 받은 아이템 하나

        if (target != null) {
            if (target.getIsSelected()) {
                // 대표 아이템을 휴지로 변경
                MemberItem baseItem = jpaQueryFactory
                        .selectFrom(memberItem)
                        .where(memberItem.member.eq(member)
                                .and(memberItem.item.level.eq(1)))
                        .fetchOne();
                baseItem.setIsSelected(true);
            }
            jpaQueryFactory.delete(memberItem)
                    .where(memberItem.id.eq(target.getId()))
                    .execute();
        } else {
            log.debug("롤백할 아이템이 없습니다.");
        }
    }
}
