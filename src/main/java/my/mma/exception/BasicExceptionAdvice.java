package my.mma.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

import static my.mma.exception.CustomErrorCode.*;

@RestControllerAdvice
@Slf4j
public class BasicExceptionAdvice {

    @ExceptionHandler
    public ResponseEntity<BasicErrorResponse> handleCustomException(
            CustomException e
    ) {
        if(e.getMessage() != null)
            log.error("ex = {}, detail error message = {}",e.getErrorCode().getErrorMessage(),e.getMessage());
        else
            log.error("ex = {}", e.getErrorCode().getErrorMessage());
        BasicErrorResponse response = BasicErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .status(e.getErrorCode().getStatus())
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler
    public ResponseEntity<BasicErrorResponse> handleHttpMessageConvertException(
            HttpMessageConversionException e
    ) {
        log.error("ex = ", e);
        BasicErrorResponse response = BasicErrorResponse.builder()
                .errorCode(BAD_REQUEST_400)
                .status(BAD_REQUEST_400.getStatus())
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    //valid
    @ExceptionHandler
    public ResponseEntity<BasicErrorResponse> handleMethodArgumentException(
            MethodArgumentNotValidException e
    ) {
        log.error("ex = {}", e.getMessage());
        BasicErrorResponse response = BasicErrorResponse.builder()
                .errorCode(VALIDATION_FAILED_400)
                .status(VALIDATION_FAILED_400.getStatus())
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public ResponseEntity<BasicErrorResponse> handleNoResource(
            Exception e
    ) {
        log.error("ex = {}", e.getMessage());
        BasicErrorResponse response = BasicErrorResponse.builder()
                .errorCode(URL_NOT_FOUND)
                .status(URL_NOT_FOUND.getStatus())
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    @ExceptionHandler
    public ResponseEntity<BasicErrorResponse> handleException(
            Exception e
    ) {
        log.error("ex = {}", e.getMessage());
        log.error("detail message = ", e);
        BasicErrorResponse response = BasicErrorResponse.builder()
                .errorCode(SERVER_ERROR_500)
                .status(SERVER_ERROR_500.getStatus())
                .timeStamp(LocalDateTime.now())
                .build();
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }


}

