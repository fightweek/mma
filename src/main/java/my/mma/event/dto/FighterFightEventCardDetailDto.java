package my.mma.event.dto;

import my.mma.event.dto.StreamFightEventDto.FighterFightEventCardFighterDto;


public record FighterFightEventCardDetailDto(FighterFightEventCardFighterDto winner, FighterFightEventCardFighterDto loser, String fightWeight) {

}
