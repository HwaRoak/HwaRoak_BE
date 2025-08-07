package com.umc.hwaroak.serviceImpl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.converter.AlarmConverter;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.dto.request.AlarmRequestDto;
import com.umc.hwaroak.dto.response.AlarmResponseDto;
import com.umc.hwaroak.event.CustomTransactionSynchronization;
import com.umc.hwaroak.event.RedisPublisher;
import com.umc.hwaroak.exception.GeneralException;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.response.ErrorCode;
import com.umc.hwaroak.service.AlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
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


    private final JPAQueryFactory queryFactory;  // 주입!

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
                .message(sender.getNickname() + "님이 친구 요청을 보냈습니다.")
                .content(sender.getNickname() + "님이 친구 요청을 보냈습니다.")
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

    /**
     * 알람함 최신순 전체 조회
     * receiverId로 조회 or (receiverId=NULL && 공지)
     */
    @Override
    public List<AlarmResponseDto.InfoDto> getAllAlarmsForMember() {
        Member receiver = memberLoader.getMemberByContextHolder();
        List<Alarm> alarms = alarmRepository.findAllIncludingGlobalAlarms(receiver);

        return alarms.stream()
                .map(alarm -> AlarmResponseDto.InfoDto.builder()
                        .id(alarm.getId())
                        .title(alarm.getTitle())
                        .content(alarm.getContent())
                        .alarmType(alarm.getAlarmType())
                        .isRead(alarm.isRead())
                        .createdAt(alarm.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     *  불씨 보냈을시 알람 생성하기
     */
    @Override
    public void sendFireAlarm(Member sender, Member receiver) {
        String nickname = sender.getNickname();

        Alarm alarm = Alarm.builder()
                .sender(sender)
                .receiver(receiver)
                .alarmType(AlarmType.FIRE)
                .title("불 키우기")
                .message(nickname + "님께서 불씨를 지폈어요!")
                .content(nickname + "님께서 불씨를 지폈어요!")
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
        Alarm alarm = Alarm.builder()
                .alarmType(AlarmType.NOTIFICATION)
//                .receiver(null)
//                .sender(null) // 필요 시 admin 계정 넣을 수도 있음
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

    @Scheduled(cron = "0 0 0 1 * *") // 매달 1일 00:00
    @Transactional
    public void createMonthlyDailyAlarm() {
        // 전달 월 구하기 (ex. 7월 1일이면 6월)
        LocalDate today = LocalDate.now();
        Month lastMonth = today.minusMonths(1).getMonth();

        // 랜덤 content 생성
        String content1 = "리포트 반영 완료! 내 감정을 돌아볼까요?";
        String content2 = String.format("리포트 도착! %d월의 화록은 어땠을까요?", lastMonth.getValue());
        String content = Math.random() < 0.5 ? content1 : content2;

        Alarm alarm = Alarm.builder()
                .alarmType(AlarmType.DAILY)
                .title("감정 리포트가 반영됐어요!")
                .content(content)
                .message("한 달 동안의 내 감정을 돌아볼 시간이에요.")
                .receiver(null)
                .sender(null)
                .isRead(false)
                .build();

        alarmRepository.save(alarm);
    }


}
