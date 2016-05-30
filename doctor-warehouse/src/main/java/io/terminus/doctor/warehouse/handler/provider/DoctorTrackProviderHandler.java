package io.terminus.doctor.warehouse.handler.provider;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
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
public class DoctorTrackProviderHandler implements IHandler{

    private final DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    @Autowired
    public DoctorTrackProviderHandler(DoctorWareHouseTrackDao doctorWareHouseTrackDao){
        this.doctorWareHouseTrackDao = doctorWareHouseTrackDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return dto.getActionType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        // 修改仓库数量信息
        DoctorWareHouseTrack doctorWareHouseTrack = doctorWareHouseTrackDao.findById(dto.getWareHouseId());
        if(isNull(doctorWareHouseTrack)){
            doctorWareHouseTrackDao.create(buildDoctorWreHouseTrack(dto));
        }else {
            doctorWareHouseTrack.setLotNumber(doctorWareHouseTrack.getLotNumber() + dto.getCount());
            String key = dto.getMaterialTypeId().toString();
            Map<String,Object> trackExtraMap = doctorWareHouseTrack.getExtraMap();
            if(trackExtraMap.containsKey(key)){
                trackExtraMap.put(key, dto.getCount() + Params.getWithConvert(trackExtraMap, key, a -> Long.valueOf(a.toString())));
            }else {
                trackExtraMap.put(key, dto.getCount());
            }
            doctorWareHouseTrack.setExtraMap(trackExtraMap);
            doctorWareHouseTrackDao.update(doctorWareHouseTrack);
        }
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
