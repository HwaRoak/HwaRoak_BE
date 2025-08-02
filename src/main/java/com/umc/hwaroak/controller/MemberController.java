package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.request.AlarmSettingRequestDto;
import com.umc.hwaroak.dto.response.AlarmSettingResponseDto;
import com.umc.hwaroak.dto.response.MemberResponseDto;
import com.umc.hwaroak.dto.request.MemberRequestDto;
import com.umc.hwaroak.service.AlarmSettingService;
import com.umc.hwaroak.service.EmotionSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.umc.hwaroak.service.MemberService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Member API", description = "사용자 관련 API")
@RestController
@RequestMapping("api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final EmotionSummaryService emotionSummaryService;
    private final AlarmSettingService alarmSettingService;

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


    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "프로필 이미지 업로드",
            description = "사용자의 프로필 이미지를 업로드하고 URL을 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "업로드된 이미지 URL 반환",
            content = @Content(schema = @Schema(implementation = String.class))
    )
    public MemberResponseDto.ProfileImageDto uploadProfileImage(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("image") MultipartFile image
    ) {
        return memberService.uploadProfileImage(image);
    }


    @PatchMapping("/profile-image")
    @Operation(summary = "프로필 이미지 삭제", description = "사용자의 프로필 이미지를 삭제하고 기본 이미지로 변경합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MemberResponseDto.ProfileImageDto.class)))
    public MemberResponseDto.ProfileImageDto deleteProfileImage() {
        return memberService.deleteProfileImage();
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

    @GetMapping("/alarmSetting")
    @Operation(summary = "알림 설정 조회", description = "사용자의 알림 관련 설정들을 조회합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AlarmSettingResponseDto.InfoDto.class)))
    public AlarmSettingResponseDto.InfoDto getAlarmSettingInfo() {
        return alarmSettingService.getAlarmSettingInfo();
    }

    @PatchMapping("/alarmSetting")
    @Operation(summary = "알림 설정 변경", description = "사용자의 알림 관련 설정을 변경합니다. 변경할 필드와 데이터만 넣으면 됩니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = AlarmSettingResponseDto.InfoDto.class)))
    public AlarmSettingResponseDto.InfoDto editAlarmSettingInfo(
            @RequestBody AlarmSettingRequestDto.EditDto requestDto
    ) {
        return alarmSettingService.editAlarmSettingInfo(requestDto);
    }
}
