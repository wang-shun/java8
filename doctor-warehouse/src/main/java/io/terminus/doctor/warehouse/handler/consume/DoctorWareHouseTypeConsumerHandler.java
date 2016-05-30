package io.terminus.doctor.warehouse.handler.consume;

import com.google.common.collect.Maps;
import io.terminus.doctor.warehouse.constants.DoctorFarmWareHouseTypeConstants;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: 大类warehouse type 类型的修改方式   TODO 添加平均消耗日期信息
 */
@Component
public class DoctorWareHouseTypeConsumerHandler implements IHandler{

    private final DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;

    @Autowired
    public DoctorWareHouseTypeConsumerHandler(DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao){
        this.doctorFarmWareHouseTypeDao = doctorFarmWareHouseTypeDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return dto.getActionType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        // update ware house type count
        DoctorFarmWareHouseType doctorFarmWareHouseType = doctorFarmWareHouseTypeDao.findByFarmIdAndType(
                dto.getFarmId(), dto.getType());
        checkState(!isNull(doctorFarmWareHouseType), "doctorFarm.wareHouseType.empty");
        doctorFarmWareHouseType.setLogNumber(doctorFarmWareHouseType.getLogNumber() - dto.getCount());
        Map<String,Object> extraMap = isNull(doctorFarmWareHouseType.getExtraMap())? Maps.newHashMap() :doctorFarmWareHouseType.getExtraMap();
        if(extraMap.containsKey(DoctorFarmWareHouseTypeConstants.CONSUME_DATE) &&
                DateTime.now().withTimeAtStartOfDay().isEqual(Long.valueOf(extraMap.get(DoctorFarmWareHouseTypeConstants.CONSUME_DATE).toString()))){
            extraMap.put(DoctorFarmWareHouseTypeConstants.CONSUME_COUNT,
                    Long.valueOf(extraMap.get(DoctorFarmWareHouseTypeConstants.CONSUME_COUNT).toString()) + dto.getCount());
        }else {
            extraMap.put(DoctorFarmWareHouseTypeConstants.CONSUME_DATE, DateTime.now().withTimeAtStartOfDay().getMillis());
            extraMap.put(DoctorFarmWareHouseTypeConstants.CONSUME_COUNT, dto.getCount());
        }
        doctorFarmWareHouseType.setExtraMap(extraMap);
        doctorFarmWareHouseTypeDao.update(doctorFarmWareHouseType);
    }
}
