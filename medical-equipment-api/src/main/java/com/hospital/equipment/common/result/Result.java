package com.hospital.equipment.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一返回结果体
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    public static <T> Result<T> error(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(msg);
        r.setData(data);
        return r;
    }
}
