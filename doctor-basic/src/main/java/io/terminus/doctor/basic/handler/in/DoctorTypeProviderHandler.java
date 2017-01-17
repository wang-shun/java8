package io.terminus.doctor.basic.handler.in;

import io.terminus.doctor.basic.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.EventHandlerContext;
import io.terminus.doctor.basic.handler.IHandler;
import io.terminus.doctor.basic.model.DoctorFarmWareHouseType;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorTypeProviderHandler implements IHandler{

    private final DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;

    @Autowired
    public DoctorTypeProviderHandler(DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao){
        this.doctorFarmWareHouseTypeDao = doctorFarmWareHouseTypeDao;
    }

    @Override
    public boolean ifHandle(DoctorMaterialConsumeProvider.EVENT_TYPE eventType) {
        return eventType != null && eventType.isIn();
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context) throws RuntimeException {
        // 修改猪场仓库类型的数量信息
        DoctorFarmWareHouseType doctorFarmWareHouseType = doctorFarmWareHouseTypeDao.findByFarmIdAndType(
                dto.getFarmId(), dto.getType());
        if(isNull(doctorFarmWareHouseType)){
            doctorFarmWareHouseType = buildDoctorWareHouseType(dto);
            doctorFarmWareHouseTypeDao.create(doctorFarmWareHouseType);
        }else {
            doctorFarmWareHouseType.setLotNumber(doctorFarmWareHouseType.getLotNumber() + dto.getCount());
            doctorFarmWareHouseType.setUpdatorId(dto.getStaffId());
            doctorFarmWareHouseType.setUpdatorName(dto.getStaffName());
            doctorFarmWareHouseTypeDao.update(doctorFarmWareHouseType);
        }
        context.setWareHouseTypeId(doctorFarmWareHouseType.getId());
    }

    @Override
    public void rollback(DoctorMaterialConsumeProvider cp) {
        DoctorFarmWareHouseType farmWareHouseType = doctorFarmWareHouseTypeDao.findByFarmIdAndType(cp.getFarmId(), cp.getType());
        checkState(!isNull(farmWareHouseType), "doctorFarm.wareHouseType.empty");
        farmWareHouseType.setLotNumber(farmWareHouseType.getLotNumber() - cp.getEventCount());
        doctorFarmWareHouseTypeDao.update(farmWareHouseType);
    }

    /**
     * build 对应的猪场类型
     * @param dto
     * @return
     */
    private DoctorFarmWareHouseType buildDoctorWareHouseType(DoctorMaterialConsumeProviderDto dto){
        return DoctorFarmWareHouseType.builder()
                .farmId(dto.getFarmId()).farmName(dto.getFarmName()).type(dto.getType())
                .lotNumber(dto.getCount()).creatorId(dto.getStaffId()).creatorName(dto.getStaffName())
                .build();
    }
}
