/*
 * Copyright (c) 2014 杭州端点网络科技有限公司
 */

package io.terminus.doctor.web.core.component;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.terminus.pampas.engine.handlebars.HandlebarsEngine;
import io.terminus.doctor.web.core.component.AliyunImageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2015 杭州端点网络科技有限公司
 * Date: 2/26/16
 * Time: 5:41 PM
 * Author: 2015年 <a href="mailto:d@terminus.io">张成栋</a>
 */
@Component
@ConditionalOnBean(HandlebarsEngine.class)
public class ParanaHbsHelpers {
    private HandlebarsEngine handlebarsEngine;

    @Autowired
    public ParanaHbsHelpers(HandlebarsEngine handlebarsEngine) {
        this.handlebarsEngine = handlebarsEngine;

        this.handlebarsEngine.registerHelper("splitter", new Helper<String>() {
            @Override
            public CharSequence apply(String context, Options options) throws IOException {
                try {
                    String delimiter = options.param(0);
                    if (Strings.isNullOrEmpty(delimiter)) {
                        return "";
                    }

                    Integer index = options.param(1, 0);

                    List<String> pieces = Splitter.on(delimiter).omitEmptyStrings().splitToList(context);

                    return pieces.get(index);
                } catch (Exception e) {
                    //ignore exception
                    return "";
                }
            }
        });

        this.handlebarsEngine.registerHelper("cdnPath", new Helper<String>() {

            @Override
            public CharSequence apply(String path, Options options) throws IOException {
                if (isEmpty(path)) {
                    return "http://zcy-dev.img-cn-hangzhou.aliyuncs.com/system/error/image_not_found.001.jpeg";
                }

                // empty params
                if (options.params == null || options.params.length == 0) {
                    return path;
                }

                // 外部图片不压缩
                //TODO:这里图片迁移会有问题,是否只判断阿里云域名?
                if (!path.trim().contains("aliyuncs.com")) {
                    return path;
                }

                return path + AliyunImageProperties.from(options.params).getQueryString();
            }
        });

        this.handlebarsEngine.registerHelper("isEmpty", new Helper<Object>() {
            @Override
            public CharSequence apply(Object context, Options options) throws IOException {
                if (isEmpty(context)) {
                    return options.fn();
                } else {
                    return options.inverse();
                }
            }
        });
    }

    public boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence) {
            return ((CharSequence) value).length() == 0 || ((CharSequence) value).equals("[]");
        }
        if (value instanceof Collection) {
            return ((Collection) value).size() == 0;
        }
        if (value instanceof Iterable) {
            return !((Iterable) value).iterator().hasNext();
        }
        if (value instanceof Boolean) {
            return !(Boolean) value;
        }
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() == 0;
        }

        return false;
    }
}
