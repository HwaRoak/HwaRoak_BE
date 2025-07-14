package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
