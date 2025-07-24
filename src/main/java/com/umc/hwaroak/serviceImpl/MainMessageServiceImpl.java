package com.umc.hwaroak.serviceImpl;

import com.umc.hwaroak.authentication.MemberLoader;
import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.Member;
import com.umc.hwaroak.domain.common.AlarmType;
import com.umc.hwaroak.dto.response.MainMessageResponseDto;
import com.umc.hwaroak.repository.AlarmRepository;
import com.umc.hwaroak.repository.DiaryRepository;
import com.umc.hwaroak.repository.MainMessageRepository;
import com.umc.hwaroak.service.MainMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static com.umc.hwaroak.domain.common.MainMessageType.*;

@RequiredArgsConstructor
@Service
public class MainMessageServiceImpl implements MainMessageService {

    private final MainMessageRepository mainMessageRepository;
    private final DiaryRepository diaryRepository;
    private final AlarmRepository alarmRepository;
    private final MemberLoader memberLoader;

    // 일기
   // private boolean isRewardAvailable(Member member) {
        //return member.getReward()==0;
   // }


    /*private String getRewardMessage(Member member) {
        if (!member.isRewardReceived()) {
            return "보상을 받아봐!";
        }
        int level = member.리워드하려는 아이템의 멘트가져와야함(); -> memberService.findSelectedItem()
        return mainMessageRepository.findByTypeAndItemLevel(REWARD_BY_LEVEL, level)
                .map(MainMessage::getContent)
                .orElse("보상을 축하해!"); // fallback
    } */

    /**
     * 사용자가 받은 불씨 알람중 읽지 않은 것이 있나?
     */
    private boolean hasUnreadFireAlarm(Member member) {
        return alarmRepository.existsByReceiverAndAlarmTypeAndReadFalse(
                member, AlarmType.FIRE
        );
    }

    /**
     * 준비된 불씨 친추멘트 반환 -> 근데 멘트 예시중에 "그 사이 친구들도 다양한 날들을 보냈대. 구경 가볼래?” 이런건 괜찮은데
     * "00님의 요즘 감정이 조금 바뀐 것 같아. 어떤 하루를 보냈을까?" 여기서 @@님은 어쩔까~~요.
     */
    private MainMessageResponseDto getFireMessage() {
        return mainMessageRepository.findRandomByType(FIRE_ALERT)
                .map(m -> MainMessageResponseDto.of(m.getContent()))
                .orElse(MainMessageResponseDto.of("🔥 친구가 응원했어요!")); // fallback
    }

    /**
     * 사용자가 오늘 일기를 썼는가?!
     */
    private boolean hasWrittenTodayDiary(Member member) {
        return diaryRepository.existsByMemberIdAndRecordDate(member.getId(), LocalDate.now());
    }

    /**
     * 일기 안쓴 경우의 원래 디폴트 값들을 랜덤으로 반환
     */
    private MainMessageResponseDto getDiaryPromptMessage() {
        return mainMessageRepository.findRandomByType(DIARY_EMPTY)
                .map(m -> MainMessageResponseDto.of(m.getContent()))
                .orElse(MainMessageResponseDto.of("오늘 하루를 기록해보세요!")); // fallback
    }

    /**
     *  일기 쓴 경우의 Diary의 feedback(감정 분석 된 내용)을 반환함.
     */
    private MainMessageResponseDto getEmotionFeedbackMessage(Member member) {
        return diaryRepository.findByRecordDate(member.getId(), LocalDate.now())
                .map(Diary::getFeedback)
                .map(MainMessageResponseDto::of)
                .orElse(MainMessageResponseDto.of("오늘은 어떤 하루였나요?"));
    }

    /**
     * 사실상 이게 메인 로직. 조건이 복잡하지만 많진 않아서 if문 위치로 우선순위를 정하였습니다.
     * 1. 리워드 수령 가능여부
     * 2. 친구의 불씨 알람 도착 여부-> 정확히는 불씨 알람중 읽지 않은 것이 있나?
     * 3. 오늘의 일기 미작성? -> 오늘은 어떤 하루였어~? 등의 일기 작성하라고 재촉하는 느낌의 멘트 랜덤 반환
     * 4. 일기 작성 -> 리워드 수령할 수 있는 상황 아니고? 불씨 알람 다 읽었거나 없고? 일기 썻으면? 일기에 대한 피드백이 디폴트 메시지가 됩니다.
     */
    public MainMessageResponseDto getMainMessage() {

        Member member = memberLoader.getMemberByContextHolder();

       // if (isRewardAvailable(member)) {
           // return getRewardMessage(member);
        //}

        if (hasUnreadFireAlarm(member)) {
            return getFireMessage();
        }

        if (!hasWrittenTodayDiary(member)) {
            return getDiaryPromptMessage();
        }

        return getEmotionFeedbackMessage(member);
    }



}
