package my.mma.fighter.service;

import my.mma.event.entity.FightEvent;
import my.mma.event.entity.FighterFightEvent;
import my.mma.event.repository.FighterFightEventRepository;
import my.mma.fighter.dto.FighterDetailDto;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.global.repository.AlertRepository;
import my.mma.global.s3.service.S3ImgService;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static my.mma.fixture.fighter.FighterFixture.createFighterWithNumber;
import static my.mma.fixture.fightevent.FightEventFixture.createUpcomingFightEventWithId;
import static my.mma.fixture.user.UserFixture.createUserWithEmail;
import static my.mma.global.entity.TargetType.FIGHTER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FighterDetailServiceTest {

    @Mock
    private FighterRepository fighterRepository;
    @Mock
    private FighterFightEventRepository fighterFightEventRepository;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private S3ImgService s3Service;

    @InjectMocks
    private FighterService fighterService;

    @DisplayName("파이터의 상세 정보를 반환(FighterDetailDto extends FighterDto)")
    @Test
    void fighterDetail() {
        //given
        String email = "myEmail123@google.com";
        long fighterId = 5L;
        Fighter fighter = createFighterWithNumber((int) fighterId);
        List<FighterFightEvent> ffes = getFighterFightEvents();
        User user = createUserWithEmail(email);
        boolean isAlertExists = true;
        String imgUrl = "img-url";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(alertRepository.existsByUserAndTargetTypeAndTargetId(
                user, FIGHTER, fighterId)).thenReturn(isAlertExists);
        when(fighterRepository.findById(fighterId)).thenReturn(Optional.of(fighter));
        when(fighterFightEventRepository.findByFighter(fighter)).thenReturn(ffes);
        when(s3Service.generateImgUrl(anyString(), anyInt())).thenReturn(imgUrl);

        //when
        FighterDetailDto fighterDetailDto = fighterService.detail(email, fighterId);

        //then
        assertThat(fighterDetailDto.getId()).isEqualTo(fighterId);
        assertThat(fighterDetailDto.getName()).isNotNull();
        assertThat(fighterDetailDto.getNickname()).isNotNull();
        assertThat(fighterDetailDto.getHeight()).isNotZero();
        assertThat(fighterDetailDto.getWeight()).isNotNull();
        assertThat(fighterDetailDto.getBodyUrl()).isNotNull();
        assertThat(fighterDetailDto.getBirthday()).isNotNull();
        assertThat(fighterDetailDto.getReach()).isNotZero();
        assertThat(fighterDetailDto.getRanking()).isNotNull();
        assertThat(fighterDetailDto.getRecord()).isNotNull();

        assertThat(fighterDetailDto.isAlert()).isEqualTo(isAlertExists);
        assertThat(fighterDetailDto.getFighterFightEvents().size()).isEqualTo(ffes.size());
    }

    private static List<FighterFightEvent> getFighterFightEvents() {
        List<FightEvent> fightEvents = new ArrayList<>();
        fightEvents.add(createUpcomingFightEventWithId(1));
        fightEvents.add(createUpcomingFightEventWithId(2));
        List<FighterFightEvent> ffes = fightEvents.stream()
                .flatMap(fightEvent -> fightEvent.getFighterFightEvents().stream()).toList();
        return ffes;
    }


}