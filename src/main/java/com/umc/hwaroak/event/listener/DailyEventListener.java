package com.umc.hwaroak.event.listener;

import com.umc.hwaroak.event.DailyReminderEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

public class DailyEventListener {

    @TransactionalEventListener
    @Async
    public void handleDailyAlarm(DailyReminderEvent event) {

    }
}
