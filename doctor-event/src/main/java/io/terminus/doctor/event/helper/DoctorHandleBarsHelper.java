package io.terminus.doctor.event.helper;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.google.common.base.Strings;
import io.terminus.common.utils.Splitters;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

/**
 * Desc: 猪场消息HbsHelper
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/14
 */
@Component
public class DoctorHandleBarsHelper {

    private final Handlebars handlebars = new Handlebars();

    public Template compileInline(String input) throws IOException {
        return handlebars.compileInline(input);
    }

    @PostConstruct
    public void init() {
        handlebars.registerHelper("of", (source, options) -> {
            if (source == null) {
                return options.inverse();
            }

            String _source = source.toString();
            String param = options.param(0);
            if (Strings.isNullOrEmpty(param)) {
                return options.inverse();
            }

            List<String> targets = Splitters.COMMA.splitToList(param);
            if (targets.contains(_source)) {
                return options.fn();
            }
            return options.inverse();
        });
    }
}
