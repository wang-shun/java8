package io.terminus.doctor.basic.enums;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

/**
 * @Author:Danny
 * @Description:
 * @Date:Create In 20:23 2018/4/11/011
 * @Modified By:
 */
public enum WarehouseMaterialHandleSubType {

    /**
     * 未知
     */
    UNKNOWN(0,"未知"),

    /**
     * 采购入库
     */
    CG_IN(1,"采购入库"),

    /**
     * 退料入库
     */
    TL_IN(2,"退料入库"),

    /**
     * 配方入库
     */
    PF_IN(3,"配方入库"),

    /**
     * 盘盈入库
     */
    PY_IN(4,"盘盈入库"),

    /**
     * 调拨入库
     */
    DB_IN(5,"调拨入库"),


    /**
     * 领料出库
     */
    LL_OUT(6,"领料出库"),

    /**
     * 盘亏出库
     */
    PK_OUT(7,"盘亏出库"),

    /**
     * 配方出库
     */
    PF_OUT(8,"配方出库"),

    /**
     * 调拨出库
     */
    DB_OUT(9,"调拨出库");

    @Getter
    private int value;

    @Getter
    private String desc;

    WarehouseMaterialHandleSubType(int value,String desc) {
        this.value = value;
        this.desc = desc;
    }

    //所有类型
    public static final List<Integer> ALL_TYPES = Lists.newArrayList(
            CG_IN.getValue(),
            TL_IN.getValue(),
            PF_IN.getValue(),
            PY_IN.getValue(),
            DB_IN.getValue(),
            LL_OUT.getValue(),
            PK_OUT.getValue(),
            PF_OUT.getValue(),
            DB_OUT.getValue()
    );

    //所有类型名称
    public static final List<String> ALL_TYPES_DESC = Lists.newArrayList(
            CG_IN.getDesc(),
            TL_IN.getDesc(),
            PF_IN.getDesc(),
            PY_IN.getDesc(),
            DB_IN.getDesc(),
            LL_OUT.getDesc(),
            PK_OUT.getDesc(),
            PF_OUT.getDesc(),
            DB_OUT.getDesc()
    );

}
