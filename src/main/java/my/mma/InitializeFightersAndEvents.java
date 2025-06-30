package my.mma;

import lombok.extern.slf4j.Slf4j;
import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FightResult;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.entity.WinMethod;
import my.mma.event.repository.FightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.entity.FightRecord;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.user.repository.UserRepository;
import my.mma.user.entity.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@Transactional(readOnly = true)
public class InitializeFightersAndEvents {

    private final FighterRepository fighterRepository;
    private final FightEventRepository fightEventRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public InitializeFightersAndEvents(FighterRepository fighterRepository,
                                       FightEventRepository fightEventRepository, UserRepository userRepository,
                                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userRepository = userRepository;
        this.fighterRepository = fighterRepository;
        this.fightEventRepository = fightEventRepository;
    }

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initializeAll() {
        User user = User.builder()
                .email("jht1234@naver.com")
                .password(bCryptPasswordEncoder.encode("pwd123"))
                .nickname("진현택")
                .role("ROLE_ADMIN")
                .point(0)
                .build();
        userRepository.save(user);
        readJsonFile();
    }

    /**
     * {
     * "name": "Merab Dvalishvili",
     * "nickname": "The Machine",
     * "record": "20-4-0",
     * "height": "5' 6",
     * "weight": "135",
     * "birthday": "Jan 10, 1991",
     * "reach": "68",
     * "stance": "Orthodox"
     * },
     */
    private void readJsonFile() {
        // 3-66
        JSONParser parser = new JSONParser();
        try {
            FileReader reader = new FileReader("E:\\etc\\ufc_data.json");
            JSONObject jsonObj = (JSONObject) parser.parse(reader);
            JSONArray fighters = (JSONArray) jsonObj.get("fighters");
            JSONArray events = (JSONArray) jsonObj.get("events");
            int i = 0, j = 0;
            for (Object arr : fighters) {
                JSONObject fighterObj = (JSONObject) arr;
                String record = fighterObj.get("record").toString();
                String weight = fighterObj.get("weight").toString();
                String[] split_record = record.split("-");
                Fighter fighter = Fighter.builder()
                        .name(fighterObj.get("name").toString())
                        .nickname(fighterObj.get("nickname") != null ? fighterObj.get("nickname").toString() : null)
                        .height(!fighterObj.get("height").toString().contains("-") ? fighterObj.get("height").toString() : null)
                        .reach(!fighterObj.get("reach").toString().contains("-") ? Integer.parseInt(fighterObj.get("reach").toString()) : 0)
                        .weight(!weight.contains("-") ? weight : null)
//                        .division(!weight.contains("-") ? Fighter.get_division(weight) : null)
                        .birthday(!fighterObj.get("birthday").toString().contains("-") ? LocalDate.parse(fighterObj.get("birthday").toString(),
                                DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH)) : null)
                        .fightRecord(FightRecord.builder()
                                .win(Integer.parseInt(split_record[0]))
                                .loss(Integer.parseInt(split_record[1]))
                                .draw(((int) split_record[2].charAt(0)) - 48)
                                .build())
                        .build();
                fighterRepository.save(fighter);
                i++;
//                if (i == 66)
//                    break;
            }
            for (Object arr : events) {
                JSONObject eventObj = (JSONObject) arr;
                JSONArray cards = (JSONArray) eventObj.get("cards");
                FightEvent fightEvent = FightEvent.builder()
                        .name(eventObj.get("event_name").toString())
                        .eventDate(LocalDate.parse(eventObj.get("event_date").toString(),
                                DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)))
                        .eventLocation(eventObj.get("location").toString())
                        .completed(true)
                        .build();
                List<FighterFightEvent> fighterFightEvents = new ArrayList<>();
                for (Object arr2 : cards) {
                    JSONObject cardObj = (JSONObject) arr2;
                    String winnerName = cardObj.get("winner").toString();
                    String loserName = cardObj.get("loser").toString();
                    String method = cardObj.get("method").toString();
                    String[] timeParts = cardObj.get("fight_time").toString().split(":");
                    Fighter winner = fighterRepository.findByName(winnerName).orElseThrow(
                            () -> new RuntimeException("No such fighter found " + winnerName)
                    );
                    Fighter loser = fighterRepository.findByName(loserName).orElseThrow(
                            () -> new RuntimeException("No such fighter found " + loserName)
                    );
                    FighterFightEvent fighterFightEvent = FighterFightEvent.builder()
                            .winner(winner)
                            .loser(loser)
                            .fightWeight(cardObj.get("fight_weight").toString())
                            .fightResult(FightResult.builder()
                                    .winMethod(
                                            method.contains("DEC") ? WinMethod.valueOf(method) :
                                                    (method.contains("SUB") ? WinMethod.SUB :
                                                            (method.contains("KO") ? WinMethod.KO_TKO : WinMethod.ELSE))
                                    )
                                    .winDescription(method)
                                    .endTime(LocalTime.of(0, Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])))
                                    .round(Integer.parseInt(cardObj.get("round").toString()))
                                    .build())
                            .build();
                    fightEvent.addFighterFightEvent(fighterFightEvent);
                }
                fightEventRepository.save(fightEvent);
                j++;
//                if (j == 3)
//                    break;
            }
        } catch (Exception e) {
            log.error("error=", e);
            throw new CustomException(CustomErrorCode.SERVER_ERROR);
        }
    }

}
