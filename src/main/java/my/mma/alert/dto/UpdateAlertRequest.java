package my.mma.alert.dto;

import jakarta.validation.constraints.NotNull;
import my.mma.alert.constant.AlertTarget;
import my.mma.alert.entity.Alert;
import my.mma.user.entity.User;

public record UpdateAlertRequest(@NotNull Long targetId, @NotNull Boolean on, @NotNull AlertTarget alertTarget) {
    public Alert toEntity(User user){
        return Alert.builder()
                .user(user)
                .targetId(targetId)
                .alertTarget(alertTarget)
                .build();
    }
}