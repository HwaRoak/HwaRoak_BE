package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.dto.request.AlarmRequestDto;
import com.umc.hwaroak.dto.response.AlarmResponseDto;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.AlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {

    private final MemberLoader memberLoader;
    private final AlarmRepository alarmRepository;

    /**
     * 공지(NOTIFIACTION) 최신순 정렬 가져오기
     */
    @Override
    public List<AlarmResponseDto.PreviewDto> getNoticeList() {

        memberLoader.getMemberByContextHolder();

        return alarmRepository.findByAlarmTypeOrderByCreatedAtDesc(AlarmType.NOTIFICATION).stream()
                .map(alarm -> AlarmResponseDto.PreviewDto.builder()
                        .id(alarm.getId())
                        .title(alarm.getTitle())
                        .createdAt(alarm.getCreatedAt())
                        .build())
                .collect(toList());
    }

    /**
     * 공지 id로 상세조회하기
     */
    @Override
    public AlarmResponseDto.InfoDto getNoticeDetail(Long id) {

        memberLoader.getMemberByContextHolder();

        Alarm alarm = alarmRepository.findByIdAndAlarmType(id, AlarmType.NOTIFICATION)
                .orElseThrow(() -> new GeneralException(ErrorCode.NOTICE_NOT_FOUND));

        return AlarmResponseDto.InfoDto.builder()
                .id(alarm.getId())
                .title(alarm.getTitle())
                .content(alarm.getContent())
                .createdAt(alarm.getCreatedAt())
                .build();
    }

    /**
     *  친구 요청시 알람 생성하기
     */
    @Override
    public void sendFriendRequestAlarm(Member sender, Member receiver) {
        String nickname = sender.getNickname();

        Alarm alarm = Alarm.builder()
                .sender(sender)
                .receiver(receiver)
                .alarmType(AlarmType.FRIEND_REQUEST)
                .title("친구 요청")
                .content(nickname + "님이 친구 요청을 보냈습니다.")
                .build();

        alarmRepository.save(alarm);
    }

    @Override
    public List<AlarmResponseDto.InfoDto> getAllAlarmsForMember(Member receiver) {
        List<Alarm> alarms = alarmRepository.findAllByReceiverOrderByCreatedAtDesc(receiver);

        return alarms.stream()
                .map(alarm -> AlarmResponseDto.InfoDto.builder()
                        .id(alarm.getId())
                        .title(alarm.getTitle())
                        .content(alarm.getContent())
                        .alarmType(alarm.getAlarmType())
                        .createdAt(alarm.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     *  공지 수동 등록
     */
    @Transactional
    public void createNotice(AlarmRequestDto.CreateNoticeDto requestDto) {
        Alarm alarm = Alarm.builder()
                .alarmType(AlarmType.NOTIFICATION)
                .receiver(null)
                .sender(null) // 필요 시 admin 계정 넣을 수도 있음
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .message(requestDto.getMessage())
                .isRead(false)
                .build();

        alarmRepository.save(alarm);
    }

}
