/*
 * Copyright (c) 2014 杭州端点网络科技有限公司
 */

package io.terminus.doctor.web.core.component;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.model.BaseUser;
import io.terminus.pampas.common.UserUtil;
import io.terminus.pampas.engine.ThreadVars;
import io.terminus.pampas.engine.handlebars.HandlebarsEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2015 杭州端点网络科技有限公司
 * Date: 2/26/16
 * Time: 5:41 PM
 * Author: 2015年 <a href="mailto:d@terminus.io">张成栋</a>
 */
@Component
@ConditionalOnBean(HandlebarsEngine.class)
public class DoctorHbsHelpers {

    private final HandlebarsEngine handlebarsEngine;

    @Autowired
    public DoctorHbsHelpers(final HandlebarsEngine handlebarsEngine) {
        this.handlebarsEngine = handlebarsEngine;

        this.handlebarsEngine.registerHelper("cross", new Helper<Collection>() {
            @Override
            public CharSequence apply(Collection cross1, Options options) throws IOException {
                Collection cross2 = options.param(0, Lists.newArrayList());
                // 两者都为 null 时直接 return 空
                if (cross1 == null && cross2 == null) {
                    return "";
                }
                String p1 = options.param(1);
                String p2 = options.param(2, p1);
                Map<String, Map<String, Object>> crossResult = Maps.newLinkedHashMap();
                Fields.FieldGetter getter1 = null;
                if (cross1 != null) {
                    for (Object o : cross1) {
                        if (getter1 == null) {
                            getter1 = Fields.createGetter(o.getClass(), p1);
                        }
                        Object key = getter1.get(o);
                        if (key == null) continue;
                        String keyStr = key.toString();
                        Map<String, Object> entry = crossResult.get(keyStr);
                        if (entry == null) {
                            entry = Maps.newHashMap();
                            crossResult.put(keyStr, entry);
                        }
                        entry.put("left", o);
                    }
                }
                Fields.FieldGetter getter2 = null;
                if (cross2 != null) {
                    for (Object o : cross2) {
                        if (getter2 == null) {
                            getter2 = Fields.createGetter(o.getClass(), p2);
                        }
                        Object key = getter2.get(o);
                        if (key == null) continue;
                        String keyStr = key.toString();
                        Map<String, Object> entry = crossResult.get(keyStr);
                        if (entry == null) {
                            entry = Maps.newHashMap();
                            crossResult.put(keyStr, entry);
                        }
                        entry.put("right", o);
                    }
                }
                StringBuilder result = new StringBuilder();
                int index = 0;
                for (Map.Entry<String, Map<String, Object>> entry : crossResult.entrySet()) {
                    Context current = Context.newBuilder(options.context, entry.getValue())
                            .combine("@key", entry.getKey())
                            .combine("@index", index).build();
                    result.append(options.fn(current));
                    index++;
                }
                return result.toString();
            }
        });

        /**
         * detect locale
         */
        this.handlebarsEngine.registerHelper("isLocale", new Helper<String>(){
            @Override
            public CharSequence apply(String locale, Options options) throws IOException {
                if (ThreadVars.getLocale().toString().startsWith(locale)) {
                    return options.fn();
                } else {
                    return options.inverse();
                }
            }
        });

        this.handlebarsEngine.registerHelper("hasRole", new Helper<String>() {
            @Override
            public CharSequence apply(String key, Options options) throws IOException {
                BaseUser user = UserUtil.getCurrentUser();
                if (user == null || user.getRoles() == null || user.getRoles().isEmpty()) {
                    return options.inverse();
                }
                Pattern pat = Pattern.compile(key);
                for (String role : user.getRoles()) {
                    if (pat.matcher(role).matches()) {
                        return options.fn();
                    }
                }
                return options.inverse();
            }
        });
    }
}
