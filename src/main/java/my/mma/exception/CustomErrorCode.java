package my.mma.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@Getter
public enum CustomErrorCode {

    //kakao
    KAKAO_API_CALL_FAILED("인가되지 않은 code 요청", BAD_REQUEST),
    NO_KAKAO_CODE_CONFIGURED("카카오 인가 코드 발급 필요", BAD_REQUEST),
    //user
    NO_SUCH_USER_CONFIGURED_400("해당 회원 찾지 못함", BAD_REQUEST),
    NO_SUCH_EMAIL_CONFIGURED_400("이메일 계정 찾지 못함", BAD_REQUEST),
    // 회원가입할 때
    DUPLICATED_EMAIL_400("중복된 이메일 계정", BAD_REQUEST),
    WITHDRAWN_USER_403("탈퇴한 사용자", FORBIDDEN),
    // 로그인 시도할 때
    DUPLICATED_EMAIL_403("중복된 이메일 계정", FORBIDDEN),
    DUPLICATED_NICKNAME_400("중복된 닉네임", BAD_REQUEST),
    DUPLICATED_PASSWORD_400("중복된 비밀 번호", BAD_REQUEST),
    //authentication, authorization
    JWT_TOKEN_EXPIRED("토큰 기간 만료", UNAUTHORIZED),
    UNSUPPORTED_JWT("잘못된 토큰", UNAUTHORIZED),
    SERVER_ERROR_500("서버 에러", HttpStatus.INTERNAL_SERVER_ERROR),
    SOCIAL_TOKEN_VERIFY_FAILED_400("소셜 로그인 토큰 검증 실패", BAD_REQUEST),
    // event
    NO_SUCH_EVENT_FOUND_400("이벤트 찾지 못함", BAD_REQUEST),
    // fighter
    NO_SUCH_FIGHTER_CONFIGURED_400("No such fighter found", BAD_REQUEST),
    // global
    VALIDATION_FAILED_400("Java bean validation failed", BAD_REQUEST),
    BAD_REQUEST_400("잘못된 요청", BAD_REQUEST),
    URL_NOT_FOUND("해당 요청 경로 찾을 수 없음", NOT_FOUND);

    private final String errorMessage;
    private final HttpStatus status;

}