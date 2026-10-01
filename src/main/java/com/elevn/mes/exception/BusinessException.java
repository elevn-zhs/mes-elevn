package com.elevn.mes.exception;

public class BusinessException extends RuntimeException{
    private int code = 400;    // 业务错误码（400 类：参数/业务问题）

    public BusinessException() {
    }

    public BusinessException(String message) {
        super(message);
    }
    public BusinessException(String message,int code) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public int getCode(){
        return this.code;
    }
    public void setCode(int code){
        this.code = code;
    }
}
