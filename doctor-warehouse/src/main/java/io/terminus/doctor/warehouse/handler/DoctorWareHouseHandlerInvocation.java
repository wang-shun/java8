package io.terminus.doctor.warehouse.handler;

import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.EventHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorWareHouseHandlerInvocation {

    private final DoctorWareHouseHandlerChain doctorWareHouseHandlerChain;

    @Autowired
    public DoctorWareHouseHandlerInvocation(DoctorWareHouseHandlerChain chain){
        this.doctorWareHouseHandlerChain = chain;
    }

    public void invoke(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context){
        doctorWareHouseHandlerChain.getHandlerList().forEach(iHandler -> {
            if(iHandler.ifHandle(dto)){
                iHandler.handle(dto, context);
            }
        });
    }

    public void rollback(Long eventId){
        doctorWareHouseHandlerChain.getHandlerList().forEach(iHandler -> {
            if(iHandler.canRollback(eventId)){
                iHandler.rollback(eventId);
            }
        });
    }
}
