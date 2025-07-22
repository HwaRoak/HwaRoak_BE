package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "alarm")
public class Alarm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "alarm_type")
    private AlarmType alarmType;

    @Column(name = "message")
    private String message;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    // 알림 읽었음 추가
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    // 알람 발송시각 분초까지 추가
    @Column(name = "fired_at")
    private LocalDateTime fired_at;
}
