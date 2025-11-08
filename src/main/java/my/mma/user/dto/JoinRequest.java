package my.mma.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinRequest(
        @NotBlank @Email
        String email,
        @NotBlank @Size(min = 2, max = 12) String nickname,
        @NotBlank @Size(min = 6, max = 20) String password) {
}
