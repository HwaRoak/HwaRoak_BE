package com.umc.hwaroak.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.hwaroak.domain.MainMessage;
import com.umc.hwaroak.domain.QMainMessage;
import com.umc.hwaroak.domain.common.MainMessageType;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MainMessageRepositoryImpl implements MainMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MainMessage> findRandomByType(MainMessageType type) {
        QMainMessage mainMessage = QMainMessage.mainMessage;

        List<MainMessage> result = queryFactory
                .selectFrom(mainMessage)
                .where(mainMessage.type.eq(type))
                .fetch();

        if (result.isEmpty()) return Optional.empty();

        Collections.shuffle(result); // Java 랜덤 셔플
        return Optional.of(result.get(0));
    }
}