package com.umc.hwaroak.event;

import org.springframework.context.ApplicationEvent;

public class ItemRollbackEvent extends ApplicationEvent {

    private Long memberId;

    public ItemRollbackEvent(Object source, Long memberId) {
        super(source);
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }
}
