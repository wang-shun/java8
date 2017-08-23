package io.terminus.doctor.basic.enums;

import io.terminus.common.exception.ServiceException;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * Created by sunbo@terminus.io on 2017/8/24.
 */
public enum WarehousePurchaseHandleFlag {

    /**
     * 出库完
     */
    OUT_FINISH(0),

    /**
     * 未出库完
     */
    NOT_OUT_FINISH(1);

    @Getter
    private int value;

    WarehousePurchaseHandleFlag(int value) {
        this.value = value;
    }

    public static WarehousePurchaseHandleFlag fromValue(int value) {
        return Stream.of(WarehousePurchaseHandleFlag.values())
                .parallel()
                .filter(t -> t.getValue() == value)
                .findFirst()
                .orElseThrow(() -> new ServiceException("unknown.warehouse.purchase.handle.flag"));
    }
}
