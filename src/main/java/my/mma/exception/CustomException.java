package my.mma.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private final CustomErrorCode errorCode;

    public CustomException(CustomErrorCode errorCode){
        super(errorCode.getErrorMessage());
        this.errorCode = errorCode;
    }

    public CustomException(CustomErrorCode errorCode, String errorMessage){
        super(errorMessage);
        this.errorCode = errorCode;
    }

}