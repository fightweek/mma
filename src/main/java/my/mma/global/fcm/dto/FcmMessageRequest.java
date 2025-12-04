package my.mma.global.fcm.dto;

import my.mma.alert.constant.AlertTarget;

public record FcmMessageRequest(String token, AlertTarget targetType, int targetId) {
}
