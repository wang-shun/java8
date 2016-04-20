package io.terminus.doctor.web.design.modal;

import io.terminus.common.utils.Splitters;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Author:Guo Chaopeng
 * Created on 3/2/15.
 */
public class Template implements Serializable, Comparable<Template> {

    public final static String TEMPLATE_KEY_PREFIX = "_template:";

    private static final long serialVersionUID = 8319047364195240725L;

    public static enum Status {
        INIT, RELEASE
    }

    @Getter
    @Setter
    private String app;

    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String desc;

    @Getter
    @Setter
    private Integer status;

    @Getter
    @Setter
    private boolean isDefault;

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Template)) {
            return false;
        }

        Template that = (Template) o;
        if (!app.equals(that.app)) {
            return false;
        }
        if (!key.equals(that.key)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int prime = 59;
        int result = 1;
        result = result * prime + app.hashCode();
        result = result * prime + key.hashCode();
        return result;
    }

    @Override
    public int compareTo(Template that) {
        if (app.equals(that.app)) {
            return keyId(key) - keyId(that.key);
        }
        return app.compareTo(that.app);
    }

    private int keyId(String key) {
        return Integer.parseInt(Splitters.COLON.splitToList(key).get(1));
    }

}
