package my.mma.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@Getter
public enum CustomErrorCode {

    //kakao
    KAKAO_API_CALL_FAILED("인가되지 않은 code 요청",BAD_REQUEST),
    NO_KAKAO_CODE_CONFIGURED("카카오 인가 코드 발급 필요",BAD_REQUEST),
    //user
    NO_SUCH_USER_CONFIGURED_400("해당 회원 찾지 못함", BAD_REQUEST),
    NO_SUCH_USER_CONFIGURED_500("해당 회원 찾지 못함", INTERNAL_SERVER_ERROR),
    NO_SUCH_EMAIL_CONFIGURED_500("이메일 계정 찾지 못함",INTERNAL_SERVER_ERROR),
    DUPLICATED_EMAIL_400("중복된 이메일 계정",BAD_REQUEST),
    DUPLICATED_NICKNAME_400("중복된 닉네임",BAD_REQUEST),
    DUPLICATED_PASSWORD_400("중복된 비밀 번호",BAD_REQUEST),
    //authentication, authorization
    JWT_TOKEN_EXPIRED("토큰 기간 만료",UNAUTHORIZED),
    UNSUPPORTED_JWT("잘못된 토큰",UNAUTHORIZED),
    SERVER_ERROR("서버 에러", INTERNAL_SERVER_ERROR);

    private final String errorMessage;
    private final HttpStatus status;

}