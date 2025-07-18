package com.umc.hwaroak.service;

import com.umc.hwaroak.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DiaryScheduler {

    private final DiaryRepository diaryRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void deleteDiary() {
        log.info("일기 영구 삭제 시작 ...");
        diaryRepository.deleteForever();
    }
}
