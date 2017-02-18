package io.terminus.doctor.common.utils;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.exception.InvalidException;

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

    public static void expectTrue(boolean b, String errorMessage, Object... objects) {
        expectTrue(b, false, null, errorMessage, objects);
    }

    public static void expectTrue(boolean b, boolean isBatchEvent, String attach, String errorMessage, Object... objects) {
        if (!b) {
            throw new InvalidException(isBatchEvent, errorMessage, attach, objects);
        }
    }

    public static <T> T expectNotNull(T reference, String errorMessage, Object... params) {
        if(reference == null) {
            throw new InvalidException(false, errorMessage, null, params);
        } else {
            return reference;
        }
    }
}
