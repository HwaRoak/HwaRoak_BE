package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emotion_summary")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmotionSummary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "year_month", nullable = false)
    private String yearMonth; // "2025-07" 형식

    @Column(name = "diary_count", nullable = false)
    private int diaryCount=0;

    @Column(name = "calm_count", nullable = false)
    private int calmCount=0;

    @Column(name = "happy_count", nullable = false)
    private int happyCount=0;

    @Column(name = "sad_count", nullable = false)
    private int sadCount=0;

    @Column(name = "angry_count", nullable = false)
    private int angryCount=0;

    @Column(name = "summary_message", columnDefinition = "TEXT")
    private String summaryMessage;

    public void updateCounts(int diaryCount, int calm, int happy, int sad, int angry, String message) {
        this.diaryCount = diaryCount;
        this.calmCount = calm;
        this.happyCount = happy;
        this.sadCount = sad;
        this.angryCount = angry;
        this.summaryMessage = message;
    }
}
