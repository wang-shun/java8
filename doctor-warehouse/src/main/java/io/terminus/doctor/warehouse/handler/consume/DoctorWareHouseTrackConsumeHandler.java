package io.terminus.doctor.warehouse.handler.consume;

import io.terminus.doctor.warehouse.constants.DoctorWareHouseTrackConstants;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorWareHouseTrackConsumeHandler implements IHandler{

    private final DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    @Autowired
    public DoctorWareHouseTrackConsumeHandler(DoctorWareHouseTrackDao doctorWareHouseTrackDao){
        this.doctorWareHouseTrackDao = doctorWareHouseTrackDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return Objects.equals(dto.getActionType(), DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        // update warehouse track
        DoctorWareHouseTrack doctorWareHouseTrack = this.doctorWareHouseTrackDao.findById(dto.getWareHouseId());
        checkState(!isNull(doctorWareHouseTrack), "not.find.doctorWareHouse");
        doctorWareHouseTrack.setLotNumber(doctorWareHouseTrack.getLotNumber() - dto.getCount());

        // track中 存放 不同material 数量信息
        Map<String, Object> consumeMap = doctorWareHouseTrack.getExtraMap();
        Long count = Long.valueOf(consumeMap.get(dto.getMaterialTypeId().toString()).toString());
        consumeMap.put(dto.getMaterialTypeId().toString(), count - dto.getCount());
        consumeMap.put(DoctorWareHouseTrackConstants.RECENT_CONSUME_DATE, DateTime.now().toDate());
        doctorWareHouseTrack.setExtraMap(consumeMap);
        doctorWareHouseTrackDao.update(doctorWareHouseTrack);
    }
}
