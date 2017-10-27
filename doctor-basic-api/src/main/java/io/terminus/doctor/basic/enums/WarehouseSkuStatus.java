package io.terminus.doctor.basic.enums;

import io.terminus.common.exception.ServiceException;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

/**
 * Created by sunbo@terminus.io on 2017/10/26.
 */
public enum WarehouseSkuStatus {


    /**
     * 正常
     */
    NORMAL(0),

    /**
     * 禁用
     */
    FORBIDDEN(1);

    @Getter
    private int value;


    WarehouseSkuStatus(int value) {
        this.value = value;
    }

    public static WarehouseSkuStatus get(int value) {
        return Arrays.stream(WarehouseSkuStatus.values())
                .filter(s -> s.value == value)
                .findAny()
                .orElseThrow(() -> new ServiceException("warehouse.sku.status.not.support"));
    }

}
