package com.umc.hwaroak.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.hwaroak.converter.DiaryConverter;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.QDiary;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DiaryRepositoryCustomImpl implements DiaryRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;
    private final QDiary diary = QDiary.diary;

    @Override
    public List<DiaryResponseDto.ThumbnailDto> findDiaryByMonth(Long memberId, Integer year, Integer month) {

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(diary.member.id.eq(memberId));
        builder.and(diary.recordDate.year().eq(year));
        builder.and(diary.recordDate.month().eq(month));

        List<Diary> results = jpaQueryFactory
                .selectFrom(diary)
                .where(builder)
                .fetch();
        return results.stream()
                .map(DiaryConverter::toThumbnailDto)
                .toList();
    }

    @Override
    public List<Diary> findAllDiariesByYearMonth(Long memberId, int year, int month) {
        return jpaQueryFactory
                .selectFrom(diary)
                .where(
                        diary.member.id.eq(memberId),
                        diary.recordDate.year().eq(year),
                        diary.recordDate.month().eq(month)
                )
                .fetch();
    }
}
