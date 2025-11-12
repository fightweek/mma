package my.mma.global.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePreferenceRequest(@NotNull Long targetId, @NotNull Boolean on) {

}

