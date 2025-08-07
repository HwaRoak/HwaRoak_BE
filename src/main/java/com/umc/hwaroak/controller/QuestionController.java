package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.response.QuestionResponseDto;
import com.umc.hwaroak.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/question")
@RequiredArgsConstructor
@Tag(name = "Question", description = "메인 화면 질문(멘트) 관련 API")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(
            summary = "메인 메시지 조회",
            description = """
            메인 화면에 노출할 메시지를 우선순위 조건에 따라 반환합니다.<br><br>
            - 1순위: 보상 수령 가능<br>
            - 2순위: 읽지 않은 불씨 알람 존재<br>
            - 3순위: 오늘 일기 미작성<br>
            - 4순위: 오늘 일기 작성 시 감정 기반 메시지
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "메인 메시지 조회 성공",
            content = @Content(schema = @Schema(implementation = QuestionResponseDto.class))
    )
    @GetMapping
    public QuestionResponseDto getMainMessage() {
        return questionService.getMainMessage();
    }
}
