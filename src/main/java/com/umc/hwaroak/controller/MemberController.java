package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import com.umc.hwaroak.service.EmotionSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.umc.hwaroak.service.MemberService;

import java.util.List;

@Tag(name = "Member API", description = "사용자 관련 API")
@RestController
@RequestMapping("api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmotionSummaryService emotionSummaryService;

    @GetMapping("")
    @Operation(summary = "회원 정보 조회", description = "회원 정보를 조회합니다.")
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

    @GetMapping("/items")
    @Operation(summary = "보유 아이템 리스트 조회", description = "사용자의 아이템 목록을 조회합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.ItemDto.class)))
    public List<MemberResponseDto.ItemDto> getMyItems(){
        return memberService.getMyItems();
    }

    @GetMapping("/items/selected")
    @Operation(summary = "대표 아이템 조회", description = "사용자의 대표 아이템을 조회합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.ItemDto.class)))
    public MemberResponseDto.ItemDto getMySelectedItem(){
        return memberService.findSelectedItem();
    }

    @PatchMapping("/items/{itemId}/selected")
    @Operation(summary = "대표 아이템 변경", description = "대표 아이템을 선택한 아이템으로 변경합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.ItemDto.class)))
    public MemberResponseDto.ItemDto changeSelectedItem(
            @Schema(description = "변경하려는 아이템의 id", example = "1")
            @PathVariable Long itemId
    ){
        return memberService.changeSelectedItem(itemId);
    }

    @GetMapping("/preview")
    @Operation(summary = "마이페이지용 preview 조회",
            description = "마이페이지 preview를 조회합니다. 감정통계에서는 반올림 때문에 비율 총합이 100이 넘을 수도 있습니다. " +
                    "분석할 데이터가 없는 경우 null이 반환되며, " +
                    "프로필 이미지 url이 비어있을 경우 빈 문자열(\"\")을 반환하니 기본 이미지로 처리하면 됩니다.)")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.PreviewDto.class)))
    public MemberResponseDto.PreviewDto getEmotionSummary() {

        return memberService.getMyPagePreview();
    }

    @GetMapping("emotions/{summaryMonth}")
    @Operation(summary = "감정분석 상세 조회", description = "특정 달의 감정분석을 조회합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.DetailDto.class)))
    public MemberResponseDto.DetailDto getDetailEmotionSummary(
            @Schema(description = "조회할 연월", example = "2025-07")
            @PathVariable String summaryMonth
    ) {
        return emotionSummaryService.getDetailEmotionSummary(summaryMonth);
    }

}
