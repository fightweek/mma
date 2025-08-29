package my.mma.stream.dto;

import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserChatLog {

    private String nickname;
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @Builder
    public static class ChatMessage {
        private String message;
        private LocalTime time;
    }

    public void addMessage(ChatMessage message){
        this.messages.add(message);
    }

}
