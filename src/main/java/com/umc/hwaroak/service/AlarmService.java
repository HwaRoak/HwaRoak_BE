package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Alarm;

import java.util.List;

public interface AlarmService {
    List<Alarm> getNotices();
    Alarm getNoticeById(Long id);
}
