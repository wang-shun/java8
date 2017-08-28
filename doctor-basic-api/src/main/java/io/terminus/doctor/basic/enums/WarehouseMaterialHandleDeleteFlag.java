package io.terminus.doctor.basic.enums;

import io.terminus.common.exception.ServiceException;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Created by sunbo@terminus.io on 2017/8/28.
 */
public enum WarehouseMaterialHandleDeleteFlag {

    /**
     *
     */
    NOT_DELETE(0),

    /**
     * 未出库完
     */
    DELETE(1);

    @Getter
    private int value;

    WarehouseMaterialHandleDeleteFlag(int value) {
        this.value = value;
    }

    public static WarehouseMaterialHandleDeleteFlag fromValue(int value) {
        return Stream.of(WarehouseMaterialHandleDeleteFlag.values())
                .parallel()
                .filter(t -> t.getValue() == value)
                .findFirst()
                .orElseThrow(() -> new ServiceException("unknown.warehouse.purchase.handle.flag"));
    }
}
