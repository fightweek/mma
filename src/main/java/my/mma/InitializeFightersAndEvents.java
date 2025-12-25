package my.mma;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.fighter.dto.ChosenGameFighterNamesDto;
import my.mma.announcement.entity.Announcement;
import my.mma.announcement.repository.AnnounceRepository;
import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.fightevent.entity.property.FightResult;
import my.mma.fightevent.entity.property.WinMethod;
import my.mma.fightevent.repository.FightEventRepository;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.entity.FightRecord;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.redis.prefix.RedisKeyPrefix;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.stream.dto.BlockedUserIdsDto;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
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
import java.util.*;

import static my.mma.global.redis.prefix.RedisKeyPrefix.BLOCKED_USERS_PREFIX;
import static my.mma.global.utils.ModifyUtils.toKg;

@Slf4j
@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InitializeFightersAndEvents {

    private final FighterRepository fighterRepository;
    private final FightEventRepository fightEventRepository;
    private final UserRepository userRepository;
    private final AnnounceRepository announceRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisUtils<ChosenGameFighterNamesDto> adminChosenFightersRedisUtils;
    private final RedisUtils<BlockedUserIdsDto> blockedUsersRedisUtils;

    /**
     * 스프링 애플리케이션 컨텍스트가 완전히 초기화되고 모든 빈들이 로드된 후 실행됨
     * 즉, 애플리케이션이 시작되어 서비스 요청을 처리할 준비가 되면 실행됨
     *
     * @PostConstruct 와 다르게, Proxy 클래스의 생성도 마친 상태에서 실행되므로,
     * AOP가 적용된 클래스에 대해서도 작업이 가능하다.
     */
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initializeAll() {
        User user = userRepository.save(User.builder()
                .email("jht1234@naver.com")
                .password(bCryptPasswordEncoder.encode("pwd123"))
                .nickname("진현택")
                .role("ROLE_ADMIN")
                .point(1000)
                .build());
        setBlockedUsers(user);
        saveAnnouncements();
        adminChosenFightersRedisUtils.saveData("chosenFighters", new ChosenGameFighterNamesDto());
        readJsonFile();
    }

    private void setBlockedUsers(User user) {
        Set<Long> blockedUserIds = new HashSet<>();
        for (int i = 0; i < 20; i++) {
            User blockedUser = userRepository.save(User.builder()
                    .email("pwd4321" + i + "@naver.com")
                    .password(bCryptPasswordEncoder.encode("pwd123"))
                    .nickname("nickname-" + i)
                    .role("ROLE_USER")
                    .point(1000)
                    .build());
            userRepository.save(blockedUser);
            blockedUserIds.add(blockedUser.getId());
        }
        BlockedUserIdsDto blockedUserIdsDto = new BlockedUserIdsDto();
        blockedUserIdsDto.getBlockedUserIds().addAll(blockedUserIds);
        blockedUsersRedisUtils.saveData(BLOCKED_USERS_PREFIX.getPrefix() + user.getId(),
                blockedUserIdsDto);
    }

    private void saveAnnouncements() {
        List<Announcement> announcements = new ArrayList<>();
        for (int i = 0; i < 55; i++) {
            StringBuilder sb = new StringBuilder();
            if (i % 2 == 0)
                for (int j = 0; j < 100; j++) {
                    sb.append("This is announcement ").append(i).append("\\'s").append("content");
                }
            else
                sb.append("This is announcement ").append(i).append("\\'s").append("content");
            Announcement announcement = Announcement.builder()
                    .title("Announcement-" + i)
                    .content(sb.toString())
                    .pinned(i % 3 == 0)
                    .build();
            announcements.add(announcement);
        }
        announceRepository.saveAll(announcements);
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
            FileReader reader = new FileReader("/my-files/ufc_data.json");
//            FileReader reader = new FileReader("etc/ufc_data.json");
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
                        .height(!fighterObj.get("height").toString().contains("-") ?
                                toCentimeter(fighterObj.get("height").toString()) : 0)
                        .reach(!fighterObj.get("reach").toString().contains("-") ?
                                (int) (Integer.parseInt(fighterObj.get("reach").toString()) * 2.54 + 0.5) : 0)
                        .weight(!weight.contains("-") ? toKg(weight) : null)
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
//                if (i == 96)
//                    break;
            }
            for (Object arr : events) {
                JSONObject eventObj = (JSONObject) arr;
                JSONArray cards = (JSONArray) eventObj.get("cards");
                FightEvent fightEvent = FightEvent.builder()
                        .name(eventObj.get("event_name").toString())
                        .eventDate(LocalDate.parse(eventObj.get("event_date").toString(),
                                DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)))
                        .location(eventObj.get("location").toString())
                        .completed(true)
                        .build();
                for (Object arr2 : cards) {
                    JSONObject cardObj = (JSONObject) arr2;
                    String winnerName = cardObj.get("winner").toString();
                    String loserName = cardObj.get("loser").toString();
                    Object method = cardObj.get("method");
                    Object description = cardObj.get("description");
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
                            .title(Boolean.parseBoolean(cardObj.get("is_title").toString()))
                            .fightWeight(cardObj.get("fight_weight").toString())
                            .fightResult(FightResult.builder()
                                    .winMethod(method != null ? WinMethod.valueOf(method.toString()) : null)
                                    .winDescription(description != null ? description.toString() : null)
                                    .endTime(LocalTime.of(0, Integer.parseInt(timeParts[0]), Integer.parseInt(timeParts[1])))
                                    .round(Integer.parseInt(cardObj.get("round").toString()))
                                    .draw(Boolean.parseBoolean(cardObj.get("draw").toString()))
                                    .nc(Boolean.parseBoolean(cardObj.get("nc").toString()))
                                    .build())
                            .build();
                    fightEvent.addFighterFightEvent(fighterFightEvent);
                }
                fightEventRepository.save(fightEvent);
                j++;
                if (j == 40)
                    break;
            }
        } catch (Exception e) {
            log.error("error=", e);
            throw new CustomException(CustomErrorCode.SERVER_ERROR_500);
        }
    }

    public int toCentimeter(String footInch) {
        String[] split = footInch.split("'\\s*");
        if (split.length != 2)
            return 0;
        return (int) ((Integer.parseInt(split[0]) * 12 + Integer.parseInt(split[1])) * 2.54 + 0.5);
    }

}
