package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.response.MainMessageResponseDto;
import com.umc.hwaroak.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Reward", description = "보상 관련 API")
@RestController
@RequestMapping("/api/v1/reward")
@RequiredArgsConstructor
public class RewardController {

    private final DiaryService diaryService;

    @Operation(summary = "보상 지급", description = "보상 수령 가능 조건을 만족하면 보상을 지급합니다.")
    @ApiResponse(responseCode = "200", description = "보상 지급 성공")
    @PostMapping
    public MainMessageResponseDto claimReward() {
        return diaryService.claimReward(); // 💡 ApiResponse<T>로 자동 래핑됨
    }
}
