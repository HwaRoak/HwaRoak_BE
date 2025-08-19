package com.umc.hwaroak.event;

import org.springframework.context.ApplicationEvent;

import java.time.Clock;

public class DailyReminderEvent extends ApplicationEvent {

    public DailyReminderEvent(Object source) {
        super(source);
    }

    public DailyReminderEvent(Object source, Clock clock) {
        super(source, clock);
    }
}
