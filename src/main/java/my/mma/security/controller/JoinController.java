package my.mma.security.controller;

import lombok.RequiredArgsConstructor;
import my.mma.security.service.JoinService;
import my.mma.security.oauth2.dto.JoinDto;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JoinController{

    private final JoinService joinService;

    @PostMapping("/join")
    public String join(@Validated @RequestBody JoinDto joinDto){
        joinService.joinUser(joinDto);
        return "ok";
    }

}