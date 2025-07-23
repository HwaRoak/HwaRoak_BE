package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.AlarmSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlarmSettingRepository extends JpaRepository<AlarmSetting,Long> {
    Optional<AlarmSetting> findByMemberId(Long memberId);
}
