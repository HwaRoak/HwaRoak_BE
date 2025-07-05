package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "member_answer")
public class MemberAnswer extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_answer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;
}
