package io.terminus.doctor.basic.enums;

import lombok.Getter;

import java.util.stream.Stream;

/**
 * Created by sunbo@terminus.io on 2017/8/10.
 */
public enum WarehouseMaterialHandleType {

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
     * 调拨（无用）
     */
    TRANSFER(3),


    /**
     * 盘点（无用）
     */
    INVENTORY(4),

    /**
     * 盘盈
     */
    INVENTORY_PROFIT(7),

    /**
     * 盘亏
     */
    INVENTORY_DEFICIT(8),


    /**
     * 调入
     */
    TRANSFER_IN(9),

    /**
     * 调出
     */
    TRANSFER_OUT(10),


    /**
     * 配方生产
     */
    FORMULA(5);


    @Getter
    private int value;


    WarehouseMaterialHandleType(int value) {
        this.value = value;
    }


    public static WarehouseMaterialHandleType fromValue(int value) {
        return Stream.of(WarehouseMaterialHandleType.values()).parallel().filter(t -> t.value == value).findFirst().orElse(UNKNOWN);
    }

}
