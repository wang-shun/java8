package io.terminus.doctor.basic.enums;

import lombok.Getter;

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

}
