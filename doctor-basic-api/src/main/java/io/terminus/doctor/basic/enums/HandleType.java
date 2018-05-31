package io.terminus.doctor.basic.enums;

import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by sunbo@terminus.io on 2017/8/10.
 */
public enum HandleType {

    /**
     * 未知
     */
    UNKNOWN(0,"未知"),

    /**
     * 采购入库
     */
    IN(1,"采购入库"),


    /**
     * 领料出库
     */
    OUT(2,"领料出库"),


    /**
     * 调拨
     */
    TRANSFER(3,"调拨"),


    /**
     * 盘点
     */
    INVENTORY(4,"盘点"),

    /**
     * 盘盈
     */
    INVENTORY_PROFIT(7,"盘盈入库"),

    /**
     * 盘亏
     */
    INVENTORY_DEFICIT(8,"盘亏出库"),


    /**
     * 调入
     */
    TRANSFER_IN(9,"调拨入库"),

    /**
     * 调出
     */
    TRANSFER_OUT(10,"调拨出库"),


    /**
     * 配方生产（无用）
     */
    FORMULA(5,"配方生产"),

    /**
     * 配方生产导致的入库，饲料
     */
    FORMULA_IN(11,"配方入库"),

    /**
     * 配方生产导致的出库，药品和原料
     */
    FORMULA_OUT(12,"配方出库"),


    /**
     * 退料入库
     */
    RETURN(13,"退料入库");


    @Getter
    private int value;

    @Getter
    private String desc;

    HandleType(int value,String desc) {
        this.value = value;
        this.desc = desc;
    }

    //所有类型
    public static final List<Integer> ALL_TYPES = Lists.newArrayList(
            IN.getValue(),
            OUT.getValue(),
            INVENTORY_PROFIT.getValue(),
            INVENTORY_DEFICIT.getValue(),
            TRANSFER_IN.getValue(),
            TRANSFER_OUT.getValue(),
            FORMULA_IN.getValue(),
            FORMULA_OUT.getValue(),
            RETURN.getValue()
    );

    //所有类型名称
    public static final List<String> ALL_TYPES_DESC = Lists.newArrayList(
            IN.getDesc(),
            OUT.getDesc(),
            INVENTORY_PROFIT.getDesc(),
            INVENTORY_DEFICIT.getDesc(),
            TRANSFER_IN.getDesc(),
            TRANSFER_OUT.getDesc(),
            FORMULA_IN.getDesc(),
            FORMULA_OUT.getDesc(),
            RETURN.getDesc()
    );

}
