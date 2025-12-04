package my.mma.fightevent.service;

import my.mma.exception.CustomException;
import my.mma.fightevent.dto.CardStartDateTimeInfoDto;
import my.mma.fightevent.dto.FightEventDto;
import my.mma.fightevent.dto.FightEventDto.FighterFightEventDto;
import my.mma.fightevent.dto.FighterFightEventCardDetailDto;
import my.mma.fightevent.entity.FightEvent;
import my.mma.fightevent.entity.FighterFightEvent;
import my.mma.fightevent.entity.property.CardStartDateTimeInfo;
import my.mma.fightevent.repository.FightEventRepository;
import my.mma.fightevent.repository.FighterFightEventRepository;
import my.mma.alert.repository.AlertRepository;
import my.mma.global.s3.service.S3ImgService;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.ceil;
import static my.mma.exception.CustomErrorCode.BAD_REQUEST_400;
import static my.mma.fixture.entity.fightevent.FightEventFixture.createUpcomingFightEventWithId;
import static my.mma.fixture.entity.user.UserFixture.createUser;
import static my.mma.alert.constant.AlertTarget.UPCOMING_EVENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FightEventServiceTest {

    @Mock
    private FightEventRepository fightEventRepository;
    @Mock
    private FighterFightEventRepository fighterFightEventRepository;
    @Mock
    private S3ImgService s3Service;
    @Mock
    private AlertRepository alertRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FightEventService fightEventService;

    @DisplayName("이벤트 날짜가 입력으로 들어올 때, 해당 날짜에 해당하는 fightEvent를 DTO로 반환")
    @Test
    void returnFightEventDto_givenFightEventDate() {
        //given
        User user = createUser();
        FightEvent fightEvent = createUpcomingFightEventWithId(1);
        boolean isAlerted = true;
        String imgUrl = "img-url";
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(fightEventRepository.findByEventDate(any(LocalDate.class))).thenReturn(Optional.of(fightEvent));
        when(alertRepository.existsByUserAndAlertTargetAndTargetId(user, UPCOMING_EVENT, fightEvent.getId())).thenReturn(isAlerted);
        when(s3Service.generateImgUrl(anyString(), anyInt())).thenReturn(imgUrl);

        //when
        FightEventDto fightEventDto = fightEventService.getSchedule(LocalDate.now(), user.getEmail());

        //then
        for (FighterFightEventDto ffe : fightEventDto.getFighterFightEvents()) {
            assertThat(ffe.getWinner().getHeadshotUrl()).isEqualTo(imgUrl);
            assertThat(ffe.getLoser().getHeadshotUrl()).isEqualTo(imgUrl);
        }
        assertThat(fightEventDto.isAlert()).isEqualTo(isAlerted);
        assertThat(fightEventDto.isUpcoming()).isNotEqualTo(fightEvent.isCompleted());
        assertThat(fightEventDto.getId()).isEqualTo(fightEvent.getId());
        assertThat(fightEventDto.getName()).isEqualTo(fightEvent.getName());
        assertThat(fightEventDto.getDate()).isEqualTo(fightEvent.getEventDate());
        assertThat(fightEventDto.getLocation()).isEqualTo(fightEvent.getLocation());
        assertThat(compareCardStartDateTimeInfo(
                fightEventDto.getMainCardDateTimeInfo(), fightEvent.getMainCardDateTimeInfo()
        )).isTrue();
        assertThat(compareCardStartDateTimeInfo(
                fightEventDto.getPrelimCardDateTimeInfo(), fightEvent.getPrelimCardDateTimeInfo()
        )).isTrue();
        assertThat(compareCardStartDateTimeInfo(
                fightEventDto.getEarlyCardDateTimeInfo(), fightEvent.getEarlyCardDateTimeInfo()
        )).isTrue();
        assertThat(fightEventDto.getMainCardCnt()).isEqualTo(fightEvent.getMainCardCnt());
        assertThat(fightEventDto.getPrelimCardCnt()).isEqualTo(fightEvent.getPrelimCardCnt());
        assertThat(fightEventDto.getEarlyCardCnt()).isEqualTo(fightEvent.getEarlyCardCnt());
        assertThat(fightEventDto.getFighterFightEvents().size()).isEqualTo(fightEvent.getFighterFightEvents().size());
    }

    @DisplayName("검색 키워드(이벤트명)와 pageable을 기준으로 메인 이벤트 카드 DTO 목록을 반환한다")
    @Test
    void returnFighterFightEventDtoPage_givenNameSearched() {
        //given
        String imgUrl = "img-url";
        int pageSize = 10;
        int fightEventCount = 15;
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<FightEvent> fightEventPage = getFightEventPage(fightEventCount, pageable);
        when(fightEventRepository.findByNameContainingIgnoreCase(anyString(), eq(pageable)
        )).thenReturn(Optional.of(fightEventPage));
        when(s3Service.generateImgUrl(anyString(), anyInt())).thenReturn(imgUrl);

        //when
        Page<FighterFightEventDto> mainCardPageDto = fightEventService.search("", pageable);

        //then
        List<FighterFightEvent> pagedMainCards = fightEventPage.map(
                fightEvent -> fightEvent.getFighterFightEvents().get(0)).toList();
        List<FighterFightEventDto> pagedMainCardsDto = mainCardPageDto.toList();
        for (int i = 0; i < pagedMainCardsDto.size(); i++) {
            assertThat(pagedMainCardsDto.get(i).getEventName()).isEqualTo(fightEventPage.toList().get(i).getName());
            assertThat(pagedMainCardsDto.get(i).getId()).isEqualTo(pagedMainCards.get(i).getId());
            assertThat(pagedMainCardsDto.get(i).getFightWeight()).isEqualTo(pagedMainCards.get(i).getFightWeight());
            assertThat(pagedMainCardsDto.get(i).getWinner().getName()).isEqualTo(pagedMainCards.get(i).getWinner().getName());
            assertThat(pagedMainCardsDto.get(i).getWinner().getNickname()).isEqualTo(pagedMainCards.get(i).getWinner().getNickname());
            assertThat(pagedMainCardsDto.get(i).getWinner().getHeadshotUrl()).isEqualTo(imgUrl);
            assertThat(pagedMainCardsDto.get(i).getLoser().getName()).isEqualTo(pagedMainCards.get(i).getLoser().getName());
            assertThat(pagedMainCardsDto.get(i).getLoser().getNickname()).isEqualTo(pagedMainCards.get(i).getLoser().getNickname());
            assertThat(pagedMainCardsDto.get(i).getLoser().getHeadshotUrl()).isEqualTo(imgUrl);
            assertThat(pagedMainCardsDto.get(i).getEventId()).isEqualTo(pagedMainCards.get(i).getFightEvent().getId());
            assertThat(pagedMainCardsDto.get(i).getEventName()).isEqualTo(pagedMainCards.get(i).getFightEvent().getName());
        }
        assertThat(mainCardPageDto.getSize()).isEqualTo(pageSize);
        assertThat(mainCardPageDto.getTotalPages()).isEqualTo((int) ceil((double) fightEventCount / pageSize));
    }

    @DisplayName("입력된 파이트 카드의 id(ffeId)에 해당되는 카드의 상세 정보를 반환한다.")
    @Test
    void returnFighterFightEventDetail_givenFfeId() {
        //given
        String imgUrl = "img-url";
        FightEvent fightEvent = createUpcomingFightEventWithId(1);
        FighterFightEvent ffe = fightEvent.getFighterFightEvents().get(2);
        when(fighterFightEventRepository.findById(ffe.getId())).thenReturn(Optional.of(ffe));
        when(s3Service.generateImgUrl(anyString(),anyInt())).thenReturn(imgUrl);

        //when
        FighterFightEventCardDetailDto cardDetailDto = fightEventService.cardDetail(ffe.getId());

        //then
        assertThat(cardDetailDto.fightWeight()).isEqualTo(ffe.getFightWeight());

        assertThat(cardDetailDto.winner().getId()).isEqualTo(ffe.getWinner().getId());
        assertThat(cardDetailDto.winner().getName()).isEqualTo(ffe.getWinner().getName());
        assertThat(cardDetailDto.winner().getNickname()).isEqualTo(ffe.getWinner().getNickname());
        assertThat(cardDetailDto.winner().getBodyUrl()).isEqualTo(imgUrl);

        assertThat(cardDetailDto.loser().getId()).isEqualTo(ffe.getLoser().getId());
        assertThat(cardDetailDto.loser().getName()).isEqualTo(ffe.getLoser().getName());
        assertThat(cardDetailDto.loser().getNickname()).isEqualTo(ffe.getLoser().getNickname());
        assertThat(cardDetailDto.loser().getBodyUrl()).isEqualTo(imgUrl);
    }

    @DisplayName("입력된 파이트 카드의 id(ffeId)에 해당되는 카드가 없으면, 400 예외가 발생한다.")
    @Test
    void throwException400_whenFfeIdNotFound() {
        //given
        Long ffeId = 2L;
        when(fighterFightEventRepository.findById(ffeId)).thenReturn(Optional.empty());

        //when && then
        assertThatThrownBy(() -> fightEventService.cardDetail(ffeId))
                .isInstanceOf(CustomException.class)
                .hasMessage(BAD_REQUEST_400.getErrorMessage());
    }

    private boolean compareCardStartDateTimeInfo(CardStartDateTimeInfoDto infoDto, CardStartDateTimeInfo info) {
        return infoDto.date().equals(info.getDate()) && infoDto.time().equals(info.getTime());
    }

    private Page<FightEvent> getFightEventPage(int range, Pageable pageable) {
        List<FightEvent> fightEvents = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            FightEvent fightEvent = createUpcomingFightEventWithId(i);
            fightEvents.add(fightEvent);
        }
        return new PageImpl<>(fightEvents, pageable, fightEvents.size());
    }

}