package io.terminus.doctor.web.design.modal;

import lombok.Getter;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 15/1/16
 */
public enum EcpComponentCategory {
    ADMIN("不可装修"), OFFICIAL("官方"), COMMON("通用"), SHOP("店铺"), ITEM("商品");

    @Getter
    private String name;

    EcpComponentCategory(String name) {
        this.name = name;
    }
}
