package my.mma.news.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NewsSource {

    MMA_JUNIKE("mmajunkie"),
    MMA_FIGHTING("mmafighting"),
    FREAK_MMA("freak.mma");

    private final String source_name;

}
