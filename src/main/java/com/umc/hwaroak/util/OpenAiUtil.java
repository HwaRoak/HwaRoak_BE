package com.umc.hwaroak.util;

import com.umc.hwaroak.domain.Diary;
import com.umc.hwaroak.domain.common.EmotionCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiUtil {

    private final ChatModel chatModel;

    public String reviewDiary(String diary) {

        String systemPrompt = """
        너는 일기에 대하여 공감하고 위로해주는 AI다.
        너는 항상 유쾌하고 친구같은 말투로 대답한다. 80byte 내외로 답한다.
        텍스트 이모티콘은 Kaomoji다. 문장의 끝에 항상 텍스트 이모티콘을 사용한다.
        
        예시1. 오늘은 참 재미있는 일이 있었네! >ㅁ<
        예시2. 오늘은 속상한 일이 있었구나ㅜ.ㅜ 너무 속생해 할 필요 없어!
        예시3. 그거 참 화난다! (｀Д´)
        """;
        SystemMessage systemMessage = new SystemMessage(systemPrompt);

        UserMessage userMessage = new UserMessage(diary);
        String result = chatModel.call(systemMessage, userMessage);
        log.info("Response from Open AI: " + result);
        return result;
    }

    // ai 기반 감정분석 멘트 생성
    public String analysisEmotions(int month, Map<EmotionCategory, Integer> emotionCounts, List<Diary> diaries){
        String systemPrompt = """
            너는 감정 통계를 바탕으로 그 달을 정리해주는 친구 같은 AI야.
            
            규칙:
            1. 입력으로 특정 달과, 감정 통계, 일기 내용 리스트를 줄 거야.
            2. 감정 통계는 감정 카테고리와 그에 대응하는 개수가 포함돼. 감정 카테고리는 [평온, 행복, 우울, 분노] 이렇게 4개가 있어.
            3. 특정 달에 작성한 일기들에 감정들이 포함되어 있는데, 이걸 집계한 게 감정 통계야
            4. 응답은 자연스러운 일기 또는 짧은 회고처럼 작성해.
            5. 응답의 처음을 분석 대상이 되는 달을 언급하며 시작해. 예를 들어, 7월일 경우 "7월달에는..."으로 시작하는 거야.
            6. 그 뒤에는 가장 높은 감정 카테고리를 언급해. 예를 들어, "평온의 감정이 제일 많았어요!"
            7. 그리고 그 뒤에 회고나 느낌을 적는 거야. 4~5줄 정도로. 감정 통계와 일기 내용을 기반으로.
            8. 이모티콘은 생략해.
        
            응답 예시:
            7월달에는 우울의 감정이 제일 많았어요! 무기력한 날도, 숙제 때문에 피곤했던 날도 있었지만, 이번 달도 무사히 지나가네요!
            """;

        // 감정 통계 문자열 변환
        StringBuilder emotionSB = new StringBuilder();
        for (Map.Entry<EmotionCategory, Integer> entry : emotionCounts.entrySet()) {
            emotionSB.append(entry.getKey().name()).append("=").append(entry.getValue()).append("개, ");
        }
        String emotionCountsStr = emotionSB.substring(0, emotionSB.length() - 2);

        // 일기 내용 추출
        StringBuilder diaryContentSB = new StringBuilder();
        for (Diary diary : diaries) {
            if (diary.getContent() != null && !diary.getContent().isBlank()) {
                diaryContentSB.append("- ").append(diary.getContent()).append("\n");
            }
        }
        String diaryContentsStr = diaryContentSB.toString();

        // 입력 메시지 구성
        String inputMessage = String.format("""
            달: %s
            감정 통계: [%s]
            일기 내용:
            %s
            """, month, emotionCountsStr, diaryContentsStr);


        // 응답 생성
        SystemMessage systemMessage = new SystemMessage(systemPrompt);
        UserMessage userMessage = new UserMessage(inputMessage);

        String result = chatModel.call(systemMessage, userMessage);
        log.info("감정 회고 생성 결과: {}", result);
        return result;
    }
}
