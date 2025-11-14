package my.mma.fightevent.repository;

import my.mma.fighter.entity.FightRecord;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.fightevent.entity.property.CardStartDateTimeInfo;
import my.mma.fightevent.entity.property.FightResult;
import my.mma.fightevent.entity.property.WinMethod;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DataJpaTest
@ActiveProfiles("test")
class FighterFightEventRepositoryTest {

    @Autowired
    private FighterFightEventRepository fighterFightEventRepository;
    @Autowired
    private FightEventRepository fightEventRepository;
    @Autowired
    private FighterRepository fighterRepository;

    private final String name1 = "name1";
    private final String name2 = "name2";
    private final String name3 = UUID.randomUUID().toString();

    @BeforeEach
    void setup() {
        Fighter winner = buildFighterByName(name1);
        Fighter loser = buildFighterByName(name2);
        fighterRepository.save(winner);
        fighterRepository.save(loser);
        FightEvent fightEvent1 = buildFightEvent(null, null);
        FightEvent fightEvent2 = buildFightEvent(winner, null);
        FightEvent fightEvent3 = buildFightEvent(winner, null);
        FightEvent fightEvent4 = buildFightEvent(null, loser);
        fightEventRepository.saveAll(List.of(fightEvent1, fightEvent2, fightEvent3, fightEvent4));
    }

    @Test
    @DisplayName("[JPQL] 입력으로 들어온 Fighter가 포함된 모든 fighterFightEvent 목록 반환")
    void findByFighter_fromFighterFightEventRepository() {
        //when
        Fighter fighter1 = fighterRepository.findByName(name1).get();
        Fighter fighter2 = fighterRepository.findByName(name2).get();
        Fighter fighter3 = buildFighterByName(name3);
        fighterRepository.saveAndFlush(fighter3);
        List<FighterFightEvent> cardsIncludingName1 = fighterFightEventRepository.findByFighter(fighter1);
        List<FighterFightEvent> cardsIncludingName2 = fighterFightEventRepository.findByFighter(fighter2);
        List<FighterFightEvent> emptyCard = fighterFightEventRepository.findByFighter(fighter3);

        //then
        Assertions.assertThat(cardsIncludingName1.size()).isEqualTo(2);
        Assertions.assertThat(cardsIncludingName1.get(0).getWinner()).isEqualTo(fighter1);
        Assertions.assertThat(cardsIncludingName1.get(1).getWinner()).isEqualTo(fighter1);

        Assertions.assertThat(cardsIncludingName2.size()).isEqualTo(1);
        Assertions.assertThat(cardsIncludingName2.get(0).getLoser()).isEqualTo(fighter2);

        Assertions.assertThat(emptyCard).isEmpty();
    }

    FightEvent buildFightEvent(Fighter winner, Fighter loser) {
        int mainCardCnt = 6;
        int prelimCardCnt = 4;
        int earlyCardCnt = 2;
        FightEvent fightEvent = FightEvent.builder()
                .eventDate(LocalDate.of(2000, 10, 10))
                .name("eventName123")
                .location("location123")
                .completed(false)
                .mainCardCnt(mainCardCnt)
                .mainCardDateTimeInfo(buildCardStartDateTimeInfo())
                .prelimCardCnt(prelimCardCnt)
                .prelimCardDateTimeInfo(buildCardStartDateTimeInfo())
                .earlyCardCnt(earlyCardCnt)
                .earlyCardDateTimeInfo(buildCardStartDateTimeInfo())
                .fighterFightEvents(new ArrayList<>())
                .build();
        for (int i = 0; i < mainCardCnt + prelimCardCnt + earlyCardCnt - 1; i++)
            fightEvent.getFighterFightEvents().add(buildFighterFightEvent(fightEvent, null, null));
        fightEvent.getFighterFightEvents().add(buildFighterFightEvent(fightEvent, winner, loser));
        return fightEvent;
    }

    FighterFightEvent buildFighterFightEvent(FightEvent fightEvent, Fighter winner, Fighter loser) {
        if (winner == null) {
            winner = buildFighterByName(UUID.randomUUID().toString());
            fighterRepository.save(winner);
        }
        if (loser == null) {
            loser = buildFighterByName(UUID.randomUUID().toString());
            fighterRepository.save(loser);
        }
        return FighterFightEvent.builder()
                .fightEvent(fightEvent)
                .winner(winner)
                .loser(loser)
                .fightResult(FightResult.builder()
                        .nc(false)
                        .draw(false)
                        .endTime(LocalTime.of(0, 15, 0))
                        .round(3)
                        .winMethod(WinMethod.U_DEC).build())
                .title(false)
                .fightWeight("fight-weight")
                .build();
    }

    Fighter buildFighterByName(String name) {
        return Fighter.builder()
                .name(name)
                .nickname("nickname-" + name)
                .reach(100)
                .birthday(LocalDate.of(2000, 1, 1))
                .ranking(15)
                .weight(155.5)
                .height(100)
                .fightRecord(FightRecord.builder().win(10).loss(0).draw(0).build())
                .build();
    }

    CardStartDateTimeInfo buildCardStartDateTimeInfo() {
        return CardStartDateTimeInfo.builder()
                .date(LocalDate.of(2000, 10, 10))
                .time(LocalTime.of(3, 3, 3))
                .build();
    }

}