package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import com.umc.hwaroak.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    // 일기 작성
    @PostMapping("")
    public DiaryResponseDto create(
            @RequestParam Long memberId, // TODO: SpringSecurity 기반으로 변환
            @RequestBody DiaryRequestDto requestDto
            ) {
        return diaryService.createDiary(memberId,requestDto);
    }

    // 일기 조회
    @GetMapping("")
    public DiaryResponseDto get(@RequestParam("date")LocalDate date) {
        return diaryService.readDiary(date);
    }

    // 일기 수정
    @PatchMapping("/{diaryId}")
    public DiaryResponseDto update(@PathVariable Long diaryId, @RequestBody DiaryRequestDto requestDto) {
        return diaryService.updateDiary(diaryId, requestDto);
    }
}
