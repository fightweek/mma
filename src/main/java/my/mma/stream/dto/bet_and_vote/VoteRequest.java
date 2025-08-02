package my.mma.stream.dto.bet_and_vote;

import lombok.*;
import my.mma.bet.entity.Vote;
import my.mma.event.entity.FighterFightEvent;
import my.mma.user.entity.User;

@Builder
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class VoteRequest {

    private Long winnerId;
    private Long loserId;
    private Long fighterFightEventId;

    public Vote toEntity(User user, FighterFightEvent fighterFightEvent){
        return Vote.builder()
                .user(user)
                .fighterFightEvent(fighterFightEvent)
                .winnerId(winnerId)
                .loserId(loserId)
                .build();
    }

}
