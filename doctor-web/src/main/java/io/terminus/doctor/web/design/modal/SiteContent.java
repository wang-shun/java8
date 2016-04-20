package io.terminus.doctor.web.design.modal;

import io.terminus.pampas.design.model.Page;
import io.terminus.pampas.design.model.Site;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Author:cp
 * Created on 11/5/15.
 */
public class SiteContent extends BaseDesignContent {
    @Setter
    @Getter
    private Site site;
    @Setter
    @Getter
    private List<Page> pages;
    @Setter
    @Getter
    private Map<Long, Map<String, String>> pageParts;
    @Setter
    @Getter
    private Map<String, String> siteParts;
    @Setter
    @Getter
    private Map<String, String> globalParts;
}
