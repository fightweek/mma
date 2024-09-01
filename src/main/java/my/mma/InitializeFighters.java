package my.mma;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import my.mma.fighter.entity.*;
import my.mma.fighter.repository.FightEventRepository;
import my.mma.fighter.repository.FighterRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@Transactional(readOnly = true)
public class InitializeFighters {

    @Value("${rapid-api.mma-stats-api.uri}")
    private String fightsByYearURI;

    @Value("${rapid-api.mma-stats-api.host}")
    private String fightsByYearHost;

    @Value("${rapid-api.ufc-fighters.uri}")
    private String ufcFightersURI;

    @Value("${rapid-api.ufc-fighters.host}")
    private String ufcFightersHost;

    @Value("${rapid-api.key}")
    private String apiKey;

    private final DateTimeFormatter dateTimeFormatter;
    private final FighterRepository fighterRepository;
    private final FightEventRepository fightEventRepository;
    private final RestTemplate restTemplate;

    public InitializeFighters(FighterRepository fighterRepository, RestTemplateBuilder restTemplateBuilder,
                              FightEventRepository fightEventRepository) {
        this.fighterRepository = fighterRepository;
        this.restTemplate = restTemplateBuilder.build();
        this.fightEventRepository = fightEventRepository;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM_dd_yyyy",Locale.ENGLISH);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void fighterInit() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-rapidapi-host",ufcFightersHost);
        headers.add("x-rapidapi-key",apiKey);
        HttpEntity httpEntity = new HttpEntity(headers);
        String newURI = String.format("%s/%c",ufcFightersURI, 'a');
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(newURI)
                .queryParam("limit", 3000);
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, httpEntity, String.class);
        processFighterData(response.getBody());
        eventInit();
    }

    @Transactional
    public void processFighterData(String body) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(body);
        for (JsonNode fighterNode : rootNode) {
            String weightClass = fighterNode.path("weight_class").asText(null);
            Arrays.stream(Division.values())
                    .filter(d -> d.name().equalsIgnoreCase(weightClass))
                    .findFirst().ifPresent(division ->{
                        String full_name = fighterNode.path("first_name").asText(null)
                                +' '+fighterNode.path("last_name").asText(null);
                        if(fighterRepository.findByName(full_name).isEmpty())
                            fighterRepository.save(Fighter.builder()
                                    .name(full_name)
                                    .fightRecord(FightRecord.builder()
                                            .win(fighterNode.path("wins").asInt())
                                            .draw(fighterNode.path("draws").asInt())
                                            .loss(fighterNode.path("losses").asInt())
                                            .build())
                                    .nickname(fighterNode.path("nickname").asText(null))
                                    .division(division)
                                    .build());
                    });
        }
    }

    @Transactional
    public void eventInit() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("x-rapidapi-host",fightsByYearHost);
        headers.add("x-rapidapi-key",apiKey);
        HttpEntity httpEntity = new HttpEntity(headers);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(fightsByYearURI)
                .queryParam("year", 2024);
        ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, httpEntity, String.class);
        saveEvents(response.getBody());
    }


    @Transactional
    public void saveEvents(String jsonBody) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonBody);

        // Iterate over the dates (e.g., "April_06_2024", "April_13_2024")
        Iterator<String> fieldNames = rootNode.fieldNames();
        while (fieldNames.hasNext()) {
            String dateKey = fieldNames.next();
            //dateKey=April_06_2024
            LocalDate parsedDate = extractDate(dateKey);
            log.info("parsedDate={}",parsedDate);
            JsonNode dateNode = rootNode.get(dateKey);

            JsonNode firstMatchUpNode = dateNode.get(0).get("matchup");
            String fighter1Name = firstMatchUpNode.get(0).asText();
            String fighter2Name = firstMatchUpNode.get(1).asText();

            Optional<Fighter> findMainFighter1 = fighterRepository.findByNameIgnoreCaseAndSpaces(fighter1Name);
            Optional<Fighter> findMainFighter2 = fighterRepository.findByNameIgnoreCaseAndSpaces(fighter2Name);

            if(findMainFighter1.isPresent() && findMainFighter2.isPresent()){
                boolean eventExists = fightEventRepository.existsByFightDateAndMainFighter1AndMainFighter2(
                        parsedDate, findMainFighter1.get(), findMainFighter2.get());
                if (eventExists) {
                    log.info("Duplicate FightEvent detected for date: {} with fighters: {} and {}",
                            parsedDate, fighter1Name, fighter2Name);
                    continue;
                }
                FightEvent fightEvent = FightEvent.builder()
                        .mainFighter1(findMainFighter1.get())
                        .mainFighter2(findMainFighter2.get())
                        .fightDate(parsedDate)
                        .fighterFightEvents(new ArrayList<>())
                        .build();
                for (int i=0;i<dateNode.size();i++) {
                    //matchUpNode={"matchup":["Brendan Allen","Chris Curtis"]}
                    JsonNode matchUpNode = dateNode.get(i).get("matchup");
                    if(matchUpNode.get(0) != null) {
                        fighter1Name = matchUpNode.get(0).asText();
                        fighter2Name = matchUpNode.get(1).asText();

                        Optional<Fighter> findFighter1 = fighterRepository.findByNameIgnoreCaseAndSpaces(fighter1Name);
                        Optional<Fighter> findFighter2 = fighterRepository.findByNameIgnoreCaseAndSpaces(fighter2Name);
                        if (findFighter1.isPresent() && findFighter2.isPresent()) {
                            fightEvent.addFighterFightEvent(FighterFightEvent.builder()
                                    .fightEvent(fightEvent)
                                    .fighter1(findFighter1.get())
                                    .fighter2(findFighter2.get())
                                    .isEnded(parsedDate.isBefore(LocalDate.now()))
                                    .build());
                        }
                    }
                }
                fightEventRepository.save(fightEvent);
            }
        }
    }

    private LocalDate extractDate(String dateKey) {
        try {
            return LocalDate.parse(dateKey,dateTimeFormatter);
        } catch (Exception e) {
//            throw new CustomException(CustomErrorCode.DATETIME_PARSE_ERROR_500);
            throw new RuntimeException("date time parse error");
        }
    }
}
