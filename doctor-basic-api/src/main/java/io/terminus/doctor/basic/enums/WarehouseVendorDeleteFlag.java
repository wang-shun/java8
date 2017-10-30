package io.terminus.doctor.basic.enums;

import io.terminus.common.exception.ServiceException;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by sunbo@terminus.io on 2017/10/30.
 */
public enum WarehouseVendorDeleteFlag {

    /**
     * 正常
     */
    NORMAL(0),

    /**
     * 删除
     */
    DELETE(1);

    @Getter
    private int value;


    WarehouseVendorDeleteFlag(int value) {
        this.value = value;
    }

    public static WarehouseVendorDeleteFlag get(int value) {
        return Arrays.stream(WarehouseVendorDeleteFlag.values())
                .filter(s -> s.value == value)
                .findAny()
                .orElseThrow(() -> new ServiceException("warehouse.vendor.delete.flag.not.support"));
    }
}
