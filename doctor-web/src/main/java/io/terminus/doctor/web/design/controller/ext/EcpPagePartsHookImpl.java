package io.terminus.doctor.web.design.controller.ext;

import io.terminus.doctor.web.design.service.EcpSiteService;
import io.terminus.pampas.design.controller.ext.PagePartsHook;
import io.terminus.pampas.design.service.SiteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 15/1/15
 */
@Component @Slf4j
public class EcpPagePartsHookImpl implements PagePartsHook {
    @Autowired(required = false)
    private SiteService siteService;
    @Autowired
    private EcpSiteService ecpSiteService;

    @Override
    public void save(String path, Map<String, String> parts) {
        authCheck(path);
    }

    @Override
    public void release(String path) {
        authCheck(path);
    }

    @Override
    public void delete(String path) {
        authCheck(path);
    }

    @Override
    public void move(String srcPath, String destPath) {
        authCheck(srcPath);
        authCheck(destPath);
    }

    private void authCheck(String path) {
//        BaseUser BaseUser = UserUtil.getCurrentUser();
//        User.TYPE currentUserType = User.TYPE.fromNumber(BaseUser.getType());
//        boolean superUser = currentUserType == User.TYPE.ADMIN || currentUserType == User.TYPE.SITE_OWNER;
//        PagePartPath pagePartPath = new PagePartPath(path);
//        switch (PagePartScope.fromString(pagePartPath.getScope())) {
//            case NAIVE:
//                if (superUser) {
//                    return;
//                }
//                break;
//            case PAGE:
//                if (superUser) {
//                    return;
//                }
//                Long pageId = Long.valueOf(pagePartPath.getKey());
//                Site site = siteService.findByPageId(pageId);
//                boolean isShopLayout = ecpSiteService.isShopLayout(site);
//                if (isShopLayout) {
//                    if (Objects.equal(site.getUserId(), BaseUser.getId())) {
//                        return;
//                    }
//                }
//                break;
//        }
//        log.warn("no auth for path: {}, currentUser: {}", path, BaseUser.getId());
//        throw new JsonResponseException(401, "pageparts.update.noauth");
    }
}
