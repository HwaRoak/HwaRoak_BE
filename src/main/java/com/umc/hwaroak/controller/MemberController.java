package com.umc.hwaroak.controller;


import com.umc.hwaroak.dto.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.umc.hwaroak.service.MemberService;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.(임시로 id 기반 조회)")
    public MemberResponseDTO.InfoDTO getInfo(
            @Parameter(name = "memberId", description = "회원 id", example = "1")
            @PathVariable Long memberId
    ) {
        return memberService.getInfo(memberId);
    }
}
