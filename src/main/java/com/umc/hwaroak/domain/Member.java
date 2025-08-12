package com.umc.hwaroak.domain;

import com.umc.hwaroak.domain.common.BaseEntity;
import com.umc.hwaroak.domain.common.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Table(name = "member")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "name")
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Setter
    @Column(name = "reward")
    private Integer reward;

    @Column(name = "feeling")
    private String feeling;

    // 프로필 이미지 추가
    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "introduction")
    private String introduction;

    // 역할 필드 추가
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member")
    private List<Diary> diaryList = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private AlarmSetting alarmSetting;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alarm> receivedAlarmList = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alarm> sendAlarmList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberItem> itemList = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friend> receivedFriend = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friend> sendFriend = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberItem> memberItemList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmotionSummary> emotionSummaryList = new ArrayList<>();

    public void update(String nickname, String introduction){
        if (nickname != null) {
            this.nickname = nickname;
        }

        if (introduction != null) {
            this.introduction = introduction;
        }
    }
  
    // 로그인 시 사용
    public Member( String userId, String nickname, String profileImage) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.role = Role.USER;
        this.reward = 7;
        this.feeling = "default";
    }

}
