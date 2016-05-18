/*
 * Copyright (c) 2015 杭州端点网络科技有限公司
 */

package io.terminus.doctor.open.util;

import com.google.common.base.Optional;
import io.terminus.common.model.Response;
import io.terminus.pampas.openplatform.exceptions.OPClientException;

import java.util.Collections;

/**
 * @author Effet
 */
public class OPRespHelper {

    public static <T> T or(Response<T> resp, T failValue) {
        return resp.isSuccess() ? resp.getResult() : failValue;
    }

    public static Boolean orFalse(Response<Boolean> resp) {
        return or(resp, Boolean.FALSE);
    }

    public static <T> T orOPEx(Response<T> resp) {
        if (resp.isSuccess()) {
            return resp.getResult();
        }
        throw new OPClientException(resp.getError());
    }

    /**
     * need not to cast
     */
    public static <T, D extends T> Response<T> ok(D data) {
        Response<T> resp = new Response<>();
        resp.setResult(data);
        return resp;
    }

    /**
     * Guava {@code Optional} Helpers
     */
    public static final class Opt {
        public static <T> Response<T> unwrap(Response<Optional<T>> resp, String error) {
            if (resp.isSuccess()) {
                if (resp.getResult().isPresent()) {
                    return Response.ok(resp.getResult().get());
                }
                return Response.fail(error);
            }
            return Response.fail(resp.getError());
        }

        public static <T, D extends T> Response<Optional<T>> of(D data) {
            return Response.ok(Optional.<T>of(data));
        }

        public static <T> Response<Optional<T>> absent() {
            return Response.ok(Optional.<T>absent());
        }

        public static <T, D extends T> Response<Optional<T>> fromNullable(D data) {
            return Response.ok(Optional.<T>fromNullable(data));
        }
    }

    /**
     * Vanilla java {@code Map} Helpers
     */
    public static final class Map {
        public static <K, V> Response<java.util.Map<K, V>> empty() {
            return Response.ok(Collections.<K, V>emptyMap());
        }
    }
}
