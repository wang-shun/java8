package io.terminus.doctor.warehouse.handler.consume;

import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return dto.getActionType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {

        // 校验库存数量信息
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(
                dto.getFarmId(), dto.getWareHouseId(), dto.getMaterialTypeId());
        checkState(!isNull(doctorMaterialInWareHouse), "query.doctorMaterialConsume.fail");
        checkState(dto.getCount()<=doctorMaterialInWareHouse.getLotNumber(), "consume.not.enough");
        doctorMaterialInWareHouse.setLotNumber(doctorMaterialInWareHouse.getLotNumber() - dto.getCount());
        doctorMaterialInWareHouseDao.update(doctorMaterialInWareHouse);
    }
}
