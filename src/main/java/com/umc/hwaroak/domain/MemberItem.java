package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import com.umc.hwaroak.domain.common.Item;
import jakarta.persistence.*;

@Entity
@Table(name = "member_item")
public class MemberItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "item")
    private Item item;
}
