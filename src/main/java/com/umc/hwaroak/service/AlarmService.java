package com.umc.hwaroak.service;

import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.dto.request.AlarmRequestDto;
import com.umc.hwaroak.dto.response.AlarmResponseDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlarmService {
    /**
     * 공지 최신 순으로 가져오기
     */
    List<AlarmResponseDto.PreviewDto> getNoticeList();

    /**
     * 공지 상세보기
     */
    AlarmResponseDto.InfoDto getNoticeDetail(Long id);

    /**
     *  친구 요청 알람 보내기
     */
    void sendFriendRequestAlarm(Member sender, Member receiver);

    /**
     * 로그인한 유저의 알림함 전체를 최신순으로 조회
     */
    List<AlarmResponseDto.InfoDto> getAllAlarmsForMember(Member member);

    /**

     *  불씨 보냈을시 알람 생성하기
     */
    void sendFireAlarm(Member sender, Member receiver);
    Optional<LocalDateTime> getLastFireTime(Member sender, Member receiver);


     *  알람 읽음 처리 하기
     */
    void markAsRead(Long alarmId, Member member);

     *  공지 수동 등록
     */
    void createNotice(AlarmRequestDto.CreateNoticeDto requestDto);
}
