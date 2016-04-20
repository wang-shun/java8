package io.terminus.doctor.web.design.controller;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.web.design.ext.TemplateDesignHook;
import io.terminus.doctor.web.design.modal.EcpAppKey;
import io.terminus.doctor.web.design.modal.EcpLayoutType;
import io.terminus.doctor.web.design.modal.Template;
import io.terminus.doctor.web.design.modal.TemplatePageCategory;
import io.terminus.doctor.web.design.service.EcpTemplateService;
import io.terminus.pampas.design.model.Page;
import io.terminus.pampas.engine.MessageSources;
import io.terminus.pampas.engine.config.model.Render;
import io.terminus.pampas.engine.exception.NotFound404Exception;
import io.terminus.pampas.engine.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Author:Guo Chaopeng
 * Created on 3/2/15.
 */
@RequestMapping("/api/design/templates")
@Controller
@Slf4j
public class Templates {

    @Autowired
    private EcpTemplateService templateService;

    @Autowired
    private ConfigService configService;

    @Autowired(required = false)
    private TemplateDesignHook templateDesignHook;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void create(@RequestBody Template template) {
        checkAuth();
        //新建模板时默认未发布
        template.setStatus(Template.Status.INIT.ordinal());
        templateService.create(template);
    }

    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(@RequestBody Template template) {
        checkAuth();
        checkTemplateExisted(template.getApp(), template.getKey());

        templateService.update(template);
    }

    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void delete(@PathVariable String key, @RequestParam String app) {
        checkAuth();
        Template existed = checkTemplateExisted(app, key);
        if (Objects.equal(existed.getStatus(), Template.Status.RELEASE.ordinal())) {
            throw new JsonResponseException(messageSources.get("design.template.published.can.not.be.deleted"));
        }
        templateService.delete(EcpAppKey.valueOf(app), key);
    }

    @RequestMapping(value = "/{key}/release", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void release(@PathVariable String key, @RequestParam String app, @RequestParam(required = false) String path) {
        checkAuth();
        checkTemplateExisted(app, key);

        //发布全部页面
        if (Strings.isNullOrEmpty(path)) {
            checkAllPathExisted(app, key);
            templateService.release(EcpAppKey.valueOf(app), key);

            if (templateDesignHook != null) {
                List<Page> morePages = templateDesignHook.getPages();
                if (morePages != null && !morePages.isEmpty()) {
                    for (Page page : morePages) {
                        templateService.release(EcpAppKey.valueOf(app), key, page.getPath());
                    }
                }
            }
        } else {//发布单个页面
            templateService.release(EcpAppKey.valueOf(app), key, path);
        }

    }

    @RequestMapping(value = "/{key}/save", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void save(@PathVariable String key, @RequestParam String app, @RequestParam String path, @RequestParam String hbsContent) {
        checkAuth();
        checkTemplateExisted(app, key);
        templateService.save(EcpAppKey.valueOf(app), key, path, hbsContent);
    }

    @RequestMapping(value = "/{app}/move", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void move(@PathVariable("app") String app, @RequestParam("srcKey") String srcKey, @RequestParam("destKey") String destKey) {
        checkAuth();
        checkTemplateExisted(app, srcKey);
        checkTemplateExisted(app, destKey);
        checkAllPathExisted(app, srcKey);

        for (TemplatePageCategory pageCategory : TemplatePageCategory.values()) {
            String hbsContent = templateService.getHbsContentDraft(EcpAppKey.valueOf(app), srcKey, pageCategory.getPath());
            templateService.save(EcpAppKey.valueOf(app), destKey, pageCategory.getPath(), hbsContent);
        }

        if (templateDesignHook != null) {
            List<Page> morePages = templateDesignHook.getPages();
            if (morePages != null && !morePages.isEmpty()) {
                for (Page page : morePages) {
                    String hbsContent = templateService.getHbsContentDraft(EcpAppKey.valueOf(app), srcKey, page.getPath());
                    templateService.save(EcpAppKey.valueOf(app), destKey, page.getPath(), hbsContent);
                }
            }
        }
    }

    @RequestMapping(value = "/getShopLayouts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Render.Layout> getShopLayouts() {
        List<Render.Layout> layouts = configService.listLayouts(EcpLayoutType.SHOP.name());
        return layouts;
    }

    @RequestMapping(value = "/{key}/default", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void setDefault(@PathVariable String key, @RequestParam String app) {
        checkAuth();
        Template existed = checkTemplateExisted(app, key);
        if (existed.isDefault()) {
            throw new JsonResponseException(messageSources.get("design.template.is.default"));
        }
        templateService.setDefault(EcpAppKey.valueOf(app), key);
    }

    private void checkAuth() {
//        BaseUser loginUser = UserUtil.getCurrentUser();
//        if (User.TYPE.fromNumber(loginUser.getType()) != User.TYPE.ADMIN) {
//            throw new UnAuthorize401Exception("only admin can operate template");
//        }
    }

    private Template checkTemplateExisted(String app, String key) {
        Template template = templateService.findBy(EcpAppKey.valueOf(app), key);
        if (template == null) {
            log.error("template not found where app: {},template id:{}", app, key);
            throw new NotFound404Exception(messageSources.get("design.template.not.found"));
        }
        return template;
    }

    private void checkAllPathExisted(String app, String key) {
        for (TemplatePageCategory pageCategory : TemplatePageCategory.values()) {
            String hbsContent = templateService.getHbsContentDraft(EcpAppKey.valueOf(app), key, pageCategory.getPath());
            if (Strings.isNullOrEmpty(hbsContent)) {
                throw new JsonResponseException(MoreObjects.firstNonNull(messageSources.get(pageCategory.getKeyForName()), pageCategory.getDefaultName()) + messageSources.get("design.template.not.designed"));
            }
        }

        if (templateDesignHook != null) {
            List<Page> morePages = templateDesignHook.getPages();
            if (morePages != null && !morePages.isEmpty()) {
                for (Page page : morePages) {
                    String hbsContent = templateService.getHbsContentDraft(EcpAppKey.valueOf(app), key, page.getPath());
                    if (Strings.isNullOrEmpty(hbsContent)) {
                        throw new JsonResponseException(page.getName() + messageSources.get("design.template.not.designed"));
                    }
                }
            }
        }
    }

}
