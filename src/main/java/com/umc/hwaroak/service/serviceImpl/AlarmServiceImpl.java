package com.umc.hwaroak.service.serviceImpl;

import com.umc.hwaroak.infrastructure.authentication.MemberLoader;
import com.umc.hwaroak.converter.AlarmConverter;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.domain.common.Role;
import com.umc.hwaroak.dto.request.AlarmRequestDto;
import com.umc.hwaroak.dto.response.AlarmResponseDto;
import com.umc.hwaroak.infrastructure.transaction.CustomTransactionSynchronization;
import com.umc.hwaroak.infrastructure.publisher.RedisPublisher;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private final MemberLoader memberLoader;
    private final AlarmRepository alarmRepository;

    private final RedisPublisher redisPublisher;

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
                .orElseThrow(() -> new GeneralException(ErrorCode.ALARM_NOT_FOUND));

        return AlarmResponseDto.InfoDto.builder()
                .id(alarm.getId())
                .title(alarm.getTitle())
                .content(alarm.getContent())
                .isRead(alarm.isRead())
                .createdAt(alarm.getCreatedAt())
                .build();
    }


    /**
     * 알람함 최신순 전체 조회
     * receiverId로 조회 or (receiverId=NULL && 공지)
     */
    @Transactional(readOnly = true)
    @Override
    public List<AlarmResponseDto.InfoDto> getAllAlarmsForMember() {
        Member receiver = memberLoader.getMemberByContextHolder();
        List<Alarm> alarms = alarmRepository.findAllIncludingGlobalAlarms(receiver);

        // sender가 존재하면 sender의 UserId 가져옵니다. 아니면 널~
        return alarms.stream()
                .map(alarm -> {
                    String userId = (alarm.getAlarmType() == AlarmType.FIRE || alarm.getAlarmType() == AlarmType.FRIEND_REQUEST)
                            ? (alarm.getSender() != null ? alarm.getSender().getUserId() : null)
                            : "";

                    return AlarmResponseDto.InfoDto.builder()
                            .id(alarm.getId())
                            .title(alarm.getTitle())
                            .content(alarm.getContent())
                            .alarmType(alarm.getAlarmType())
                            .isRead(alarm.isRead())
                            .createdAt(alarm.getCreatedAt())
                            .userId(userId)
                            .build();
                })
                .toList();
    }

     /*  알람 읽기 api
     */
    @Transactional
    public void markAsRead(Long alarmId) {
        Member member = memberLoader.getMemberByContextHolder();

        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new GeneralException(ErrorCode.ALARM_NOT_FOUND));

        // alarm.getReceiver() != null -> 공지는 receiverId NULL이기 때문에 필수!
        if (alarm.getReceiver() != null && !alarm.getReceiver().getId().equals(member.getId())) {
            throw new GeneralException(ErrorCode.FORBIDDEN_ALARM_ACCESS);
        }

        alarm.markAsRead(); // 엔티티 메서드
    }

     /**
     *  공지 수동 등록 - 관리자용
     */
    @Transactional
    public void createNotice(AlarmRequestDto.CreateNoticeDto requestDto) {

        Member member = memberLoader.getMemberByContextHolder(); // 현재 로그인한 사용자
        if (member.getRole() != Role.ADMIN) {
            throw new GeneralException(ErrorCode.ADMIN_ACCESS_ONLY);
        }
        Alarm alarm = Alarm.builder()
                .alarmType(AlarmType.NOTIFICATION)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .message(requestDto.getMessage())
                .isRead(false)
                .build();

        alarmRepository.save(alarm);
        TransactionSynchronizationManager.registerSynchronization(
                new CustomTransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        redisPublisher.publish(alarm.getAlarmType().getValue(), AlarmConverter.toPreviewDto(alarm));
                    }
                }
        );
    }

    // 마지막 불씨 보낸 시각
    @Override
    public Optional<LocalDateTime> getLastFireTime(Member sender, Member receiver){
        List<Alarm> alarms = alarmRepository.findBySenderAndReceiverAndAlarmTypeOrderByCreatedAtDesc(
                sender, receiver, AlarmType.FIRE
        );

        if (alarms.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(alarms.get(0).getCreatedAt()); // 또는 getFiredAt()
    }
}
