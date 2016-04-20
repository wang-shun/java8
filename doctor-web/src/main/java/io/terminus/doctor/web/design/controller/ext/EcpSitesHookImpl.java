package io.terminus.doctor.web.design.controller.ext;

import io.terminus.pampas.design.controller.ext.SitesHook;
import io.terminus.pampas.design.model.Site;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 15/1/16
 */
public class EcpSitesHookImpl implements SitesHook {
    @Override
    public void create(Site site) {
        authCheck(site);
    }

    @Override
    public void update(Site site) {
        authCheck(site);
    }

    @Override
    public void delete(Site site) {
        authCheck(site);
    }

    @Override
    public void setIndex(Site site, String path) {
        authCheck(site);
    }

    @Override
    public void clearIndex(Site site) {
        authCheck(site);
    }

    @Override
    public void release(Site site) {
        authCheck(site);
    }

    @Override
    public void listPages(Site site) {
        authCheck(site);
    }

    private void authCheck(Site site) {
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

