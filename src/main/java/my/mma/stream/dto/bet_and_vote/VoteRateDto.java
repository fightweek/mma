package my.mma.stream.dto.bet_and_vote;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class VoteRateDto {

    private final long ffeId;
    private final double winnerVoteRate;
    private final double loserVoteRate;

}
