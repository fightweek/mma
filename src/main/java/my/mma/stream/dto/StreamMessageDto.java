package my.mma.stream.dto;

import lombok.*;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.stream.chat.dto.ChatMessageDto.ChatMessageRequest;
import my.mma.stream.chat.dto.ChatMessageDto.ChatMessageResponse;

public class StreamMessageDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StreamMessageRequest{
        private RequestMessageType requestMessageType;
        private ChatMessageRequest chatMessageRequest;
        private StreamFightEventDto streamFightEvent;
        private int connectionCount;
    }

    @Getter
    @Setter
    @Builder
    public static class StreamMessageResponse{
        private ResponseMessageType responseMessageType;
        private ChatMessageResponse chatMessageResponse;
        private StreamFightEventDto streamFightEvent;
        private int connectionCount;

    }

    public enum RequestMessageType {
        JOIN,TALK,BET
    }

    public enum ResponseMessageType {
        TALK,CONNECTION_COUNT,FIGHT,
    }


}
