package my.mma.global.fcm.dto;

import my.mma.global.entity.TargetType;

public record FcmMessageRequest(String token, TargetType targetType, int targetId) {
}
