package io.terminus.doctor.web.core.login;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-23 3:46 PM  <br>
 * Author: xiao
 */
public class Sessions {
    public static final String TOKEN_PREFIX = "afsession";
    public static final String CODE_PREFIX = "code";
    public static final String MSG_PREFIX = "message";
    public static final String LIMIT_PREFIX = "limit";

    public static final String USER_ID = "userId";

    //设备号
    public static final String DEVICE_ID = "session_device_id";

    // 3 days
    public static final int LONG_INACTIVE_INTERVAL = 259200;
    // 15 mins
    public static final int MIDDLE_INACTIVE_INTERVAL = 900;
    // 5 mins
    public static final int SHORT_INACTIVE_INTERVAL = 300;
}
