package io.terminus.doctor.web.design.modal;

import lombok.Getter;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 15/1/14
 */
public enum ShopSitePageCategory {
    INDEX("shop.page.index", "首页", "index"),
    ITEM("shop.page.item", "商品详情", "item"),
    LIST("shop.page.list", "商品列表", "list");

    @Getter
    private String keyForName;
    @Getter
    private String defaultName;
    @Getter
    private String path;

    ShopSitePageCategory(String keyForName, String defaultName, String path) {
        this.keyForName = keyForName;
        this.defaultName = defaultName;
        this.path = path;
    }
}
