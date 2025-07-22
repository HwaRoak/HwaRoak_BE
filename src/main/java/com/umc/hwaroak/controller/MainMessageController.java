package com.umc.hwaroak.controller;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.response.MainMessageResponseDto;
import com.umc.hwaroak.service.MainMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "MainMessage", description = "메인화면 메시지 관련 API")
@RestController
@RequestMapping("/api/v1/main-message")
@RequiredArgsConstructor
public class MainMessageController {

    private final MainMessageService mainMessageService;
    private final MemberLoader memberLoader;

    @Operation(summary = "메인 메시지 조회", description = "조건 우선순위에 따라 사용자에게 보여줄 메인 메시지를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "메시지 조회 성공")
    @GetMapping
    public MainMessageResponseDto getMainMessage() {
        Member member = memberLoader.getMemberByContextHolder();
        return mainMessageService.getMainMessage(member);
    }
}
