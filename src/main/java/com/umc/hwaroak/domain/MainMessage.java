package com.umc.hwaroak.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "main_message")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MainMessage {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private MainMessageType type;

    private int itemLevel; // REWARD_BY_LEVEL에서 사용

    private String content;
}
