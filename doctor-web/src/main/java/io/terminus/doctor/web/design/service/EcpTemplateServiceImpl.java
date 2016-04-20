package io.terminus.doctor.web.design.service;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.doctor.web.design.dao.TemplateDao;
import io.terminus.doctor.web.design.modal.EcpAppKey;
import io.terminus.doctor.web.design.modal.Template;
import io.terminus.doctor.web.design.modal.TemplatePageCategory;
import io.terminus.pampas.engine.dao.RedisFileDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author:Guo Chaopeng
 * Created on 3/5/15.
 */
@Slf4j
@Service
public class EcpTemplateServiceImpl implements EcpTemplateService {

    private final static Pattern templateContentRegex = Pattern.compile("\\{\\{#designPart.*?}}([\\s\\S]*)\\{\\{/designPart}}");

    @Autowired
    private TemplateDao templateDao;

    @Autowired
    private RedisFileDao redisFileDao;

    @Autowired
    @Qualifier("pampasJedisTemplate")
    private JedisTemplate jedisTemplate;

    @Override
    public String create(Template template) {
        checkArgument(!Strings.isNullOrEmpty(template.getApp()), "app can not be empty");
        checkArgument(!Strings.isNullOrEmpty(template.getName()), "name can not be empty");

        return templateDao.create(template);
    }

    @Override
    public void save(final EcpAppKey appKey, final String key, final String path, final String hbsContent) {
        Template existed = templateDao.findTemplate(appKey.name(), key);

        if (existed == null) {
            log.warn("template not found for app:{},key:{}", appKey.name(), key);
            throw new IllegalStateException("template not found");
        }

        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                jedis.set(keyHbsContent(appKey.name(), key, path, false), hbsContent);
            }
        });
    }

    @Override
    public void delete(final EcpAppKey appKey, final String key) {
        checkNotNull(appKey, "app can not be null");
        checkArgument(!Strings.isNullOrEmpty(key), "template key can not be empty");


        Template existed = templateDao.findTemplate(appKey.name(), key);
        if (existed == null) {
            return;
        }

        //已发布的模板不能被删除
        if (Objects.equal(existed.getStatus(), Template.Status.RELEASE.ordinal())) {
            return;
        }

        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                //删除模板
                templateDao.delete(appKey.name(), key, t);

                //删除模板对应的hbs content
                for (TemplatePageCategory pageCategory : TemplatePageCategory.values()) {
                    t.del(keyHbsContent(appKey.name(), key, pageCategory.getPath(), false));
                    redisFileDao.delete(keyHbsContent(appKey.name(), key, pageCategory.getPath(), true), t);
                }
                t.exec();
            }
        });
    }

    @Override
    public void update(Template template) {
        checkArgument(!Strings.isNullOrEmpty(template.getApp()), "app can not be empty");
        checkArgument(!Strings.isNullOrEmpty(template.getKey()), "template key can not be null");

        Template existed = templateDao.findTemplate(template.getApp(), template.getKey());
        if (existed == null) {
            return;
        }

        templateDao.update(template);
    }

    @Override
    public void release(final EcpAppKey appKey, final String key) {
        checkNotNull(appKey, "app key can not be null");
        checkArgument(!Strings.isNullOrEmpty(key), "template key can not be empty");

        Template existed = templateDao.findTemplate(appKey.name(), key);
        if (existed == null) {
            return;
        }

        if (!Objects.equal(existed.getStatus(), Template.Status.RELEASE.ordinal())) {
            templateDao.release(appKey.name(), key);
        }

        for (TemplatePageCategory pageCategory : TemplatePageCategory.values()) {
            _release(appKey, key, pageCategory.getPath());
        }

    }

    @Override
    public void release(final EcpAppKey appKey, final String key, final String path) {
        Template existed = templateDao.findTemplate(appKey.name(), key);
        if (existed == null) {
            return;
        }
        _release(appKey, key, path);
    }

    @Override
    public void setDefault(EcpAppKey appKey, String key) {
        List<Template> templates = listTemplateByApp(appKey);
        for (Template template : templates) {
            if (template.getKey().equals(key)) {
                if (template.isDefault()) {
                    break;
                }
                template.setDefault(true);
                templateDao.update(template);
            } else {
                if (template.isDefault()) {
                    template.setDefault(false);
                    // stringHashMapper.toHash忽略了false字段,因此不能调用update方法
                    templateDao.replace(template);
                }
            }
        }
    }

    @Override
    public Template findBy(EcpAppKey appKey, String key) {
        return templateDao.findTemplate(appKey.name(), key);
    }

    @Override
    public String findTemplateContent(EcpAppKey appKey, String key, String path, boolean isRelease) {
        String hbsContent;
        if (isRelease) {
            hbsContent = redisFileDao.getContent(keyHbsContent(appKey.name(), key, path, isRelease));
        } else {
            hbsContent = getHbsContentDraft(appKey, key, path);
        }

        if (!Strings.isNullOrEmpty(hbsContent)) {
            Matcher matcher = templateContentRegex.matcher(hbsContent);
            while (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "";
    }

    @Override
    public Map<String, List<Template>> listTemplates() {
        Map<String, List<Template>> templates = Maps.newLinkedHashMap();

        templates.put(EcpAppKey.PC.name(), listTemplateByApp(EcpAppKey.PC));
        templates.put(EcpAppKey.MOBILE.name(), listTemplateByApp(EcpAppKey.MOBILE));
        return templates;
    }

    @Override
    public String getHbsContentDraft(final EcpAppKey appKey, final String key, final String path) {
        return jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(keyHbsContent(appKey.name(), key, path, false));
            }
        });
    }

    private List<Template> listTemplateByApp(EcpAppKey ecpAppKey) {
        return templateDao.listTemplates(ecpAppKey.name());
    }

    private void _release(final EcpAppKey appKey, final String key, final String path) {
        final String hbsContentDraft = jedisTemplate.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                return jedis.get(keyHbsContent(appKey.name(), key, path, false));
            }
        });

        if (!Strings.isNullOrEmpty(hbsContentDraft)) {
            redisFileDao.save(keyHbsContent(appKey.name(), key, path, true), hbsContentDraft);
        }
    }

    private String keyHbsContent(String app, String key, String path, boolean isRelease) {
        return isRelease ? "redis:app:" + app + ":template:" + key + ":path:" + path + ":content" : "app:" + app + ":template:" + key + ":path:" + path + ":content-draft";
    }

}
