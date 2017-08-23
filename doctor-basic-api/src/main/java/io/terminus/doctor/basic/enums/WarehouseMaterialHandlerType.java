package io.terminus.doctor.basic.enums;

import lombok.Getter;

import java.util.stream.Stream;

/**
 * Created by sunbo@terminus.io on 2017/8/10.
 */
public enum WarehouseMaterialHandlerType {

    /**
     * 未知
     */
    UNKNOWN(0),

    /**
     * 入库
     */
    IN(1),


    /**
     * 出库
     */
    OUT(2),


    /**
     * 调拨
     */
    TRANSFER(3),


    /**
     * 盘点
     */
    INVENTORY(4),


    /**
     * 配方生产
     */
    FORMULA(5);


    @Getter
    private int value;


    WarehouseMaterialHandlerType(int value) {
        this.value = value;
    }


    public static WarehouseMaterialHandlerType fromValue(int value) {
        return Stream.of(WarehouseMaterialHandlerType.values()).parallel().filter(t -> t.value == value).findFirst().orElse(UNKNOWN);
    }

}
