package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_item")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberItem extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Setter
    @Column(name = "is_selected")
    private Boolean isSelected;     // 대표 아이템 여부
}
