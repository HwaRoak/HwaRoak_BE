package com.umc.hwaroak.event.listener;

import com.umc.hwaroak.converter.AlarmConverter;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.event.FriendRequestEvent;
import com.umc.hwaroak.infrastructure.publisher.RedisPublisher;
import com.umc.hwaroak.infrastructure.transaction.CustomTransactionSynchronization;
import com.umc.hwaroak.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RequiredArgsConstructor
@Component
public class FriendEventListener {

    private final AlarmRepository alarmRepository;
    private final RedisPublisher redisPublisher;

    /**
     *  친구 요청시 알람 생성하기
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendFriendRequestAlarm(FriendRequestEvent event) {

        Alarm alarm = Alarm.builder()
                .sender(event.getSender())
                .receiver(event.getReceiver())
                .alarmType(AlarmType.FRIEND_REQUEST)
                .title("친구 요청")
                .message(event.getSender().getNickname() + "님이 친구 요청을 보냈습니다.")
                .content(event.getSender().getNickname() + "님이 친구 요청을 보냈습니다.")
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
}
