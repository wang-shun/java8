package io.terminus.doctor.basic.enums;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
     * 调拨
     */
    TRANSFER(3),


    /**
     * 盘点
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
     * 配方生产（无用）
     */
    FORMULA(5),

    /**
     * 配方生产导致的入库，饲料
     */
    FORMULA_IN(11),

    /**
     * 配方生产导致的出库，药品和原料
     */
    FORMULA_OUT(12);


    @Getter
    private int value;


    WarehouseMaterialHandleType(int value) {
        this.value = value;
    }


    public static WarehouseMaterialHandleType fromValue(int value) {
        return Stream.of(WarehouseMaterialHandleType.values()).parallel().filter(t -> t.value == value).findFirst().orElseThrow(() -> new ServiceException("unknown.warehouse.material.handle.flag"));
    }

    public static boolean isBigOut(int value) {
        return WarehouseMaterialHandleType.OUT.getValue() == value
                || WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue() == value
                || WarehouseMaterialHandleType.TRANSFER_OUT.getValue() == value
                || WarehouseMaterialHandleType.FORMULA_OUT.getValue() == value;
    }

    public static boolean isBigIn(int value) {
        return WarehouseMaterialHandleType.IN.getValue() == value
                || WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue() == value
                || WarehouseMaterialHandleType.TRANSFER_IN.getValue() == value
                || WarehouseMaterialHandleType.FORMULA_IN.getValue() == value;
    }

    public static List<Integer> getGroupType(Integer type) {
        if (null == type)
            return Collections.emptyList();

        List<Integer> types = new ArrayList<>();

        switch (type) {
            case 1:
                types.add(WarehouseMaterialHandleType.IN.getValue());
//                types.add(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
//                types.add(WarehouseMaterialHandleType.TRANSFER_IN.getValue());
                break;
            case 2:
                types.add(WarehouseMaterialHandleType.OUT.getValue());
//                types.add(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
//                types.add(WarehouseMaterialHandleType.TRANSFER_OUT.getValue());
                break;
            case 3:
                types.add(WarehouseMaterialHandleType.TRANSFER_OUT.getValue());
                types.add(WarehouseMaterialHandleType.TRANSFER_IN.getValue());
                break;
            case 4:
                types.add(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
                types.add(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
                break;
            case 9:
                types.add(WarehouseMaterialHandleType.TRANSFER_IN.getValue());
                break;
            case 10:
                types.add(WarehouseMaterialHandleType.TRANSFER_OUT.getValue());
                break;
            case 7:
                types.add(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue());
                break;
            case 8:
                types.add(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue());
                break;
            default:
                throw new JsonResponseException("warehouse.event.type.not.support");
        }
        return types;
    }

}
