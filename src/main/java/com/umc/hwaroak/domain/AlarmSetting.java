package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarm_setting")
public class AlarmSetting extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name ="reminder_enabled")
    private boolean reminderEnabled;

    @Column(name = "fire_enabled")
    private boolean fireEnabled;

    @Column(name = "reminder_time")
    private LocalDateTime reminderTime;
}
