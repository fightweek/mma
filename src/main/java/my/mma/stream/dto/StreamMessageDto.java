package my.mma.stream.dto;

import lombok.*;
import my.mma.fightevent.dto.StreamFightEventDto;
import my.mma.stream.dto.ChatMessageDto.ChatJoinRequest;
import my.mma.stream.dto.ChatMessageDto.ChatMessageRequest;
import my.mma.stream.dto.ChatMessageDto.ChatMessageResponse;

public class StreamMessageDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StreamMessageRequest{
        private RequestMessageType requestMessageType;
        private ChatJoinRequest chatJoinRequest;
        private ChatMessageRequest chatMessageRequest;
        private StreamFightEventDto streamFightEvent;
        private Long userIdToBlock;
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
        JOIN,TALK,BLOCK
    }

    public enum ResponseMessageType {
        TALK,CONNECTION_COUNT,FIGHT,
    }


}
