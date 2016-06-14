package io.terminus.doctor.msg.helper;

import com.github.jknack.handlebars.Template;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.terminus.parana.msg.impl.dao.mysql.MessageTemplateDao;
import io.terminus.parana.msg.model.MessageTemplate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/14
 */
@Slf4j
@Component
public class DoctorMessageTemplateCacher {

    private final DoctorHandleBarsHelper doctorHandleBarsHelper;
    private final MessageTemplateDao messageTemplateDao;

    @Getter
    private final LoadingCache<String, Optional<Template>> templateTitleCache;

    @Getter
    private final LoadingCache<String, Optional<Template>> templateContentCache;

    @Autowired
    public DoctorMessageTemplateCacher(DoctorHandleBarsHelper doctorHandleBarsHelper,
                                       MessageTemplateDao messageTemplateDao) {
        this.doctorHandleBarsHelper = doctorHandleBarsHelper;
        this.messageTemplateDao = messageTemplateDao;

        templateTitleCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.DAYS).build(new CacheLoader<String, Optional<Template>>() {
            @Override
            public Optional<Template> load(String name) throws Exception {
                MessageTemplate template = messageTemplateDao.findByName(name);
                if (template == null || template.getContent() == null) {
                    return Optional.empty();
                }
                return Optional.ofNullable(doctorHandleBarsHelper.compileInline(template.getTitle()));
            }
        });

        templateContentCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.DAYS).build(new CacheLoader<String, Optional<Template>>() {
            @Override
            public Optional<Template> load(String name) throws Exception {
                MessageTemplate template = messageTemplateDao.findByName(name);
                if (template == null || template.getContent() == null) {
                    return Optional.empty();
                }
                return Optional.ofNullable(doctorHandleBarsHelper.compileInline(template.getContent()));
            }
        });
    }

    public void cleanAll() {
        templateTitleCache.cleanUp();
        templateContentCache.cleanUp();
    }

    public void refreshByName(String name) {
        templateTitleCache.refresh(name);
        templateContentCache.refresh(name);
    }
}
