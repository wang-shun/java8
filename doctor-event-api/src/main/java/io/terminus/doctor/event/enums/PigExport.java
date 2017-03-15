package io.terminus.doctor.event.enums;

import com.google.common.base.Objects;
import lombok.Getter;

/**
 * Created by terminus on 2017/3/14.
 */
public enum PigExport {

    IN_FARM_EVENT(1, "猪的进场事件"),
    SEMEN_BOAR(2, "公猪采精");

    @Getter
    private final long id;
    @Getter
    private final String name;

    PigExport(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static PigExport from(String name) {

        for (PigExport type : PigExport.values()) {
            if (Objects.equal(type.name, name)) {
                return type;
            }
        }
        return null;
    }
}
