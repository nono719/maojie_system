package com.breathchain.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(0, "OK"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码 1xxx
    USERNAME_EXISTS(1001, "用户名已存在"),
    USER_NOT_FOUND(1002, "用户不存在"),
    WRONG_PASSWORD(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账户已停用"),

    TASK_NOT_FOUND(2001, "训练任务不存在"),
    TASK_NOT_ASSIGNED(2002, "尚未分配该任务"),

    TRAINING_RECORD_NOT_FOUND(3001, "训练记录不存在"),

    BLOCKCHAIN_ERROR(4001, "区块链交互失败"),
    HASH_VERIFY_FAILED(4002, "数据哈希校验失败");

    private final int code;
    private final String message;
}
