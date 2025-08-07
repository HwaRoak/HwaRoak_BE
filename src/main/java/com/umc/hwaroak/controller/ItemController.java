package com.umc.hwaroak.controller;

import com.umc.hwaroak.dto.response.ItemResponseDto;
import com.umc.hwaroak.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "아이템 API Controller", description = "아이템 관련 요청을 위한 API Controller입니다.")
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "수령한 아이템 전체 조회 API", description = """
            수령한 아이템들이 무어잇이 있는지를 반환합니다.<br>
            보상받기 처리를 한 아이템들만 반환됩니다.
            """)
    @GetMapping("")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ItemResponseDto.ItemDto.class)))
    public List<ItemResponseDto.ItemDto> getMyItems(){
        return itemService.getMyItems();
    }


    @Operation(summary = "아이템 보상받기 API", description = """
            아이템 보상을 받기 위한 API입니다.<br>
            새로운 아이템을 보상받으면, 현재 대표 아이템도 변경됩니다.
            """)
    @PatchMapping("")
    public ItemResponseDto.ItemDto receiveItems() {

        return itemService.receiveItem();
    }

    @GetMapping("/selected")
    @Operation(summary = "대표 아이템 조회", description = "사용자의 대표 아이템을 조회합니다.")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ItemResponseDto.ItemDto.class)))
    public ItemResponseDto.ItemDto getMySelectedItem(){
        return itemService.findSelectedItem();
    }

    @PatchMapping("/{itemId}/selected")
    @Operation(summary = "대표 아이템 변경", description = """
    대표 아이템을 선택한 아이템으로 변경합니다.<br>
    조회에 사용된 itemId를 사용해주세요.
    """)
    @ApiResponse(content = @Content(schema = @Schema(implementation = ItemResponseDto.ItemDto.class)))
    public ItemResponseDto.ItemDto changeSelectedItem(
            @Schema(description = "변경하려는 아이템의 id", example = "1")
            @PathVariable Long itemId
    ){
        return itemService.changeSelectedItem(itemId);
    }
}
