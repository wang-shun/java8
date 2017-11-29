package io.terminus.doctor.basic.enums;

import io.terminus.common.exception.ServiceException;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * 物料领用类型
 * Created by sunbo@terminus.io on 2017/11/28.
 */
public enum WarehouseMaterialApplyType {


    /**
     * 猪舍领用
     */
    BARN(0),
    /**
     * 猪群领用
     */
    GROUP(1),

    /**
     * 母猪领用
     */
    SOW(2);


    @Getter
    private int value;

    WarehouseMaterialApplyType(int value) {
        this.value = value;
    }


    public static WarehouseMaterialApplyType fromValue(int value) {
        return Stream.of(WarehouseMaterialApplyType.values())
                .filter(t -> t.value == value)
                .findAny()
                .orElseThrow(() -> new ServiceException("unknown.warehouse.material.apply.type"));
    }


}
