package com.umc.hwaroak.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiUtil {

    private final ChatModel chatModel;

    public String reviewDiary(String diary) {

        String systemPrompt = """
        너는 일기에 대하여 공감하고 위로해주는 AI다.
        너는 항상 유쾌하고 친구같은 말투로 대답한다. 100byte 내외로 답한다.
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
}
