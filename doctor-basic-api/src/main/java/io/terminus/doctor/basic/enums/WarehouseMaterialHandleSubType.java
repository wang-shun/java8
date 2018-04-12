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
    UNKNOWN(0),

    /**
     * 采购入库
     */
    CG_IN(1),

    /**
     * 退料入库
     */
    TL_IN(2),

    /**
     * 配方入库
     */
    PF_IN(3),

    /**
     * 盘盈入库
     */
    PY_IN(4),

    /**
     * 调拨入库
     */
    DB_IN(5),


    /**
     * 领料出库
     */
    LL_OUT(6),

    /**
     * 盘亏出库
     */
    PK_OUT(7),

    /**
     * 配方出库
     */
    PF_OUT(8),

    /**
     * 调拨出库
     */
    DB_OUT(9);

    @Getter
    private int value;

    WarehouseMaterialHandleSubType(int value) {
        this.value = value;
    }

}
