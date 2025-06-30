package my.mma.global.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdatePreferenceDto {

    private String category;
    private Long targetId;
    @JsonProperty("isOn")
    private boolean isOn;

}
