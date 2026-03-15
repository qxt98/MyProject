package com.smartpharma.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：将业务异常统一转为 Result 结构并返回 HTTP 200，前端可统一根据 code 与 message 处理。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务校验失败（如登录失败、用户名已存在、库存不足等） */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<?>> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage() != null ? e.getMessage() : "操作失败";
        return ResponseEntity.status(HttpStatus.OK).body(Result.fail(message));
    }

    /** 其他未捕获异常，避免直接返回 500 给前端 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "服务器内部错误";
        return ResponseEntity.status(HttpStatus.OK).body(Result.fail(500, message));
    }
}
