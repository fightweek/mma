package my.mma.vote.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.fightevent.dto.StreamFightEventDto;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.fightevent.repository.FighterFightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import my.mma.vote.dto.VoteCntDto;
import my.mma.vote.dto.VoteRateDto;
import my.mma.vote.dto.VoteRequest;
import my.mma.vote.entity.Vote;
import my.mma.vote.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VoteService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final FighterFightEventRepository fighterFightEventRepository;
    private final RedisUtils<StreamFightEventDto> streamFightEventredisUtils;


    @Transactional
    public VoteRateDto vote(String email, VoteRequest voteRequest) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
        FighterFightEvent ffe = extractFighterFightEventById(voteRequest.fighterFightEventId());
        Optional<Vote> voteOptional = voteRepository.findByUserAndFighterFightEvent(user, ffe);
        Vote vote;
        if (voteOptional.isPresent()) {
            vote = voteOptional.get();
            // 기존 예측과 동일한 예측 요청을 받은 경우
            if (vote.getWinnerId().equals(voteRequest.winnerId()))
                return null;
            // 그게 아니라면, 기존 예측 swap
            else {
                log.info("User {} swapped vote for ffeId {}", user.getEmail(), ffe.getId());
                vote.swapPrediction();
            }
        } else {
            vote = voteRequest.toEntity(user, ffe);
            voteRepository.save(vote);
        }
//        voteRepository.flush();

        StreamFightEventDto sfe = streamFightEventredisUtils.getData("current-event");
        VoteCntDto voteCntDto = voteRepository.countVoteByFfeIdAndFighters(ffe.getId(), ffe.getWinner().getId(), ffe.getLoser().getId());

        Integer fighter1Votes = voteCntDto.getWinnerVotes();
        Integer fighter2Votes = voteCntDto.getLoserVotes();

        int sum = fighter1Votes + fighter2Votes;
        double fighter1VoteRate = fighter1Votes == 0 ? 0.0 : 100.0 / sum * fighter1Votes;
        double fighter2VoteRate = fighter2Votes == 0 ? 0.0 : 100.0 / sum * fighter2Votes;
        sfe.getFighterFightEvents()
                .forEach(e -> {
                    if (e.getId().equals(voteRequest.fighterFightEventId())) {
                        e.setWinnerVoteRate(fighter1VoteRate);
                        e.setLoserVoteRate(fighter2VoteRate);
                    }
                });
        streamFightEventredisUtils.updateData("current-event", sfe);
        return VoteRateDto.builder()
                .ffeId(ffe.getId())
                .winnerVoteRate(fighter1VoteRate)
                .loserVoteRate(fighter2VoteRate)
                .build();
    }

    private FighterFightEvent extractFighterFightEventById(Long ffeId) {
        return fighterFightEventRepository.findById(ffeId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
    }

}
