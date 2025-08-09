package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "question")
public class Question extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="question_id")
    private Long id;

    @Column(name = "content")
    private String content;

    @Column(name = "tag")
    private String tag;  // 예: "REWARD", "EMOTION_JOY"
}
