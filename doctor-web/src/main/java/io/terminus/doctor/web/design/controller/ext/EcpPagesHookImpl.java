package io.terminus.doctor.web.design.controller.ext;

import io.terminus.pampas.design.controller.ext.PagesHook;
import io.terminus.pampas.design.model.Page;
import io.terminus.pampas.design.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 15/1/16
 */
@Component
public class EcpPagesHookImpl implements PagesHook {
    @Autowired(required = false)
    private SiteService siteService;

    @Override
    public void create(Page page) {
        authCheck(page.getSiteId());
    }

    @Override
    public void update(Page page) {
        authCheck(page.getSiteId());
    }

    @Override
    public void delete(Page page) {
        authCheck(page.getSiteId());
    }

    private void authCheck(Long siteId) {
//        Site site = siteService.findById(siteId);
//        BaseUser loginUser = UserUtil.getCurrentUser();
//        User.TYPE userType = User.TYPE.fromNumber(loginUser.getType());
//        if (userType == User.TYPE.ADMIN) {
//            return;
//        }
//        if (Objects.equal(site.getUserId(), loginUser.getId())) {
//            return;
//        }
//        throw new JsonResponseException(401, "design.no.auth");
    }
}
