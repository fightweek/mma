package my.mma.user.dto;

import lombok.Builder;
import my.mma.fightevent.dto.FightEventDto.FighterFightEventDto;
import my.mma.fighter.dto.FighterDto;

import java.util.List;

@Builder
public record UserProfileDto(UserBetRecord userBetRecord, List<FighterDto> alertFighters, List<FighterFightEventDto> alertEvents) {
}
