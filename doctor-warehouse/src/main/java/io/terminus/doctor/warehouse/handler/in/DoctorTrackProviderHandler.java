package io.terminus.doctor.warehouse.handler.in;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.EventHandlerContext;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
@Slf4j
public class DoctorTrackProviderHandler implements IHandler{

    private final DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    @Autowired
    public DoctorTrackProviderHandler(DoctorWareHouseTrackDao doctorWareHouseTrackDao){
        this.doctorWareHouseTrackDao = doctorWareHouseTrackDao;
    }

    @Override
    public boolean ifHandle(DoctorMaterialConsumeProviderDto dto) {
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getActionType());
        return eventType != null && eventType.isIn();    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context) throws RuntimeException {
        // 修改仓库数量信息
        DoctorWareHouseTrack doctorWareHouseTrack = doctorWareHouseTrackDao.findById(dto.getWareHouseId());
        if(isNull(doctorWareHouseTrack)){
            doctorWareHouseTrackDao.create(buildDoctorWreHouseTrack(dto));
        }else {
            doctorWareHouseTrack.setLotNumber(doctorWareHouseTrack.getLotNumber() + dto.getCount());
            String key = dto.getMaterialTypeId().toString();
            Map<String,Object> trackExtraMap = doctorWareHouseTrack.getExtraMap();
            if (trackExtraMap == null) trackExtraMap = Maps.newHashMap();
            if(trackExtraMap.containsKey(key)){
                trackExtraMap.put(key, dto.getCount() + Params.getWithConvert(trackExtraMap, key, a -> Double.valueOf(a.toString())));
            }else {
                trackExtraMap.put(key, dto.getCount());
            }
            doctorWareHouseTrack.setExtraMap(trackExtraMap);
            doctorWareHouseTrackDao.update(doctorWareHouseTrack);
        }
    }

    @Override
    public boolean canRollback(Long eventId) {
        return false;
    }

    @Override
    public void rollback(Long eventId) {

    }

    /**
     * 构建仓库的信息
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    private DoctorWareHouseTrack buildDoctorWreHouseTrack(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        DoctorWareHouseTrack track = DoctorWareHouseTrack.builder()
                .farmId(doctorMaterialConsumeProviderDto.getFarmId()).farmName(doctorMaterialConsumeProviderDto.getFarmName()).wareHouseId(doctorMaterialConsumeProviderDto.getWareHouseId())
                .managerId(doctorMaterialConsumeProviderDto.getStaffId()).managerName(doctorMaterialConsumeProviderDto.getStaffName())
                .lotNumber(doctorMaterialConsumeProviderDto.getCount())
                .build();

        track.setExtraMap(ImmutableMap.of(doctorMaterialConsumeProviderDto.getMaterialTypeId().toString(), doctorMaterialConsumeProviderDto.getCount()));
        return track;
    }
}
