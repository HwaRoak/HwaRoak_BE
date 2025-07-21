package com.umc.hwaroak.domain;

import com.umc.hwaroak.converter.EmotionListConverter;
import com.umc.hwaroak.domain.common.BaseEntity;
import com.umc.hwaroak.domain.common.Emotion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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

    @Lob
    @Convert(converter = EmotionListConverter.class)
    @Column(name = "emotion", columnDefinition = "TEXT")
    private List<Emotion> emotionList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Setter
    @Column(name = "feedback")
    private String feedback;

    public void update(String content, List<Emotion> emotionList) {
        if (content != null) {
            this.content = content;
        }
        if (emotionList.stream() != null) {
            this.emotionList = emotionList;
        }
    }
}
