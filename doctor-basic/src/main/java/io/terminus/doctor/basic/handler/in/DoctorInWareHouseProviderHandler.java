package io.terminus.doctor.basic.handler.in;

import io.terminus.doctor.basic.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.EventHandlerContext;
import io.terminus.doctor.basic.handler.IHandler;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorMaterialInWareHouse;
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
public class DoctorInWareHouseProviderHandler implements IHandler{

    private final DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    @Autowired
    public DoctorInWareHouseProviderHandler(DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao){
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
    }

    @Override
    public boolean ifHandle(DoctorMaterialConsumeProvider.EVENT_TYPE eventType) {
        return eventType != null && eventType.isIn();
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context) throws RuntimeException {
        // 修改数量信息
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(
                dto.getFarmId(), dto.getWareHouseId(), dto.getMaterialTypeId());
        if(isNull(doctorMaterialInWareHouse)){
            // create material in warehouse
            doctorMaterialInWareHouse = buildDoctorMaterialInWareHouse(dto);
            doctorMaterialInWareHouseDao.create(doctorMaterialInWareHouse);
        }else {
            doctorMaterialInWareHouse.setLotNumber(doctorMaterialInWareHouse.getLotNumber() + dto.getCount());
            doctorMaterialInWareHouse.setUpdatorId(dto.getStaffId());
            doctorMaterialInWareHouse.setUpdatorName(dto.getStaffName());
            doctorMaterialInWareHouseDao.update(doctorMaterialInWareHouse);
        }
        context.setMaterialInWareHouseId(doctorMaterialInWareHouse.getId());
    }

    @Override
    public void rollback(DoctorMaterialConsumeProvider cp) {
        // 事件之后的库存数据
        DoctorMaterialInWareHouse materialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(cp.getFarmId(), cp.getWareHouseId(), cp.getMaterialId());
        checkState(!isNull(materialInWareHouse), "no.material.consume");
        // 把数量减回去
        materialInWareHouse.setLotNumber(materialInWareHouse.getLotNumber() - cp.getEventCount());
        doctorMaterialInWareHouseDao.update(materialInWareHouse);
    }

    /**
     * 构建仓库原料信息
     * @param dto
     */
    private DoctorMaterialInWareHouse buildDoctorMaterialInWareHouse(DoctorMaterialConsumeProviderDto dto){
        return DoctorMaterialInWareHouse.builder()
                .farmId(dto.getFarmId()).farmName(dto.getFarmName()).wareHouseId(dto.getWareHouseId()).wareHouseName(dto.getWareHouseName())
                .materialId(dto.getMaterialTypeId()).materialName(dto.getMaterialName()).lotNumber(dto.getCount()).type(dto.getType())
                .unitName(dto.getUnitName()).unitGroupName(dto.getUnitGroupName())
                .creatorId(dto.getStaffId()).creatorName(dto.getStaffName())
                .build();
    }
}
