package io.terminus.doctor.warehouse.handler.out;

import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.EventHandlerContext;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: 处理Material In WareHouse
 */
@Component
@Slf4j
public class DoctorInWareHouseConsumeHandler implements IHandler{

    private final DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    @Autowired
    public DoctorInWareHouseConsumeHandler(DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao){
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
    }

    @Override
    public boolean ifHandle(DoctorMaterialConsumeProviderDto dto) {
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getActionType());
        return eventType != null && eventType.isOut();
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context) throws RuntimeException {

        // 校验库存数量信息
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(
                dto.getFarmId(), dto.getWareHouseId(), dto.getMaterialTypeId());
        checkState(!isNull(doctorMaterialInWareHouse), "no.material.consume");
        checkState(dto.getCount()<=doctorMaterialInWareHouse.getLotNumber(), "consume.not.enough");
        doctorMaterialInWareHouse.setLotNumber(doctorMaterialInWareHouse.getLotNumber() - dto.getCount());
        doctorMaterialInWareHouseDao.update(doctorMaterialInWareHouse);
        context.setLotNumber(doctorMaterialInWareHouse.getLotNumber());
    }

    @Override
    public boolean canRollback(DoctorMaterialConsumeProvider cp) {
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(cp.getEventType());
        return eventType != null && eventType.isOut();
    }

    @Override
    public void rollback(DoctorMaterialConsumeProvider cp) {
        DoctorMaterialInWareHouse materialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(
                cp.getFarmId(), cp.getWareHouseId(), cp.getMaterialId());
        checkState(!isNull(materialInWareHouse), "no.material.consume");
        materialInWareHouse.setLotNumber(materialInWareHouse.getLotNumber() + cp.getEventCount());
        doctorMaterialInWareHouseDao.update(materialInWareHouse);
    }
}
