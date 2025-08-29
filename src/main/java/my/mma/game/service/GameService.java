package my.mma.game.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.mma.admin.fighter.dto.ChosenGameFighterNamesDto;
import my.mma.admin.fighter.dto.RankersDto;
import my.mma.admin.fighter.dto.RankersDto.RankerDto;
import my.mma.exception.CustomErrorCode;
import my.mma.exception.CustomException;
import my.mma.fighter.entity.Fighter;
import my.mma.fighter.repository.FighterRepository;
import my.mma.game.dto.GameCategory;
import my.mma.game.dto.GameResponse;
import my.mma.game.dto.ImageGameQuestions;
import my.mma.game.dto.NameGameQuestions;
import my.mma.game.dto.NameGameQuestions.NameGameQuestionDto;
import my.mma.game.entity.GameAttempt;
import my.mma.game.repository.GameAttemptRepository;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.global.s3.service.S3ImgService;
import my.mma.user.entity.User;
import my.mma.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static my.mma.game.dto.ImageGameQuestions.*;

@Transactional(readOnly = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {

    private final RedisUtils<RankersDto> rankersRedisUtils;
    private final RedisUtils<ChosenGameFighterNamesDto> adminChosenGameFightersRedisUtils;
    private final FighterRepository fighterRepository;
    private final GameAttemptRepository gameAttemptRepository;
    private final UserRepository userRepository;
    private final S3ImgService s3Service;

    public GameResponse generateGameQuestions(boolean isNormal, boolean isImage) {
        GameResponse gameResponse = new GameResponse();
        List<String> names;
        if (isNormal) {
            RankersDto rankers = rankersRedisUtils.getData("rankers");
            ChosenGameFighterNamesDto chosenFighters = adminChosenGameFightersRedisUtils.getData("chosenFighters");
            Set<String> rankerNames = rankers.getRankerDtos().stream()
                    .map(RankerDto::getName)
                    .collect(Collectors.toSet());
            if (chosenFighters == null)
                names = new ArrayList<>(rankerNames);
            else {
                Set<String> chosenFighterNames = chosenFighters.getNames();
                names = new ArrayList<>(Stream.of(rankerNames, chosenFighterNames)
                        .flatMap(Collection::stream)
                        .toList());
            }
        } else {
            // hard
            names = fighterRepository.findEveryNames();
        }
        if (!isImage) {
            NameGameQuestions nameQuestions = new NameGameQuestions();
            nameQuestions.setGameQuestions(Arrays.stream(GameCategory.values()).map(
                    gameCategory -> generateNameQuestion(gameCategory, names)
            ).toList());
            gameResponse.setNameGameQuestions(nameQuestions);
        } else {
            ImageGameQuestions imageQuestions = generateImageQuestions(names);
            gameResponse.setImageGameQuestions(imageQuestions);
        }
        log.info("game response = {}", gameResponse);
        return gameResponse;
    }

    // gameAttempt 존재 
    public int getGameAttemptCount(String email) {
        User user = extractUserByEmail(email);
        GameAttempt gameAttempt = gameAttemptRepository.findById(user.getId()).orElseGet(
                () ->
                        gameAttemptRepository.save(GameAttempt.builder()
                                .userId(user.getId())
                                .count(10)
                                .expiration(Duration.between(
                                        LocalDateTime.now(),
                                        LocalDate.now().plusDays(1).atStartOfDay()).getSeconds()
                                ).build())
        );
        return gameAttempt.getCount();
    }

    @Transactional
    public int subtractGameAttemptCount(String email) {
        User user = extractUserByEmail(email);
        GameAttempt gameAttempt = gameAttemptRepository.findById(user.getId()).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_EVENT_FOUND_400)
        );
        gameAttempt.setCount(gameAttempt.getCount() - 1);
        gameAttemptRepository.save(gameAttempt);
        return gameAttempt.getCount();
    }

    @Transactional
    public int updatePoint(String email, int newPoint) {
        User user = extractUserByEmail(email);
        user.updatePoint(newPoint);
        return user.getPoint();
    }

    public NameGameQuestionDto generateNameQuestion(GameCategory category, List<String> names) {
        return switch (category) {
            case HEADSHOT -> generateHeadshotQuestion(names);
            case BODY -> generateBodyQuestion(names);
            case NICKNAME -> generateNicknameQuestion(names);
            case RECORD -> generateRecordQuestion(names);
            case RANKING -> generateRankingQuestion();
        };
    }

    private ImageGameQuestions generateImageQuestions(List<String> names) {
        int currentQuestionCnt = 0;
        Collections.shuffle(names);
        ImageGameQuestions imageGameQuestions = new ImageGameQuestions();
        for (String name : names) {
            String headshotUrl = s3Service.generateImgUrlOrNull(
                    "headshot/" + name.replace(' ', '-') + ".png"
            );
            if (headshotUrl != null) {
                if (currentQuestionCnt % 4 == 0) {
                    if (currentQuestionCnt == 20)
                        break;
                    currentQuestionCnt++;
                    imageGameQuestions.getGameQuestions().add(ImageGameQuestionDto.builder()
                            .name(name)
                            .answerImgUrl(headshotUrl)
                            .build());
                } else {
                    imageGameQuestions.getGameQuestions().get((currentQuestionCnt / 4))
                            .getWrongSelection().add(headshotUrl);
                    currentQuestionCnt++;
                }
            }
        }
        return imageGameQuestions;
    }

    private NameGameQuestionDto generateRankingQuestion() {
        RankersDto rankersDto = rankersRedisUtils.getData("rankers");
        if (rankersDto == null)
            return null;
        List<RankerDto> rankerDtos = rankersDto.getRankerDtos();
        Collections.shuffle(rankerDtos);
        RankerDto randomRanker = rankerDtos.get(0);
        return NameGameQuestionDto.builder()
                .answerName(randomRanker.getName())
                .ranking(randomRanker.getRanking())
                .rankingCategory(randomRanker.getCategory())
                .gameCategory(GameCategory.RANKING)
                .wrongSelection(new ArrayList<>(rankerDtos.subList(1, 4).stream().map(RankerDto::getName).toList()))
                .build();
    }

    private NameGameQuestionDto generateRecordQuestion(List<String> names) {
        Collections.shuffle(names);
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Optional<Fighter> fighterOptional = fighterRepository.findByNameAndFightRecordIsNotNull(name);
            if (fighterOptional.isPresent()) {
                Fighter fighter = fighterOptional.get();
                return NameGameQuestionDto.builder()
                        .answerName(fighter.getName())
                        .fightRecord(fighter.getFightRecord())
                        .gameCategory(GameCategory.RECORD)
                        .wrongSelection(new ArrayList<>(names.subList(i + 1, i + 4)))
                        .build();
            }
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR, "record question generation error");
    }

    private NameGameQuestionDto generateNicknameQuestion(List<String> names) {
        Collections.shuffle(names);
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Optional<Fighter> fighterOptional = fighterRepository.findByNameAndNicknameIsNotNull(name);
            if (fighterOptional.isPresent()) {
                Fighter fighter = fighterOptional.get();
                return NameGameQuestionDto.builder()
                        .answerName(fighter.getName())
                        .nickname(fighter.getNickname())
                        .wrongSelection(new ArrayList<>(names.subList(i + 1, i + 4)))
                        .gameCategory(GameCategory.NICKNAME)
                        .build();
            }
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR, "nickname question generation error");
    }

    private NameGameQuestionDto generateBodyQuestion(List<String> names) {
        Collections.shuffle(names);
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String bodyUrl = s3Service.generateImgUrlOrNull(
                    "body/" + name.replace(' ', '-') + ".png"
            );
            if (bodyUrl != null) {
                return NameGameQuestionDto.builder()
                        .answerName(name)
                        .bodyUrl(bodyUrl)
                        .wrongSelection(new ArrayList<>(names.subList(i + 1, i + 4)))
                        .gameCategory(GameCategory.BODY)
                        .build();
            }
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR, "body question generation error");
    }

    private NameGameQuestionDto generateHeadshotQuestion(List<String> names) {
        Collections.shuffle(names);
        StringBuilder sb = new StringBuilder();
        if (sb.isEmpty()) {

        }
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String headshotUrl = s3Service.generateImgUrlOrNull(
                    "headshot/" + name.replace(' ', '-') + ".png"
            );
            if (headshotUrl != null) {
                return NameGameQuestionDto.builder()
                        .answerName(name)
                        .headshotUrl(headshotUrl)
                        .wrongSelection(new ArrayList<>(names.subList(i + 1, i + 4)))
                        .gameCategory(GameCategory.HEADSHOT)
                        .build();
            }
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR, "headshot question generation error");
    }

    private User extractUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(CustomErrorCode.NO_SUCH_USER_CONFIGURED_400)
        );
    }

}
