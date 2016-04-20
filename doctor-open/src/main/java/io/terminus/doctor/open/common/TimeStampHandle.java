package io.terminus.doctor.open.common;

import io.terminus.pampas.openplatform.core.ParamPreHandle;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.stereotype.Component;

import java.util.Map;

import static io.terminus.common.utils.Arguments.isEmpty;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-17 8:19 PM  <br>
 * Author: xiao
 */
@Slf4j
//@Component
public class TimeStampHandle implements ParamPreHandle {

    private static final int REGION = 3600;


    /**
     * check params before calling
     *
     * @param params params from http request
     * @throws OPClientException
     */
    @Override
    public void handle(Map<String, Object> params) throws OPClientException {
        String key = (String) params.get("pampasCall");
        if ("server.time".equals(key)) {
            // let it go
            return;
        }

        String timestamp = (String)params.get("timestamp");

        if (isEmpty(timestamp)) {
            log.error("no timestamp specified");
            throw new OPClientException(400, "timestamp.miss");
        }


        //4. check timestamp
        DateTime time;
        try{
            time = DateTime.parse(timestamp, DateTimeFormat.forPattern("yyyyMMddHHmmss"));
        } catch (IllegalArgumentException e) {
            throw new OPClientException(400, "timestamp.incorrect");
        }

        // check if  time now - region seconds <= invoke time <= time now + region seconds
        DateTime now = DateTime.now();
        if (now.plusSeconds(REGION).isBefore(time) ||
                now.minusSeconds(REGION).isAfter(time)) {
            throw new OPClientException(401, "invoke.expired");
        }
    }
}
