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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("api/v1/diary")
@RequiredArgsConstructor
@Tag(name = "일기 API Controller", description = "일기 관련 API Controller입니다.")
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(summary = "일기 작성 API", description = """
    일기 작성 API입니다.<br>
    기록할 감정은 3개를 넘으면 안 됩니다.
    """)
    @PostMapping("")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DiaryResponseDto.class)))
    public DiaryResponseDto.CreateDto create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody DiaryRequestDto requestDto
            ) {
        return diaryService.createDiary(requestDto);
    }


    @Operation(summary = "날짜별 일기 조회 API", description = """
    일기 조회 API입니다. parmaeter로 조회하려는 날짜를 입력해주세요.<br>
    yyyy-MM-dd의 날짜 형식을 지켜주셔야합니다.
    """)
    @GetMapping("")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DiaryResponseDto.class)))
    public DiaryResponseDto.ThumbnailDto get(@RequestParam("date") LocalDateTime date) {
        return diaryService.readDiary(date);
    }

    @Operation(summary = "일기 상세 조회 API", description = """
    일기 상세보기 API입니다. Path에 해당 일기의 ID값을 입력해주세요.
    """)
    @GetMapping("/{diaryId}")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DiaryResponseDto.class)))
    public DiaryResponseDto.DetailDto getDetail(@PathVariable Long diaryId) {
        return diaryService.readDiaryWithDetail(diaryId);
    }

    @Operation(summary = "일기 수정 API", description = """
            일기 수정 API입니다. 수정하려는 일기의 ID를 작성해주세요.<br>
            RequestBody 방식은 일기 작성 API와 동일합니다.
            """)
    @PatchMapping("/{diaryId}")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DiaryResponseDto.class)))
    public DiaryResponseDto.CreateDto update(@PathVariable Long diaryId, @RequestBody DiaryRequestDto requestDto) {
        return diaryService.updateDiary(diaryId, requestDto);
    }

    @Operation(summary = "월별 일기 전체 조회 API", description = """
            월별 일기를 전체 조회하는 API입니다.<br>
            Parameter에 조회하려는 일기의 년도와 달을 작성해주세요.
            """)
    @GetMapping("/monthly")
    public List<DiaryResponseDto.ThumbnailDto> getAllDiaries(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month
    ) {
        return diaryService.readMonthDiary(year, month);
    }

    @Operation(summary = "일기 삭제 API", description = """
            일기 삭제 API입니다.<br>
            삭제할 일기의 ID를 입력해주세요.
            """)
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<?> delete(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok().body("일기 삭제에 성공하였습니다.");
    }
}
