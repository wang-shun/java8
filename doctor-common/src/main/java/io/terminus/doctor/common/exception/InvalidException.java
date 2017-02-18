/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.common.exception;

import com.google.common.base.MoreObjects;
import lombok.Getter;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2016-02-27
 */
public class InvalidException extends RuntimeException {
    private static final long serialVersionUID = -3978990660036533916L;

    /**
     * 是否是批量方法
     */
    @Getter
    private boolean isBatchEvent = false;

    /**
     * 附加值(批量方法使用)
     */
    @Getter
    private String attach;

    @Getter
    private final int status;

    /**
     * 校验失败信息
     */
    @Getter
    private final String error;

    /**
     * 校验失败的一些参数说明信息
     */
    @Getter
    private final Object[] params;

    public InvalidException(int status, String error, boolean isBatchEvent, String attach, Object... params){
        this.status = status;
        this.error = error;
        this.isBatchEvent = isBatchEvent;
        this.attach = attach;
        this.params = params;
    }

    public InvalidException(boolean isBatchEvent, String error, String attach, Object... params){
        this(500, error, isBatchEvent, attach, params);
    }

    public InvalidException(String error, Object... param) {
        this(500, error, false, null, param);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("error", error)
                .add("params", params)
                .omitNullValues()
                .toString();
    }
}
