package my.mma.user.service;

import my.mma.bet.repository.BetRepository;
import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.fightevent.repository.FightEventRepository;
import my.mma.fightevent.service.FightEventService;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.entity.Alert;
import my.mma.global.entity.TargetType;
import my.mma.global.repository.AlertRepository;
import my.mma.global.s3.service.S3ImgService;
import my.mma.user.dto.UserBetRecord;
import my.mma.user.dto.UserProfileDto;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static my.mma.fightevent.dto.FightEventDto.FighterFightEventDto.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BetRepository betRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private FighterRepository fighterRepository;
    @Mock
    private FightEventRepository fightEventRepository;
    @Mock
    private S3ImgService s3ImgService;
    @Mock
    private FightEventService eventService;

    @InjectMocks
    private UserProfileService userProfileService;

    private User user;

    private final long alertFighterId1 = 2L;
    private final long alertFighterId2 = 3L;

    private final long alertEventId = 2L;
    private final String alertEventName = "event-name2";

    private final String fighterNamePrefix = "name";
    private final String fighterNicknamePrefix = "nickname";

    @BeforeEach
    void setupUserProfile() {
        setupUser();
    }

    @Test
    void test() {
        //given
        Mockito.when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Mockito.when(betRepository.getUserBetRecord(user.getId())).thenReturn(getUserBetRecord());
        Mockito.when(alertRepository.findByUserId(user.getId())).thenReturn(getAlerts());
        Mockito.when(fighterRepository.findAllById(Mockito.any())).thenReturn(getAlertFighters());
        List<FightEvent> alertFightEvents = getAlertCards();
        Mockito.when(fightEventRepository.findAllById(Mockito.any())).thenReturn(alertFightEvents);

        String s3ImgUrl = "generated-s3-img-url";
        Mockito.when(s3ImgService.generateImgUrl(Mockito.any(),Mockito.anyInt())).thenReturn(s3ImgUrl);
        Mockito.when(eventService.getMainCardDto(alertFightEvents.get(0)))
                .thenReturn(toDto(alertFightEvents.get(0).getFighterFightEvents().get(0)));

        //when
        UserProfileDto profile = userProfileService.profile(user.getEmail());

        //then
        assertThat(profile.userBetRecord().getWin()).isEqualTo(getUserBetRecord().getWin());
        assertThat(profile.userBetRecord().getLoss()).isEqualTo(getUserBetRecord().getLoss());
        assertThat(profile.userBetRecord().getNoContest()).isEqualTo(getUserBetRecord().getNoContest());

        assertThat(profile.alertEvents().size()).isEqualTo(1);
        assertThat(profile.alertEvents().get(0).getEventId()).isEqualTo(alertEventId);
        assertThat(profile.alertEvents().get(0).getEventName()).isEqualTo(alertEventName);

        assertThat(profile.alertFighters().size()).isEqualTo(2);
        assertThat(profile.alertFighters().get(0).getId()).isEqualTo(alertFighterId1);
        assertThat(profile.alertFighters().get(0).getName()).isEqualTo(fighterNamePrefix+alertFighterId1);
        assertThat(profile.alertFighters().get(0).getNickname()).isEqualTo(fighterNicknamePrefix+alertFighterId1);
        assertThat(profile.alertFighters().get(0).getHeadshotUrl()).isEqualTo(s3ImgUrl);

        assertThat(profile.alertFighters().get(1).getId()).isEqualTo(alertFighterId2);
        assertThat(profile.alertFighters().get(1).getName()).isEqualTo(fighterNamePrefix+alertFighterId2);
        assertThat(profile.alertFighters().get(1).getNickname()).isEqualTo(fighterNicknamePrefix+alertFighterId2);
        assertThat(profile.alertFighters().get(1).getHeadshotUrl()).isEqualTo(s3ImgUrl);

    }

    private void setupUser() {
        user = User.builder()
                .id(1L)
                .nickname("nickname123")
                .email("email123@google.com")
                .point(0)
                .role("ROLE_USER")
                .password("pwd123")
                .build();
    }

    private UserBetRecord getUserBetRecord() {
        return new UserBetRecord() {
            @Override
            public int getWin() {
                return 11;
            }

            @Override
            public int getLoss() {
                return 2;
            }

            @Override
            public int getNoContest() {
                return 0;
            }
        };
    }

    private List<Alert> getAlerts() {
        Alert alert = Alert.builder()
                .id(1L)
                .user(user)
                .targetType(TargetType.FIGHTER)
                .targetId(alertFighterId1)
                .build();
        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        return alerts;
    }

    private List<Fighter> getAlertFighters() {
        Fighter fighter1 = Fighter.builder()
                .id(alertFighterId1)
                .name(fighterNamePrefix +alertFighterId1)
                .nickname(fighterNicknamePrefix +alertFighterId1)
                .reach(0)
                .height(0)
                .ranking(1)
                .birthday(LocalDate.now())
                .build();
        Fighter fighter2 = Fighter.builder()
                .id(alertFighterId2)
                .name(fighterNamePrefix +alertFighterId2)
                .nickname(fighterNicknamePrefix +alertFighterId2)
                .reach(0)
                .height(0)
                .ranking(1)
                .birthday(LocalDate.now())
                .build();
        List<Fighter> fighters = new ArrayList<>();
        fighters.add(fighter1);
        fighters.add(fighter2);
        return fighters;
    }

    private List<FightEvent> getAlertCards() {
        List<FightEvent> mainCards = new ArrayList<>();
        FightEvent fightEvent = FightEvent.builder()
                .id(alertEventId)
                .location("location1")
                .eventDate(LocalDate.now())
                .completed(false)
                .name(alertEventName)
                .build();
        FighterFightEvent fighterFightEvent = FighterFightEvent.builder()
                .fightWeight("HEAVY WEIGHT")
                .winner(getAlertFighters().get(0))
                .loser(getAlertFighters().get(1))
                .fightEvent(fightEvent)
                .title(false)
                .build();
        fightEvent.getFighterFightEvents().add(fighterFightEvent);
        mainCards.add(fightEvent);
        return mainCards;
    }
}