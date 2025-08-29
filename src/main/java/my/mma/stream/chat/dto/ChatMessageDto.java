package my.mma.stream.chat.dto;

import lombok.*;


public class ChatMessageDto {

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChatJoinRequest {
        private long userId;
        private String nickname;
    }

    @Builder
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChatMessageRequest {
        private String message;
        private int point;
    }

    @Builder
    @Getter
    @Setter
    public static class ChatMessageResponse{
        private String message;
        private String nickname;
        private long userId;
        private int point;
    }

}