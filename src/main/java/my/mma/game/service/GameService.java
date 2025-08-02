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
import my.mma.game.dto.GameQuestionsDto;
import my.mma.game.dto.GameQuestionsDto.GameQuestionDto;
import my.mma.global.redis.utils.RedisUtils;
import my.mma.global.s3.service.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Transactional(readOnly = true)
@Service
@Slf4j
@RequiredArgsConstructor
public class GameService {

    private final RedisUtils<RankersDto> rankersRedisUtils;
    private final RedisUtils<ChosenGameFighterNamesDto> adminChosenGameFightersRedisUtils;
    private final FighterRepository fighterRepository;
    private final S3Service s3Service;

    public GameQuestionsDto generateGameQuestions(boolean isNormal) {
        List<String> names;
        GameQuestionsDto gameQuestionsDto = new GameQuestionsDto();
        if (isNormal) {
            RankersDto rankers = rankersRedisUtils.getData("rankers");
            ChosenGameFighterNamesDto chosenFighters = adminChosenGameFightersRedisUtils.getData("chosenFighters");
            List<String> rankerNames = rankers.getRankerDtos().stream()
                    .map(RankerDto::getName)
                    .toList();
            List<String> chosenFighterNames = chosenFighters.getNames();
            names = new ArrayList<>(Stream.of(rankerNames, chosenFighterNames)
                    .flatMap(Collection::stream)
                    .toList());
        } else {
            // hard
            names = fighterRepository.findEveryNames();
        }
        gameQuestionsDto.setGameQuestions(Arrays.stream(GameCategory.values()).map(
                gameCategory -> generateQuestion(gameCategory, names)
        ).toList());
        return gameQuestionsDto;
    }

    public GameQuestionDto generateQuestion(GameCategory category, List<String> names) {
        switch (category) {
            case HEADSHOT:
                return generateHeadshotQuestion(names);
            case BODY:
                return generateBodyQuestion(names);
            case NICKNAME:
                return generateNicknameQuestion(names);
            case RECORD:
                return generateRecordQuestion(names);
            case RANKING:
                return generateRankingQuestion();
            default:
                throw new IllegalArgumentException("Unknown game category");
        }
    }

    private GameQuestionDto generateRankingQuestion() {
        RankersDto rankersDto = rankersRedisUtils.getData("rankers");
        if (rankersDto == null)
            return null;
        List<RankerDto> rankerDtos = rankersDto.getRankerDtos();
        Collections.shuffle(rankerDtos);
        RankerDto randomRanker = rankerDtos.get(0);
        return GameQuestionDto.builder()
                .name(randomRanker.getName())
                .ranking(randomRanker.getRanking())
                .rankingCategory(randomRanker.getCategory())
                .gameCategory(GameCategory.RANKING)
                .namesForSelection(new ArrayList<>(rankerDtos.subList(1, 3).stream().map(RankerDto::getName).toList()))
                .build();
    }

    private GameQuestionDto generateRecordQuestion(List<String> names) {
        Collections.shuffle(names);
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Optional<Fighter> fighterOptional = fighterRepository.findByNameAndFightRecordIsNotNull(name);
            if (fighterOptional.isPresent()) {
                Fighter fighter = fighterOptional.get();
                return GameQuestionDto.builder()
                        .name(fighter.getName())
                        .fightRecord(fighter.getFightRecord())
                        .gameCategory(GameCategory.RECORD)
                        .namesForSelection(new ArrayList<>(names.subList(i, i + 2)))
                        .build();
            }
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR, "record question generation error");
    }

    private GameQuestionDto generateNicknameQuestion(List<String> names) {
        Collections.shuffle(names);
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            Optional<Fighter> fighterOptional = fighterRepository.findByNameAndNicknameIsNotNull(name);
            if (fighterOptional.isPresent()) {
                Fighter fighter = fighterOptional.get();
                return GameQuestionDto.builder()
                        .name(fighter.getName())
                        .nickname(fighter.getNickname())
                        .namesForSelection(new ArrayList<>(names.subList(i, i + 2)))
                        .gameCategory(GameCategory.NICKNAME)
                        .build();
            }
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR, "nickname question generation error");
    }

    private GameQuestionDto generateBodyQuestion(List<String> names) {
        Collections.shuffle(names);
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String bodyUrl = s3Service.generateImgUrlOrNull(
                    "body/" + name.replace(' ', '-') + ".png"
            );
            if (bodyUrl != null) {
                return GameQuestionDto.builder()
                        .name(name)
                        .bodyUrl(bodyUrl)
                        .namesForSelection(new ArrayList<>(names.subList(i, i + 2)))
                        .gameCategory(GameCategory.BODY)
                        .build();
            }
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR, "body question generation error");
    }

    private GameQuestionDto generateHeadshotQuestion(List<String> names) {
        Collections.shuffle(names);
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String headshotUrl = s3Service.generateImgUrlOrNull(
                    "headshot/" + name.replace(' ', '-') + ".png"
            );
            if (headshotUrl != null) {
                return GameQuestionDto.builder()
                        .name(name)
                        .headshotUrl(headshotUrl)
                        .namesForSelection(new ArrayList<>(names.subList(i, i + 2)))
                        .gameCategory(GameCategory.HEADSHOT)
                        .build();
            }
        }
        throw new CustomException(CustomErrorCode.SERVER_ERROR, "headshot question generation error");
    }

}
