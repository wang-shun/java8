package io.terminus.doctor.warehouse.handler.provider;

import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialPriceInWareHouseDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialPriceInWareHouse;
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
public class DoctorProviderEventHandler implements IHandler{

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;
    private final DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao;

    @Autowired
    public DoctorProviderEventHandler(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao,
                                      DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao){
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
        this.doctorMaterialPriceInWareHouseDao = doctorMaterialPriceInWareHouseDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return dto.getActionType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider = DoctorMaterialConsumeProvider.buildFromDto(dto);
        doctorMaterialConsumeProviderDao.create(doctorMaterialConsumeProvider);
        doctorMaterialPriceInWareHouseDao.create(DoctorMaterialPriceInWareHouse.buildFromDto(dto, doctorMaterialConsumeProvider.getId()));
        context.put("eventId",doctorMaterialConsumeProvider.getId());
    }
}
