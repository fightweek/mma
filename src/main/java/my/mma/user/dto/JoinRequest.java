package my.mma.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(@NotBlank String email,
                          @NotBlank @Size(min = 2, max = 10) String nickname,
                          @NotBlank @Size(min = 6, max = 20) String password) {
}
