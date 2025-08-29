package my.mma.global.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdatePreferenceRequest {

    private UpdatePreferenceCategory category;
    private Long targetId;
    private boolean on;

}

