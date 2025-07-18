package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.request.DiaryRequestDto;
import com.umc.hwaroak.dto.response.DiaryResponseDto;
import com.umc.hwaroak.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
@Tag(name = "일기 API Controller", description = "일기 관련 API Controller입니다.")
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(summary = "일기 작성 API", description = "일기 작성 API입니다.")
    @PostMapping("")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DiaryResponseDto.class)))
    public DiaryResponseDto create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody DiaryRequestDto requestDto
            ) {
        return diaryService.createDiary(requestDto);
    }


    @Operation(summary = "일기 조회 API", description = """
    일기 조회 API입니다. parmaeter로 조회하려는 날짜를 입력해주세요.
    """)
    @GetMapping("")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DiaryResponseDto.class)))
    public DiaryResponseDto get(@RequestParam("date")LocalDate date) {
        return diaryService.readDiary(date);
    }

    @Operation(summary = "일기 수정 API", description = """
            일기 수정 API입니다. 수정하려는 일기의 ID를 작성해주세요.""")
    @PatchMapping("/{diaryId}")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DiaryResponseDto.class)))
    public DiaryResponseDto update(@PathVariable Long diaryId, @RequestBody DiaryRequestDto requestDto) {
        return diaryService.updateDiary(diaryId, requestDto);
    }
}
