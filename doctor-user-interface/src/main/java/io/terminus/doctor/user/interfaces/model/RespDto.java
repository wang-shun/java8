package io.terminus.doctor.user.interfaces.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RespDto<T> implements Serializable {
    private static final long serialVersionUID = -6586901193547572760L;

    private boolean success; //调用是否成功

    private T result;       // 如果success = true,则通过result可以获得调用结果

    private String error;   //如果success = false,则通过error可以查看错误信息

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.success = true;
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.success = false;
        this.error = error;
    }

    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put("success", success);
        map.put("result", result);
        map.put("error", error);
        return this.getClass() + map.toString();
    }

    public static <T> RespDto<T> ok(T data) {
        RespDto<T> resp = new RespDto<>();
        resp.setResult(data);
        return resp;
    }

    public static <T> RespDto<T> ok() {
        return RespDto.ok(null);
    }

    public static <T> RespDto<T> fail(String error) {
        RespDto<T> resp = new RespDto<>();
        resp.setError(error);
        return resp;
    }
}
