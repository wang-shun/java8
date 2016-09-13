package io.terminus.doctor.event.enums;

import lombok.Getter;

/**
 * Desc: 销售基础重量(kg)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/12
 */

public enum  SaleBaseWeight {

    BASE_10(10),
    BASE_15(15);

    @Getter
    private int weight;

    SaleBaseWeight(int weight) {
        this.weight = weight;
    }
}
