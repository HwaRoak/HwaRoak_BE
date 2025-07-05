package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import com.umc.hwaroak.domain.common.FriendStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "friend")
public class Friend extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "friend_status")
    private FriendStatus status;
}
