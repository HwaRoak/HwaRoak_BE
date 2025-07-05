package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import com.umc.hwaroak.domain.common.Emotion;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "diary")
public class Diary extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id", nullable = false)
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion")
    private Emotion emotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}
