package my.mma.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
public class BasicErrorResponse {

    private CustomErrorCode errorCode;
    private HttpStatus status;
    private LocalDateTime timeStamp;

}