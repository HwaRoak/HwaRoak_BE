package com.umc.hwaroak.event;

import com.umc.hwaroak.domain.Member;
import org.springframework.context.ApplicationEvent;

public class FireSendEvent extends ApplicationEvent {

    private Member sender;
    private Member receiver;

    public FireSendEvent(Object source, Member sender, Member receiver) {
        super(source);
        this.sender = sender;
        this.receiver = receiver;
    }

    public Member getSender() {
        return sender;
    }

    public Member getReceiver() {
        return receiver;
    }
}
