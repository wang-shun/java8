package io.terminus.doctor.web.design.modal;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Author:cp
 * Created on 11/5/15.
 */
public class ShopTemplateContent extends BaseDesignContent {
    @Setter
    @Getter
    private Template template;
    @Setter
    @Getter
    private Map<String, String> layoutHbsPair = new HashMap<>();

    public void setLayout(String layoutName, String hbs) {
        layoutHbsPair.put(layoutName, hbs);
    }
}
