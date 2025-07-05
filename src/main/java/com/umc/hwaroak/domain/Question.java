package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
public class Question extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="question_id")
    private Long id;

    @Column(name = "content")
    private String content;

    @OneToMany(mappedBy = "question")
    private List<MemberAnswer> memberAnswerList = new ArrayList<>();
}
