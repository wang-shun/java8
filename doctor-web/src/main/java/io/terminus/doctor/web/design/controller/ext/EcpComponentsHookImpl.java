package io.terminus.doctor.web.design.controller.ext;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.web.design.modal.EcpComponentCategory;
import io.terminus.doctor.web.design.service.EcpSiteService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.design.controller.ext.ComponentsHook;
import io.terminus.pampas.design.model.Site;
import io.terminus.pampas.design.service.SiteService;
import io.terminus.pampas.design.util.PagePartPath;
import io.terminus.pampas.design.util.PagePartScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 15/1/16
 */
@Component
public class EcpComponentsHookImpl implements ComponentsHook {
    @Autowired(required = false)
    private SiteService siteService;
    @Autowired
    private EcpSiteService ecpSiteService;

    @Override
    public Map<String, String> listCategories(String mode, String pagePath) {
        List<EcpComponentCategory> categoryList = Lists.newArrayList();
        categoryList.add(EcpComponentCategory.COMMON);

        if (UserUtil.getCurrentUser().getType() == UserType.ADMIN.value()){
            categoryList.add(EcpComponentCategory.SHOP);
        }else {
            PagePartPath pagePartPath = new PagePartPath(pagePath);
            PagePartScope scope = PagePartScope.fromString(pagePartPath.getScope());
            if (scope == PagePartScope.PAGE) {
                Long pageId = Long.valueOf(pagePartPath.getKey());
                Site site = siteService.findByPageId(pageId);
                boolean isShopLayout = ecpSiteService.isShopLayout(site);
                if (isShopLayout) {
                    categoryList.add(EcpComponentCategory.SHOP);
                }
            }
        }

        if (!Strings.isNullOrEmpty(mode)) {
            categoryList.add(EcpComponentCategory.ITEM);
        }
        if (UserUtil.getCurrentUser().getType() == UserType.ADMIN.value()) {
            categoryList.add(EcpComponentCategory.OFFICIAL);
        }
        Map<String, String> result = Maps.newHashMap();
        for (EcpComponentCategory category : categoryList) {
            result.put(category.name(), category.getName());
        }
        return result;
    }
}
