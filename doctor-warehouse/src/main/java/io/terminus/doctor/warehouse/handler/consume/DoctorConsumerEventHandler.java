package io.terminus.doctor.warehouse.handler.consume;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: 创建消耗事件信息内容
 */
@Component
public class DoctorConsumerEventHandler implements IHandler{

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public DoctorConsumerEventHandler(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao){
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return dto.getActionType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider = DoctorMaterialConsumeProvider.buildFromDto(dto);
        if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
            doctorMaterialConsumeProvider.setExtraMap(ImmutableMap.of(
                    "consumeDays", dto.getConsumeDays(),
                    "barnId", dto.getBarnId(),
                    "barnName", dto.getBarnName()));
        }
        doctorMaterialConsumeProviderDao.create(doctorMaterialConsumeProvider);
        context.put("eventId",doctorMaterialConsumeProvider.getId());
    }
}
