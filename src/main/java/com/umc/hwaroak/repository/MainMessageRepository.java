package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.MainMessage;
import com.umc.hwaroak.domain.common.MainMessageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MainMessageRepository extends JpaRepository<MainMessage, Long>, MainMessageRepositoryCustom {
    Optional<MainMessage> findByTypeAndItemLevel(MainMessageType type, int itemLevel);

}
