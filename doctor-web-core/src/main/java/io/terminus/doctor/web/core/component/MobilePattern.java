package io.terminus.doctor.web.core.component;

import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.regex.Pattern;

/**
 * @author Effet
 */
@Component
public class MobilePattern {

    @Getter
    protected Pattern pattern;

    @PostConstruct
    public void init() {
        pattern = Pattern.compile("^1[3|4|5|6|7|8|9]\\d{9}$");
    }

    //^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\d{8}$
}
