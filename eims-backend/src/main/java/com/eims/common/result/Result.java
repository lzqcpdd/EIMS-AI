package com.eims.common.result;

import lombok.Data;

import java.util.UUID;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;
    private String traceId;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        r.traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String message) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.message = message;
        r.traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        return r;
    }
}
