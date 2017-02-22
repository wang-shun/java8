package io.terminus.doctor.common.utils;

import com.google.common.base.MoreObjects;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.exception.InvalidException;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/15
 */

public class RespWithEx<T> extends Response<T> implements Serializable {
    private static final long serialVersionUID = -672798836247859460L;

    private InvalidException exception;

    public InvalidException getException() {
        return exception;
    }

    public void setException(InvalidException exception) {
        this.exception = exception;
        this.setSuccess(false);
        if (exception != null) {
            this.setError(exception.getMessage());
        }
    }

    public static <T> RespWithEx<T> exception(InvalidException exception) {
        RespWithEx<T> resp = new RespWithEx<>();
        resp.setException(exception);
        return resp;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("success", this.isSuccess())
                .add("result", this.getResult())
                .add("error", this.getError())
                .add("exception", exception.toString())
                .omitNullValues()
                .toString();
    }

    public static <T> RespWithEx<T> ok(T data) {
        RespWithEx<T> resp = new RespWithEx<T>();
        resp.setResult(data);
        return resp;
    }

    public static <T> RespWithEx<T> ok() {
        return RespWithEx.ok(null);
    }

    public static <T> RespWithEx<T> fail(String error) {
        RespWithEx<T> resp = new RespWithEx<T>();
        resp.setError(error);
        return resp;
    }
}
