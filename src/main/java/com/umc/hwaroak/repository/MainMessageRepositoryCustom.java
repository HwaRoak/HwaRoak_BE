package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.MainMessage;
import com.umc.hwaroak.domain.common.MainMessageType;

import java.util.Optional;

public interface MainMessageRepositoryCustom {
    Optional<MainMessage> findRandomByType(MainMessageType type);
}
