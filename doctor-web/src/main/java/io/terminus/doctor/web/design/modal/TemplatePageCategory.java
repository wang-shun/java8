package io.terminus.doctor.web.design.modal;

import lombok.Getter;

/**
 * Author:Guo Chaopeng
 * Created on 3/6/15.
 */
public enum TemplatePageCategory {

    INDEX("design.template.index", "首页", "index"),
    ITEM("design.template.item", "商品详情", "item"),
    LIST("design.template.list", "商品列表", "list"),
    DEFAULT_LAYOUT("design.template.default.layout", "默认模板", "default_layout");

    @Getter
    private String keyForName;
    @Getter
    private String defaultName;
    @Getter
    private String path;

    TemplatePageCategory(String keyForName, String defaultName, String path) {
        this.keyForName = keyForName;
        this.defaultName = defaultName;
        this.path = path;
    }

}
