package my.mma.bet.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.bet.entity.Bet;
import my.mma.bet.entity.BetCard;
import my.mma.bet.repository.BetRepository;
import my.mma.event.dto.StreamFightEventDto;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.repository.FightEventRepository;
import my.mma.event.repository.FighterFightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.bet.dto.BetResponse;
import my.mma.bet.dto.SingleBetRequest;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static my.mma.global.redis.prefix.RedisKeyPrefix.BET_PREFIX;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BetService {

    private final UserRepository userRepository;
    private final BetRepository betRepository;
    private final FightEventRepository fightEventRepository;
    private final FighterFightEventRepository fighterFightEventRepository;
    private final RedisUtils<StreamFightEventDto> streamFightEventredisUtils;
    private final RedisUtils<BetResponse> weeklyBetResponseRedisUtils;

    @Transactional
    public Integer bet(String email, SingleBetRequest betRequest) {
        User user = extractUserByEmail(email);
        Bet bet = betRequest.toEntity(user);
        for (SingleBetRequest.SingleBetCardRequest sb : betRequest.singleBetCards()) {
            FighterFightEvent ffe = extractFighterFightEventById(sb.fighterFightEventId());
            BetCard betCard = sb.toEntity(ffe, bet);
            bet.addBetCard(betCard);
        }
        user.updatePoint(user.getPoint() - betRequest.seedPoint());
        betRepository.save(bet);
        BetResponse weeklyBetResponse = weeklyBetResponseRedisUtils.getData(BET_PREFIX.getPrefix() + user.getId().toString());
        if (weeklyBetResponse == null) {
            weeklyBetResponse = new BetResponse();
            weeklyBetResponse.setEventName(streamFightEventredisUtils.getData("current-event").getName());
            weeklyBetResponse.addBetDto(BetResponse.SingleBetResponse.toDto(bet));
            weeklyBetResponseRedisUtils.saveDataWithTTL(BET_PREFIX.getPrefix() + user.getId().toString(), weeklyBetResponse, Duration.ofDays(30));
        } else {
            weeklyBetResponse.getSingleBets().add(BetResponse.SingleBetResponse.toDto(bet));
            weeklyBetResponseRedisUtils.saveDataWithTTL(BET_PREFIX.getPrefix() + user.getId().toString(), weeklyBetResponse, Duration.ofDays(30));
        }
        return user.getPoint();
    }

    public BetResponse betHistory(String email, Long eventId) {
        User user = extractUserByEmail(email);
        StreamFightEventDto streamFightEvent = streamFightEventredisUtils.getData("current-event");
        long currentEventId = streamFightEvent.getId();
        // 요청받은 eventId가 이번 주의 fightEvent인 경우 -> 무조건 redis에 담긴 해당 user의 betHistory 반환
        if (eventId == currentEventId) {
            BetResponse betResponse = weeklyBetResponseRedisUtils.getData(BET_PREFIX.getPrefix() + user.getId().toString());
            if(betResponse == null){
                // front에서 꼭 eventName을 포함한 응답을 받아야 하므로, 이벤트명만을 가진 껍데기 betResponse를 반환
                BetResponse temp = new BetResponse();
                temp.setEventName(streamFightEvent.getName());
                return temp;
            }
            return betResponse;
        }
        //  eventId & 특정 userId에 필더링된 bet(s) data
        List<Bet> bets = betRepository.findByEventIdAndUserIdWithBetCards(eventId, user.getId());
        BetResponse betResponse = new BetResponse();
        fightEventRepository.findById(eventId).ifPresent(fightEvent -> betResponse.setEventName(fightEvent.getName()));
        System.out.println(bets);
        bets.stream()
                .map(BetResponse.SingleBetResponse::toDto)
                .forEach(betResponse::addBetDto);
        return betResponse;
    }

    @Transactional
    public BetResponse deleteBet(String email, Long betId){
        User user = extractUserByEmail(email);
        BetResponse userBet = weeklyBetResponseRedisUtils.getData(BET_PREFIX.getPrefix() + user.getId().toString());
        BetResponse.SingleBetResponse singleBet = userBet.getSingleBets().stream().filter(sb -> sb.getBetId() == betId)
                .findFirst().orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400)
                );
        userBet.getSingleBets().remove(singleBet);
        weeklyBetResponseRedisUtils.updateData(BET_PREFIX.getPrefix()+user.getId().toString(),userBet);
        betRepository.deleteById(betId);
        user.updatePoint(user.getPoint()+singleBet.getSeedPoint());
        System.out.println("userBet = " + userBet);
        return userBet;
    }

    private User extractUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
    }

    private FighterFightEvent extractFighterFightEventById(Long ffeId) {
        return fighterFightEventRepository.findById(ffeId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST_400));
    }

}
