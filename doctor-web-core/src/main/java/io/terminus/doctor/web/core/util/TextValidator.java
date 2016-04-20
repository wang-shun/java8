/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.web.core.util;

import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * @See io.terminus.parana.common.util.TextValidator
 *
 * Author  : panxin
 * Date    : 8:03 PM 3/7/16
 * Mail    : panxin@terminus.io
 */
public enum TextValidator {

    EMAIL(){
        //"^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$" 此正则表达式会把+.示为不合法字符
        //\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*
        private Pattern pattern=Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        @Override
        public boolean boolCheck(String text) {
            return text != null && pattern.matcher(text).matches();
        }
    },

    /**
     * 用户名长度 5~25
     */
    USERNAME() {
        // TODO(Effet): 应更复杂
        private Pattern pattern = Pattern.compile("^[\\d_a-zA-Z\\u4E00-\\u9FA5]{5,25}$");

        @Override
        public boolean boolCheck(String text) {
            return text != null && pattern.matcher(text).matches();
        }
    },

    /**
     * 密码长度限制 6~16
     */
    PASSWORD() {
        private Pattern pattern = Pattern.compile("[\\s\\S]{6,16}");

        @Override
        public boolean boolCheck(String text) {
            return text != null && pattern.matcher(text).matches();
        }
    },

    SUB_SELLER() {
        private Pattern pattern = Pattern.compile("^[a-zA-z][\\d_a-zA-Z\\u4E00-\\u9FA5]{4,24}$");

        @Override
        public boolean boolCheck(String text) {
            return text != null && pattern.matcher(text).matches();
        }
    },

    OPERATOR() {
        private Pattern pattern = Pattern.compile("^[\\d_a-zA-Z\\u4E00-\\u9FA5]{5,25}$");

        @Override
        public boolean boolCheck(String text) {
            return text != null && pattern.matcher(text).matches();
        }
    },

    /**
     * 商品标题限制 1~200
     */
    ITEM_TITLE() {
        private Pattern pattern = Pattern.compile("^.{1,200}$");

        @Override
        public boolean boolCheck(String text) {
            return text != null && pattern.matcher(text).matches();
        }
    },

    /**
     * 资源角色名 1~20
     */
    RES_ROLE() {
        private Pattern pattern = Pattern.compile("^[\\d_a-zA-Z\\u4E00-\\u9FA5]{1,20}$");

        @Override
        public boolean boolCheck(String text) {
            return text != null && pattern.matcher(text).matches();
        }
    },

    /**
     * 子域名规则
     * http://stackoverflow.com/a/7933253
     */
    SUB_DOMAIN() {
        private Pattern pattern = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9])?$");

        @Override
        public boolean boolCheck(String text) {
            return text != null && pattern.matcher(text).matches();
        }
    };

    abstract public boolean boolCheck(String text);

    public final String check(String text, String error) throws IllegalStateException {
        checkState(boolCheck(text), error);
        return text;
    }

    public final String check(String text) throws IllegalStateException {
        return check(text, null);
    }
}
