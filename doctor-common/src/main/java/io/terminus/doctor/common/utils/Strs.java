package io.terminus.doctor.common.utils;

import com.google.common.base.Optional;
import io.terminus.common.utils.Params;

import javax.annotation.Nullable;

/**
 * @author Effet
 */
public class Strs {

    public static Optional<Long> parseLong(@Nullable Object obj) {
        String str = Params.trimToNull(obj);
        return str == null ? Optional.<Long>absent(): Optional.of(Long.parseLong(str));
    }

    public static Optional<Integer> parseInt(@Nullable Object obj) {
        String str = Params.trimToNull(obj);
        return str == null ? Optional.<Integer>absent(): Optional.of(Integer.parseInt(str));
    }
}
