package com.umc.hwaroak.event;

import org.springframework.context.ApplicationEvent;

public class ItemUpdateEvent extends ApplicationEvent {
    private Long memberId;

    public ItemUpdateEvent(Object source, Long memberId) {
        super(source);
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }
}
