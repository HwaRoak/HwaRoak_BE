package com.umc.hwaroak.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.hwaroak.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@Table(name = "member")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "name")
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "reward")
    private int reward;

    @Column(name = "feeling")
    private String feeling;

    // 생일 추가
    @Column(name = "birthday")
    private String birthday;

    // 프로필 이미지 추가
    @Column(name = "profile_image")
    private String profileImage;

    @OneToMany(mappedBy = "member")
    @JsonIgnore //테스트용
    private List<Diary> diaryList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore //테스트용
    private List<MemberAnswer> memberAnswerList = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore //테스트용
    private AlarmSetting alarmSetting;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore //테스트용
    private List<Alarm> receivedAlarmList = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore //테스트용
    private List<Alarm> sendAlarmList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore //테스트용
    private List<MemberItem> itemList = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore //테스트용
    private List<Friend> receivedFriend = new ArrayList<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore //테스트용
    private List<Friend> sendFriend = new ArrayList<>();

    // 로그인 시 사용
    public Member(String userId, String email, String name, String nickname, String birthday, String profileImage) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.birthday = birthday;
        this.profileImage = profileImage;
        this.reward = 0;
        this.feeling = "default";
    }

}
