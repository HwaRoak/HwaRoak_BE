package com.umc.hwaroak.controller;


import com.umc.hwaroak.dto.MemberResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
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
    public MemberResponseDTO.InfoDTO getInfo(
            @PathVariable Long memberId
    ) {
        return memberService.getInfo(memberId);
    }
}
