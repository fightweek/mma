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

    private UpdatePreferenceCategory category;
    private Long targetId;
    private boolean on;

}

