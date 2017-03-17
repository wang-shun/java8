package io.terminus.doctor.basic.handler;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseSnapshotDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.EventHandlerContext;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorWarehouseSnapshot;
import io.terminus.doctor.common.utils.JsonMapperUtil;
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
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getActionType());
        doctorWareHouseHandlerChain.getHandlerList().forEach(iHandler -> {
            if(iHandler.ifHandle(eventType)){
                iHandler.handle(dto, context);
            }
        });

        // 出库时记录快照, 因为入库的回滚没有用到快照
        if(eventType != null && eventType.isOut()){
            doctorWarehouseSnapshotDao.create(
                    DoctorWarehouseSnapshot.builder()
                            .eventId(context.getEventId())
                            .beforeEvent(JsonMapperUtil.JSON_NON_EMPTY_MAPPER.toJson(context.getSnapshot()))
                            .build()
            );
        }
    }

    public void rollback(DoctorMaterialConsumeProvider cp){
        doctorWareHouseHandlerChain.getHandlerList().forEach(iHandler -> {
            if(iHandler.ifHandle(DoctorMaterialConsumeProvider.EVENT_TYPE.from(cp.getEventType()))) {
                iHandler.rollback(cp);
            }
        });
        // 删除快照
        doctorWarehouseSnapshotDao.deleteByEventId(cp.getId());
    }
}
