package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import com.umc.hwaroak.domain.common.Emotion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "diary")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Diary extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diary_id", nullable = false)
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "emotion")
    private Emotion emotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Setter
    @Column(name = "feedback")
    private String feedback;

    public void update(String content, Emotion emotion) {
        if (content != null) {
            this.content = content;
        }
        if (emotion != null) {
            this.emotion = emotion;
        }
    }
}
