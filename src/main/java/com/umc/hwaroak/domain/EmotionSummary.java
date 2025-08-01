package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "emotion_summary",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_month", columnNames = {"member_id", "summary_month"})
        })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmotionSummary extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "summary_month", nullable = false)
    private String summaryMonth; // "2025-07" 형식

    @Column(name = "diary_count")
    private int diaryCount=0;

    @Column(name = "calm_count")
    private int calmCount=0;

    @Column(name = "happy_count")
    private int happyCount=0;

    @Column(name = "sad_count")
    private int sadCount=0;

    @Column(name = "angry_count")
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
