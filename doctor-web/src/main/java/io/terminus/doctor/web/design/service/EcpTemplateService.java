package io.terminus.doctor.web.design.service;

import io.terminus.doctor.web.design.modal.EcpAppKey;
import io.terminus.doctor.web.design.modal.Template;
import io.terminus.pampas.client.Export;

import java.util.List;
import java.util.Map;

/**
 * Author:Guo Chaopeng
 * Created on 3/5/15.
 */
public interface EcpTemplateService {

    /**
     * 创建模板
     */
    public String create(Template template);

    /**
     * 保存hbs content
     */
    public void save(EcpAppKey appKey, String key, String path, String hbsContent);

    public void delete(EcpAppKey appKey, String key);

    public void update(Template template);

    public void release(EcpAppKey appKey, String key);

    public void release(EcpAppKey appKey, String key, String path);

    public void setDefault(EcpAppKey appKey, String key);

    public Template findBy(EcpAppKey appKey, String key);

    public String findTemplateContent(EcpAppKey appKey, String key, String path, boolean isRelease);

    @Export
    public Map<String, List<Template>> listTemplates();

    public String getHbsContentDraft(final EcpAppKey appKey, final String key, final String path);

}
