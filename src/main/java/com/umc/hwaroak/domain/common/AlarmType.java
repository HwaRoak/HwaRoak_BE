package com.umc.hwaroak.domain.common;

import lombok.Getter;

@Getter
public enum AlarmType {
    FRIEND_REQUEST("friendRequest"),
    REMINDER("reminder"),
    FIRE("fire"),
    NOTIFICATION("notification"),
    CONNECTED("connected"),
    DAILY("daily"),
    HEARTBEAT("heartbeat");
    ;

    private final String value;

    AlarmType(String value) {
        this.value = value;
    }
}
