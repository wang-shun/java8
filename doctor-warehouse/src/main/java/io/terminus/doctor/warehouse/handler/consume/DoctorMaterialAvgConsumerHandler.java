package io.terminus.doctor.warehouse.handler.consume;

import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: 计算平均消耗信息 TODO
 */
@Component
@Slf4j
public class DoctorMaterialAvgConsumerHandler implements IHandler{

    private final DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao;

    @Autowired
    public DoctorMaterialAvgConsumerHandler(DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao){
        this.doctorMaterialConsumeAvgDao = doctorMaterialConsumeAvgDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return Objects.equals(dto.getActionType(), DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        Long lotNumber = (Long) context.get("lotNumber");
        DoctorMaterialConsumeAvg doctorMaterialConsumeAvg = doctorMaterialConsumeAvgDao.queryByIds(dto.getFarmId(), dto.getWareHouseId(), dto.getMaterialTypeId());
        if(isNull(doctorMaterialConsumeAvg)){
            // create consume avg
            doctorMaterialConsumeAvg = DoctorMaterialConsumeAvg.builder()
                    .farmId(dto.getFarmId()).wareHouseId(dto.getWareHouseId()).materialId(dto.getMaterialTypeId())
                    .consumeDate(DateTime.now().withTimeAtStartOfDay().toDate()).consumeCount(dto.getCount())
                    .consumeAvgCount(0D)
                    .build();

            if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
                doctorMaterialConsumeAvg.setConsumeAvgCount(dto.getCount() * 100 / dto.getConsumeDays());   // * 100 默认精度 0.001
                if(doctorMaterialConsumeAvg.getConsumeAvgCount() != 0) {
                    doctorMaterialConsumeAvg.setLotConsumeDay((int) (lotNumber * 100 / doctorMaterialConsumeAvg.getConsumeAvgCount()));
                }
            }
            doctorMaterialConsumeAvgDao.create(doctorMaterialConsumeAvg);
        }else{
            if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
                // calculate current avg rate
                doctorMaterialConsumeAvg.setConsumeAvgCount(dto.getCount() * 100 / dto.getConsumeDays());
                doctorMaterialConsumeAvg.setConsumeCount(dto.getCount());
                if(doctorMaterialConsumeAvg.getConsumeAvgCount() != 0) {
                    doctorMaterialConsumeAvg.setLotConsumeDay((int) (lotNumber * 100 / doctorMaterialConsumeAvg.getConsumeAvgCount()));
                }
            }else {
                Integer dayRange = Days.daysBetween(new DateTime(doctorMaterialConsumeAvg.getConsumeDate()), DateTime.now()).getDays();

                if(dayRange == 0){
                    // 同一天领用 0
                    doctorMaterialConsumeAvg.setConsumeCount(doctorMaterialConsumeAvg.getConsumeCount() + dto.getCount());
                }else {
                    // calculate avg date content
                    doctorMaterialConsumeAvg.setConsumeAvgCount(doctorMaterialConsumeAvg.getConsumeCount() * 100 / dayRange);
                    doctorMaterialConsumeAvg.setConsumeCount(dto.getCount());
                    if(doctorMaterialConsumeAvg.getConsumeAvgCount() != 0) {
                        doctorMaterialConsumeAvg.setLotConsumeDay((int) (lotNumber * 100 / doctorMaterialConsumeAvg.getConsumeAvgCount()));
                    }
                }
            }
            doctorMaterialConsumeAvg.setConsumeDate(DateTime.now().withTimeAtStartOfDay().toDate());
            doctorMaterialConsumeAvgDao.update(doctorMaterialConsumeAvg);
        }
        context.put("consumeAvgId",doctorMaterialConsumeAvg.getId());
    }
}
