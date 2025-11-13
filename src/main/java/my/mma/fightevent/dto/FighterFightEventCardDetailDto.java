package my.mma.fightevent.dto;

import lombok.Builder;
import my.mma.fightevent.dto.StreamFightEventDto.FighterFightEventCardFighterDto;


@Builder
public record FighterFightEventCardDetailDto(FighterFightEventCardFighterDto winner,
                                             FighterFightEventCardFighterDto loser,
                                             String fightWeight) {}