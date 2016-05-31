package io.terminus.doctor.warehouse.handler.provider;

import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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

    private final DoctorMaterialInfoDao doctorMaterialInfoDao;

    @Autowired
    public DoctorInWareHouseProviderHandler(DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao,
                                            DoctorMaterialInfoDao doctorMaterialInfoDao){
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return dto.getActionType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
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
        context.put("materialInWareHouseId", doctorMaterialInWareHouse.getId());
    }

    /**
     * 构建仓库原料信息
     * @param dto
     */
    private DoctorMaterialInWareHouse buildDoctorMaterialInWareHouse(DoctorMaterialConsumeProviderDto dto){
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = DoctorMaterialInWareHouse.builder()
                .farmId(dto.getFarmId()).farmName(dto.getFarmName()).wareHouseId(dto.getWareHouseId()).wareHouseName(dto.getWareHouseName())
                .materialId(dto.getMaterialTypeId()).materialName(dto.getMaterialName()).lotNumber(dto.getCount()).type(dto.getType())
                .creatorId(dto.getStaffId()).creatorName(dto.getStaffName())
                .build();

        DoctorMaterialInfo materialInfo = doctorMaterialInfoDao.findById(dto.getMaterialTypeId());
        doctorMaterialInWareHouse.setUnitGroupName(materialInfo.getUnitGroupName());
        doctorMaterialInWareHouse.setUnitName(materialInfo.getUnitName());
        return doctorMaterialInWareHouse;
    }
}
