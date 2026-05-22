package com.breathchain.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException ex) {
        log.warn("business error: code={}, msg={}", ex.getCode(), ex.getMessage());
        return Result.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidation(Exception ex) {
        String msg;
        if (ex instanceof MethodArgumentNotValidException me) {
            msg = me.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        } else if (ex instanceof BindException be) {
            msg = be.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
        } else {
            msg = ex.getMessage();
        }
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Void>> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.fail(ResultCode.UNAUTHORIZED.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.fail(ResultCode.FORBIDDEN));
    }

    @ExceptionHandler({DuplicateKeyException.class, SQLIntegrityConstraintViolationException.class})
    public Result<Void> handleDuplicateKey(Exception ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();
        String friendly = "数据重复，无法保存";
        // 解析 MySQL 错误信息提取具体字段
        if (msg.contains("uk_username") || msg.toLowerCase().contains("for key 'sys_user.uk_username")) {
            friendly = "用户名已被占用，请换一个";
        } else if (msg.contains("uk_license")) {
            friendly = "执业医师证书号已被注册";
        } else if (msg.contains("uk_user_id")) {
            friendly = "该用户已注册过医生资质";
        } else if (msg.contains("uk_task_user")) {
            friendly = "该任务已经分配给这个患者了";
        }
        log.warn("duplicate key: {}", msg);
        return Result.fail(ResultCode.BAD_REQUEST.getCode(), friendly);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleAll(Exception ex) {
        log.error("unhandled exception", ex);
        return Result.fail(ResultCode.INTERNAL_ERROR.getCode(), ex.getMessage());
    }
}
