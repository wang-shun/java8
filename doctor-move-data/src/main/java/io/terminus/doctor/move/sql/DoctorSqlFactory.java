package io.terminus.doctor.move.sql;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.URLTemplateSource;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Slf4j
public class DoctorSqlFactory {

    private static final Handlebars handlebars = new Handlebars();

    private Map<String, Template> hbsTemplateMap = Maps.newHashMap();

    private final Resource[] hbsLocations;

    public DoctorSqlFactory(Resource[] hbsLocations) {
        this.hbsLocations = hbsLocations;
    }

    @PostConstruct
    public void init() {
        List<Resource> list = Arrays.asList(hbsLocations);
        if (!notEmpty(list)) return;
        if (list.size() != Sets.newHashSet(list).size()) {
            throw new IllegalArgumentException("hbs file name repeat");
        }
        putTemplate();
    }

    private void putTemplate() {
        try {
            for(Resource resource : hbsLocations){
                URLTemplateSource templateSource = new URLTemplateSource(resource.getFilename(), resource.getURL());
                String fileName = Files.getNameWithoutExtension(resource.getFilename());
                hbsTemplateMap.put(fileName, handlebars.compile(templateSource));
                log.info("find hbs file name {}", fileName);
            }
        } catch (Exception e) {
            log.error("init put hbs template failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }


    /**
     * 通过文件名称 和 参数值 获取sql
     * @param fileName 文件名称
     * @param context 参数
     * @return sql语句
     */
    public String getSql(String fileName, Map<String, ?> context){
        Template template = hbsTemplateMap.get(fileName);
        if(isNull(template)){
            log.warn("filName not found, fileName:{}", fileName);
            return null;
        }
        try {
            String sql = template.apply(context);
            log.info("[SQL]file: {} print excute sql is {}", fileName, sql);

            return sql;
        } catch (IOException e) {
            log.error("hbs error: {}", Throwables.getStackTraceAsString(e));
            return null;
        }
    }
}
