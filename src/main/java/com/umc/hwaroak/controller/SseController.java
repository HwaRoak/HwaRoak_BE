package com.umc.hwaroak.controller;

import com.umc.hwaroak.service.EmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/sse")
@Tag(name = "SSE 연결 API Controller", description = "SSE 연결을 위한 API입니다.")
@RequiredArgsConstructor
public class SseController {

    private final EmitterService emitterService;

    @GetMapping(value = "/connect", produces = "text/event-stream")
    @Operation(summary = "SSE 연결 API",
            description = """
           SSE 연결을 위한 최초의 url입니다.<br>
           Header에는 Authorization으로 토큰만 넣어주시면 됩니다.<br>
           서버에서 설정된 시간이 만료되지 않는 이상 생성된 event는 만료되지 않습니다.
           """)
    public SseEmitter subscribe(
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId
    ) {
        return emitterService.subscribe(lastEventId);
    }
}
