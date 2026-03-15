package com.smartpharma.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应封装
 * 所有 REST 接口均返回该结构，便于前端统一处理：code=200 表示成功，data 为业务数据；非 200 表示失败，message 为错误说明。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    /** 状态码：200 成功，其他为业务或系统错误码 */
    private int code;
    /** 提示信息，失败时描述原因 */
    private String message;
    /** 业务数据，可为分页对象、实体或 null */
    private T data;

    /** 成功并返回数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /** 成功无数据（如删除、审批通过等） */
    public static <T> Result<T> ok() {
        return ok(null);
    }

    /** 失败，指定状态码与消息 */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 失败，默认 400，仅指定消息 */
    public static <T> Result<T> fail(String message) {
        return fail(400, message);
    }
}
