package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "alarm_setting")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmSetting extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Setter
    @Column(name ="reminder_enabled")
    private boolean reminderEnabled;

    @Setter
    @Column(name = "fire_enabled")
    private boolean fireEnabled;

    @Setter
    @Column(name = "reminder_time")
    private LocalTime reminderTime;

    @Setter
    @Column(name = "all_off_enabled")
    private boolean allOffEnabled;

}
