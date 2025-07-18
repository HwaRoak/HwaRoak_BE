package com.umc.hwaroak.controller;


import com.umc.hwaroak.dto.response.MemberResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.umc.hwaroak.service.MemberService;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member API Controller", description = "회원 관련 API Controller입니다.")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.(임시로 id 기반 조회)")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.class)))
    public MemberResponseDto.InfoDto getInfo() {
        return memberService.getInfo();
    }
}
