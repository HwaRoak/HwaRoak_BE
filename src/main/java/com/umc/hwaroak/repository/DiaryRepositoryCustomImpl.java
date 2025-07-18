package com.umc.hwaroak.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.hwaroak.converter.DiaryConverter;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.QDiary;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DiaryRepositoryCustomImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QDiary diary = QDiary.diary;

    @Override
    public List<DiaryResponseDto> findDiaryByMonth(Long memberId, Integer month) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(diary.member.id.eq(memberId));
        builder.and(diary.recordDate.month().eq(month));

        List<Diary> results = jpaQueryFactory
                .selectFrom(diary)
                .where(builder)
                .fetch();
        return results.stream()
                .map(DiaryConverter::toDto)
                .toList();
    }

    @Override
    public void deleteForever() {

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(diary.isDeleted.eq(true));
        builder.and(diary.deletedAt.loe(LocalDate.now().minusDays(30)));

        jpaQueryFactory.delete(diary)
                .where(builder)
                .execute();
    }
}
