package com.umc.hwaroak.listener;

import com.umc.hwaroak.converter.AlarmConverter;
import com.umc.hwaroak.domain.Alarm;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.event.FireSendEvent;
import com.umc.hwaroak.infrastructure.publisher.RedisPublisher;
import com.umc.hwaroak.infrastructure.transaction.CustomTransactionSynchronization;
import com.umc.hwaroak.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class FireEventListener{

    private final AlarmRepository alarmRepository;
    private final RedisPublisher redisPublisher;

    /**
     *  불씨 보냈을시 알람 생성하기
     */
    @EventListener
    @Transactional
    public void sendFireAlarm(FireSendEvent event) {
        String nickname = event.getSender().getNickname();

        Alarm alarm = Alarm.builder()
                .sender(event.getSender())
                .receiver(event.getReceiver())
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
}
