package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import com.umc.hwaroak.domain.common.MainMessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "main_message")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MainMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MainMessageType type;

    @Column(name = "item_level")
    private int itemLevel;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}
