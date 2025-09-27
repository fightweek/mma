package my.mma.smtp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

public record VerifyCodeRequest(@NotBlank String email,
                                @NotBlank @Size(min = 6, max = 6) @JsonProperty("verifyingCode") String code
) {}