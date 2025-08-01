package com.umc.hwaroak.util;

import com.umc.hwaroak.domain.common.AlarmType;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@EqualsAndHashCode
public class SseRepositoryKeyGenerator {
    private static final String UNDER_SCORE = "_";

    private final Long memberId;
    private final AlarmType sseEventName;
    private final LocalDateTime createdAt;

    /**
     * SSEInMemoryRepository에서 사용될
     * 특정 member에 대한 특정 브라우저,특정 SSEEventName에
     * 대한 SSEEmitter를 찾기 위한 key를 생성한다.
     * @return
     */
    public String toCompleteKeyWhichSpecifyOnlyOneValue() {

        String createdAtString = createdAt == null ? "" : createdAt.toString();
        return memberId + UNDER_SCORE + sseEventName.getValue() + UNDER_SCORE + createdAtString;
    }
}
