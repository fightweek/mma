package my.mma.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private final CustomErrorCode errorCode;
    private String errorMessage;

    public CustomException(CustomErrorCode errorCode){
        this.errorCode = errorCode;
    }

    public CustomException(CustomErrorCode errorCode, String errorMessage){
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }


}