/*
 * Copyright (c) 2015 杭州端点网络科技有限公司
 */

package io.terminus.doctor.common.utils;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.exception.InvalidException;

import static io.terminus.common.utils.Arguments.notNull;

/**
 * @author Effet
 */
public class RespWithExHelper {

    public static <T> T or(RespWithEx<T> resp, T failValue) {
        return resp.isSuccess() ? resp.getResult() : failValue;
    }

    public static <T> T or500(RespWithEx<T> resp) {
        if (resp.isSuccess()) {
            return resp.getResult();
        }
        throw new JsonResponseException(500, resp.getError());
    }

    public static <T> T orServEx(RespWithEx<T> resp) {
        if (resp.isSuccess()) {
            return resp.getResult();
        }
        throw new ServiceException(resp.getError());
    }

    public static <T> T orInvalid(RespWithEx<T> resp) {
        if (resp.isSuccess()) {
            return resp.getResult();
        }
        if (notNull(resp.getException())) {
            throw resp.getException();
        }
        throw new InvalidException(resp.getError());
    }
}
