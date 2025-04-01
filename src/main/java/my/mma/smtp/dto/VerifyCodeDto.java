package my.mma.smtp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class VerifyCodeDto {

    @NotBlank
    @Size(min = 5, max = 20)
    private String email;

    @NotBlank
    @Size(min = 6,max = 6)
    @JsonProperty("verify_code")
    private String code;

}
