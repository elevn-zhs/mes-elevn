package com.elevn.mes.common.handler;

import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLSyntaxErrorException;

@RestControllerAdvice // 全局异常处理器
public class GlobalExceptionHandler {
    //日志输出的工具对象
    // 不要忘记导包
    // import org.slf4j.Logger;
    //import org.slf4j.LoggerFactory;
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 当程序中任何controller中的方法出现了BusinessException都会执行这里的方法，最终返回这里的Result
    @ExceptionHandler(BusinessException.class) // 专门处理自定义异常
    public Result businessExceptionHandle(BusinessException e){
        log.error(e.getMessage());
        e.printStackTrace();// 异常信息栈输出到控制台，纯粹是为了方便观察
        // 将exception对象转换为一个Result响应给客户端
        return Result.error(e.getCode(),e.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result methodArgumentNotValidExceptionHandle(MethodArgumentNotValidException e){
        log.error("参数校验异常");
        e.printStackTrace();// 异常信息栈输出到控制台，纯粹是为了方便观察
        return Result.error(e.getFieldError().getDefaultMessage());
    }

    // 兜底的异常处理
    @ExceptionHandler(Exception.class)
    public Result exceptionHandle(Exception e){
        log.error(e.getMessage());
        e.printStackTrace();// 异常信息栈输出到控制台，纯粹是为了方便观察
        return Result.error(e.getMessage());
    }

}
