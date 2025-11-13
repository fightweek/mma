package my.mma.vote.dto;

import my.mma.vote.entity.Vote;
import my.mma.fightevent.entity.FighterFightEvent;
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
