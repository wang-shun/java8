package io.terminus.doctor.web.design.dao;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.terminus.common.redis.dao.RedisBaseDao;
import io.terminus.common.redis.utils.JedisTemplate;
import io.terminus.doctor.web.design.modal.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Author:Guo Chaopeng
 * Created on 3/4/15.
 */
@Repository
public class TemplateDao extends RedisBaseDao<Template> {

    @Autowired
    public TemplateDao(@Qualifier("pampasJedisTemplate") JedisTemplate template) {
        super(template);
    }

    public String create(final Template createdTemplate) {

        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();

                String key = newKeyByAppKey(createdTemplate.getApp());
                createdTemplate.setKey(key);

                t.hmset(keyTemplate(createdTemplate.getApp(), key), stringHashMapper.toHash(createdTemplate));
                t.sadd(keyTemplates(createdTemplate.getApp()), key);
                t.exec();
            }
        });
        return createdTemplate.getKey();
    }

    public void update(final Template updatedTemplate) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                t.hmset(keyTemplate(updatedTemplate.getApp(), updatedTemplate.getKey()), stringHashMapper.toHash(updatedTemplate));
                t.exec();
            }
        });
    }

    public void replace(final Template newTemplate) {
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                t.del(keyTemplate(newTemplate.getApp(), newTemplate.getKey()));
                t.hmset(keyTemplate(newTemplate.getApp(), newTemplate.getKey()), stringHashMapper.toHash(newTemplate));
                t.exec();
            }
        });
    }

    public void delete(final String app, final String key, final Transaction t) {
        t.del(keyTemplate(app, key));
        t.srem(keyTemplates(app), key);
        t.srem(keyReleaseTemplates(app), key);
    }

    public void release(final String app, final String key) {

        final Template existed = findTemplate(app, key);
        if (existed == null || Objects.equal(existed.getStatus(), Template.Status.RELEASE.ordinal())) {
            return;
        }
        existed.setStatus(Template.Status.RELEASE.ordinal());
        template.execute(new JedisTemplate.JedisActionNoResult() {
            @Override
            public void action(Jedis jedis) {
                Transaction t = jedis.multi();
                t.hmset(keyTemplate(app, key), stringHashMapper.toHash(existed));
                //add release template index
                t.sadd(keyReleaseTemplates(app), key);
                t.exec();
            }
        });
    }

    public List<Template> findReleaseTemplates(final String app) {
        return template.execute(new JedisTemplate.JedisAction<List<Template>>() {
            @Override
            public List<Template> action(Jedis jedis) {
                Set<String> templateKeys = jedis.smembers(keyReleaseTemplates(app));
                List<Template> templates = Lists.newArrayList();

                for (String templateKey : templateKeys) {
                    if (!Strings.isNullOrEmpty(templateKey)) {
                        Template exist = findTemplate(app, templateKey);
                        if (exist != null) {
                            templates.add(exist);
                        }
                    }
                }
                return templates;
            }
        });
    }

    public List<Template> listTemplates(final String app) {
        return template.execute(new JedisTemplate.JedisAction<List<Template>>() {
            @Override
            public List<Template> action(Jedis jedis) {
                Set<String> templateKeys = jedis.smembers(keyTemplates(app));
                List<Template> templates = Lists.newArrayList();

                for (String templateKey : templateKeys) {
                    if (!Strings.isNullOrEmpty(templateKey)) {
                        Template exist = findTemplate(app, templateKey);
                        templates.add(exist);
                    }
                }
                return templates;
            }
        });
    }

    public Template findTemplate(final String app, final String key) {
        Map<String, String> hash = template.execute(new JedisTemplate.JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> action(Jedis jedis) {
                return jedis.hgetAll(keyTemplate(app, key));
            }
        });
        return stringHashMapper.fromHash(hash);
    }

    private String newKeyByAppKey(final String app) {
        return template.execute(new JedisTemplate.JedisAction<String>() {
            @Override
            public String action(Jedis jedis) {
                Long id = jedis.incr(keyForTemplateId(app));
                return Template.TEMPLATE_KEY_PREFIX + id;
            }
        });
    }

    private String keyForTemplateId(String app) {
        return "app:" + app + ":id";
    }

    private String keyTemplate(String app, String key) {
        return "app:" + app + ":template:" + key;
    }

    private String keyTemplates(String app) {
        return "app:" + app + ":templates";
    }

    private String keyReleaseTemplates(String app) {
        return "app:" + app + ":release";
    }

}
