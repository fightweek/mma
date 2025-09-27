package my.mma.stream.dto.bet_and_vote;

import my.mma.bet.entity.Vote;
import my.mma.event.entity.FighterFightEvent;
import my.mma.user.entity.User;

public record VoteRequest(long winnerId, long loserId, long fighterFightEventId) {

    public Vote toEntity(User user, FighterFightEvent fighterFightEvent){
        return Vote.builder()
                .user(user)
                .fighterFightEvent(fighterFightEvent)
                .winnerId(winnerId)
                .loserId(loserId)
                .build();
    }

}
