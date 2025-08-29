package my.mma.stream.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.bet.entity.Bet;
import my.mma.bet.entity.BetCard;
import my.mma.bet.entity.Vote;
import my.mma.bet.repository.BetRepository;
import my.mma.bet.repository.VoteRepository;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.repository.FighterFightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.stream.dto.ReportRequest;
import my.mma.stream.dto.bet_and_vote.*;
import my.mma.stream.repository.ReportRepository;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

import static my.mma.global.redis.prefix.RedisKeyPrefix.BET_PREFIX;
import static my.mma.stream.dto.bet_and_vote.TodayBetResponse.SingleBetResponse;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StreamPredictionService {

    private final UserRepository userRepository;
    private final VoteRepository voteRepository;
    private final BetRepository betRepository;
    private final ReportRepository reportRepository;
    private final FighterFightEventRepository fighterFightEventRepository;
    private final RedisUtils<StreamFightEventDto> streamFightEventredisUtils;
    private final RedisUtils<TodayBetResponse> todayBetResponseRedisUtils;

    @Transactional
    public VoteRateDto vote(String email, VoteRequest voteRequest) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
        FighterFightEvent ffe = extractFighterFightEventById(voteRequest.getFighterFightEventId());
        Optional<Vote> voteOptional = voteRepository.findByUserAndFighterFightEvent(user, ffe);
        Vote vote;
        if (voteOptional.isPresent()) {
            vote = voteOptional.get();
            if (vote.getWinnerId().equals(voteRequest.getWinnerId()))
                return null;
            else {
                log.info("User {} swapped vote for ffeId {}", user.getEmail(), ffe.getId());
                vote.swapPrediction();
            }
        } else {
            vote = voteRequest.toEntity(user, ffe);
            voteRepository.save(vote);
        }
        voteRepository.flush();

        StreamFightEventDto sfe = streamFightEventredisUtils.getData("current-event");
        VoteCntDto voteCntDto = voteRepository.countVoteByFfeIdAndFighters(ffe.getId(), ffe.getWinner().getId(), ffe.getLoser().getId());

        Integer fighter1Votes = voteCntDto.getWinnerVotes();
        Integer fighter2Votes = voteCntDto.getLoserVotes();
        System.out.println("fighter1Votes = " + fighter1Votes);
        System.out.println("fighter2Votes = " + fighter2Votes);

        int sum = fighter1Votes + fighter2Votes;
        double fighter1VoteRate = fighter1Votes == 0 ? 0.0 : 100.0 / sum * fighter1Votes;
        double fighter2VoteRate = fighter2Votes == 0 ? 0.0 : 100.0 / sum * fighter2Votes;
        sfe.getFighterFightEvents()
                .forEach(e -> {
                    if (e.getId().equals(voteRequest.getFighterFightEventId())) {
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

    @Transactional
    public Integer bet(String email, SingleBetRequest betRequest) {
        User user = extractUserByEmail(email);
        Bet bet = betRequest.toEntity(user);
        int totalPoint = 0;
        for (SingleBetRequest.SingleBetCardRequest sb : betRequest.getSingleBetCards()) {
            FighterFightEvent ffe = extractFighterFightEventById(sb.getFighterFightEventId());
            BetCard betCard = sb.toEntity(ffe, bet);
            totalPoint += sb.getSeedPoint();
            bet.addBetCard(betCard);
        }
        user.updatePoint(user.getPoint()-totalPoint);
        betRepository.save(bet);
        TodayBetResponse todayBetResponse = todayBetResponseRedisUtils.getData(BET_PREFIX.getPrefix()+user.getId().toString());
        if(todayBetResponse == null){
            todayBetResponse = new TodayBetResponse();
            todayBetResponse.addBetDto(SingleBetResponse.toDto(bet));
            todayBetResponseRedisUtils.saveDataWithTTL(BET_PREFIX.getPrefix()+user.getId().toString(),todayBetResponse,Duration.ofHours(10));
        }else{
            todayBetResponse.getSingleBets().add(SingleBetResponse.toDto(bet));
            todayBetResponseRedisUtils.saveDataWithTTL(BET_PREFIX.getPrefix()+user.getId().toString(),todayBetResponse,Duration.ofHours(10));
        }
        return user.getPoint();
    }

    public TodayBetResponse todayBetHistory(String email) {
        User user = extractUserByEmail(email);
        return todayBetResponseRedisUtils.getData(BET_PREFIX.getPrefix()+user.getId().toString());
    }

    @Transactional
    public void report(String email, ReportRequest request) {
        User user = extractUserByEmail(email);
        reportRepository.save(request.toEntity(user.getId()));
    }

    private User extractUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
    }

    private FighterFightEvent extractFighterFightEventById(Long ffeId){
        return fighterFightEventRepository.findById(ffeId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
    }
}
