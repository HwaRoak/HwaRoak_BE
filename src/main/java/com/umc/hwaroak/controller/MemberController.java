package com.umc.hwaroak.controller;


import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.umc.hwaroak.service.MemberService;

@Tag(name = "Member API", description = "사용자 관련 API")
@RestController
@RequestMapping("api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/{memberId}")
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.(임시로 id 기반 조회)")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.InfoDto.class)))
    public MemberResponseDto.InfoDto getInfo() {
        return memberService.getInfo();
    }

    @PatchMapping("")
    @Operation(summary = "회원 정보 수정",
            description = "회원 정보 중 일부 또는 전체를 수정합니다. 수정하지 않을 필드는 요청에 포함하지 않으며, 삭제할 필드는 \"\"(빈 문자열)로 넣습니다")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.InfoDto.class)))
    public MemberResponseDto.InfoDto editInfo(
            @RequestBody MemberRequestDto.editDto requestDto
            ){
        return memberService.editInfo(requestDto);
    }

}
