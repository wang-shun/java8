package io.terminus.doctor.web.design.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.web.design.service.DesignContentService;
import io.terminus.doctor.web.design.service.EcpTemplateService;
import io.terminus.pampas.design.model.Site;
import io.terminus.pampas.design.service.SiteService;
import io.terminus.pampas.engine.exception.NotFound404Exception;
import io.terminus.doctor.web.design.modal.BaseDesignContent;
import io.terminus.doctor.web.design.modal.EcpAppKey;
import io.terminus.doctor.web.design.modal.ShopTemplateContent;
import io.terminus.doctor.web.design.modal.SiteContent;
import io.terminus.doctor.web.design.modal.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;

import static com.google.common.base.Preconditions.checkState;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * Author:cp
 * Created on 11/4/15.
 */
@Controller
@RequestMapping("/api/design-content")
@Slf4j
public class DesignContents {

    @Autowired
    private EcpTemplateService templateService;

    @Autowired
    private DesignContentService designContentService;

    @Autowired(required = false)
    private SiteService siteService;

    private static final JsonMapper JSON_MAPPER = JsonMapper.JSON_NON_EMPTY_MAPPER;


    @RequestMapping(value = "/export-shop-template/{app}/{key}", method = RequestMethod.GET)
    @ResponseBody
    public void exportShopTemplate(@PathVariable("app") String app, @PathVariable("key") String key, HttpServletResponse response) {
        Template template = checkTemplateExisted(app, key);
        try {
            ShopTemplateContent shopTemplateContent = designContentService.exportShopTemplate(app, key);
            export(shopTemplateContent, template.getApp() + "-" + template.getName(), response);
        } catch (Exception e) {
            log.error("export shop template(app={},key={}) failed, cause:{}", app, key, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "shop.template.export.fail");
        }
    }

    @RequestMapping(value = "/export-site/{siteId}", method = RequestMethod.GET)
    @ResponseBody
    public void exportSite(@PathVariable("siteId") Long siteId, HttpServletResponse response) {
        Site site = checkSiteExisted(siteId);
        try {
            SiteContent siteContent = designContentService.exportSite(siteId);
            export(siteContent, site.getName(), response);
        } catch (Exception e) {
            log.error("export site(id={}) failed, cause:{}", siteId, Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(500, "site.export.fail");
        }
    }

    @RequestMapping(value = "/import-shop-template", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void importShopTemplate(MultipartFile file) {
        ShopTemplateContent shopTemplateContent;
        try {
            String content = new String(file.getBytes(), "UTF-8");
            shopTemplateContent = JSON_MAPPER.fromJson(content, ShopTemplateContent.class);
            checkState(notNull(shopTemplateContent));
        } catch (Exception e) {
            log.error("fail to parser shop template file,cause:", e);
            throw new JsonResponseException("parser.shop.template.file.fail");
        }

        try {
            designContentService.importShopTemplate(shopTemplateContent);
        } catch (Exception e) {
            log.error("fail to import shop template,cause:", e);
            throw new JsonResponseException("import.shop.template.fail");
        }
    }

    @RequestMapping(value = "/import-site", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void importSite(MultipartFile file) {
        SiteContent siteContent;
        try {
            String content = new String(file.getBytes(), "UTF-8");
            siteContent = JSON_MAPPER.fromJson(content, SiteContent.class);
            checkState(notNull(siteContent));
        } catch (Exception e) {
            log.error("fail to parser site file,cause:", e);
            throw new JsonResponseException("parser.site.file.fail");
        }

        try {
            designContentService.importSite(siteContent);
        } catch (IllegalStateException e) {
            log.error("fail to import site,cause:", e);
            throw new JsonResponseException(e.getMessage());
        } catch (Exception e) {
            log.error("fail to import site,cause:", e);
            throw new JsonResponseException("import.site.fail");
        }
    }

    private void export(BaseDesignContent designContent, String fileName, HttpServletResponse response) throws IOException {
        String fileNameWithExt = URLEncoder.encode(fileName, "UTF-8") + ".design";
        response.setContentType("text/plain; charset=UTF-8");
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"", fileNameWithExt);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(headerKey, headerValue);

        OutputStream outputStream = response.getOutputStream();

        PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
        out.print(JSON_MAPPER.toJson(designContent));

        out.flush();
        out.close();
    }

    private Template checkTemplateExisted(String app, String key) {
        Template template = templateService.findBy(EcpAppKey.valueOf(app), key);
        if (template == null) {
            log.error("template not found where app: {},template id:{}", app, key);
            throw new NotFound404Exception("design.template.not.found");
        }
        return template;
    }

    private Site checkSiteExisted(Long siteId) {
        Site site = siteService.findById(siteId);
        if (site == null) {
            log.error("site(id={}) not found", siteId);
            throw new NotFound404Exception("site.not.found");
        }
        return site;
    }
}
