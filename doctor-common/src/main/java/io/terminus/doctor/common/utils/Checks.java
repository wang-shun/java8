package io.terminus.doctor.common.utils;

import io.terminus.common.exception.ServiceException;

import javax.annotation.Nullable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/15
 */

public final class Checks {

    public static <T> T checkNotNull(T reference, @Nullable Object errorMessage) {
        if (reference == null) {
            throw new ServiceException(String.valueOf(errorMessage));
        }
        return reference;
    }
}
