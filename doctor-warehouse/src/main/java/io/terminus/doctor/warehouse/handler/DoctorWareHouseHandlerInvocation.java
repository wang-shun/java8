package io.terminus.doctor.warehouse.handler;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorWarehouseSnapshotDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.EventHandlerContext;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorWarehouseSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorWareHouseHandlerInvocation {

    private final DoctorWareHouseHandlerChain doctorWareHouseHandlerChain;
    private final DoctorWarehouseSnapshotDao doctorWarehouseSnapshotDao;
    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public DoctorWareHouseHandlerInvocation(DoctorWareHouseHandlerChain chain,
                                            DoctorWarehouseSnapshotDao doctorWarehouseSnapshotDao,
                                            DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao){
        this.doctorWareHouseHandlerChain = chain;
        this.doctorWarehouseSnapshotDao = doctorWarehouseSnapshotDao;
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
    }

    public void invoke(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context){
        doctorWareHouseHandlerChain.getHandlerList().forEach(iHandler -> {
            if(iHandler.ifHandle(dto)){
                iHandler.handle(dto, context);
            }
        });
        doctorWarehouseSnapshotDao.create(
                DoctorWarehouseSnapshot.builder()
                        .eventId(context.getEventId())
                        .beforeEvent(JsonMapper.JSON_NON_EMPTY_MAPPER.toJson(context.getSnapshot()))
                        .build()
        );
    }

    public void rollback(Long eventId){
        DoctorMaterialConsumeProvider cp = doctorMaterialConsumeProviderDao.findById(eventId);
        if(cp == null){
            throw new ServiceException("event.not.found");
        }
        doctorWareHouseHandlerChain.getHandlerList().forEach(iHandler -> {
            if(iHandler.canRollback(cp)){
                iHandler.rollback(cp);
            }
        });
        // 删除快照
        doctorWarehouseSnapshotDao.deleteByEventId(eventId);
    }
}
