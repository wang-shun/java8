package io.terminus.doctor.event.handler;

import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorEventHandlerChainInvocation {

    @Autowired
    private DoctorEventHandlerChain doctorEventHandlerChain;

    /**
     * invoke handler info
     * @param basic
     * @param extra
     * @param context
     * @throws RuntimeException
     */
    public void invoke(DoctorBasicInputInfoDto basic, Map<String,Object> extra, Map<String,Object> context) throws RuntimeException{

        for (DoctorEventCreateHandler doctorEventCreateHandler : doctorEventHandlerChain.getDoctorEventCreateHandlers()) {
            if(doctorEventCreateHandler.preHandler(basic,extra,context)){
                doctorEventCreateHandler.handler(basic, extra, context);
                doctorEventCreateHandler.afterHandler(basic, extra, context);
            }
        }
    }
}
