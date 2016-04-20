package io.terminus.doctor.web.design.controller.ext;

import io.terminus.doctor.web.design.service.EcpSiteService;
import io.terminus.pampas.design.controller.ext.DesignHook;
import io.terminus.pampas.design.model.Site;
import io.terminus.pampas.design.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 15/1/15
 */
@Component
public class EcpDesignHookImpl implements DesignHook {
    @Autowired(required = false)
    private SiteService siteService;
    @Autowired
    private EcpSiteService ecpSiteService;

    @Override
    public void design(Site site, Map<String, Object> context) {
        authCheck(site);
    }

    @Override
    public void page(Long pageId, Map<String, Object> context) {
        Site site = siteService.findByPageId(pageId);
        authCheck(site);
        boolean isShopLayout = ecpSiteService.isShopLayout(site);
        if (isShopLayout) {
            context.put("sellerId", site.getUserId());
        }
    }

    @Override
    public String customPage(String url, String pagePath) {
//        BaseUser BaseUser = UserUtil.getCurrentUser();
//        User.TYPE userType = User.TYPE.fromNumber(BaseUser.getType());
//        if (userType == User.TYPE.ADMIN) {
//            return null;
//        }
//        throw new JsonResponseException(401, "design.no.auth");
        return null;
    }

    private void authCheck(Site site) {
//        BaseUser BaseUser = UserUtil.getCurrentUser();
//        User.TYPE userType = User.TYPE.fromNumber(BaseUser.getType());
//        if (userType == User.TYPE.ADMIN) {
//            return;
//        }
//        if (Objects.equal(site.getUserId(), BaseUser.getId())) {
//            return;
//        }
//        throw new JsonResponseException(401, "design.no.auth");
    }
}
