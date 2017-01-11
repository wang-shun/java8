package io.terminus.doctor.basic.handler.out;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.basic.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseSnapshotDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.EventHandlerContext;
import io.terminus.doctor.basic.handler.IHandler;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorWarehouseSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: 计算平均消耗信息
 */
@Component
@Slf4j
public class DoctorMaterialAvgConsumerHandler implements IHandler{

    private final DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao;
    private final DoctorWarehouseSnapshotDao doctorWarehouseSnapshotDao;

    @Autowired
    public DoctorMaterialAvgConsumerHandler(DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao,
                                            DoctorWarehouseSnapshotDao doctorWarehouseSnapshotDao){
        this.doctorMaterialConsumeAvgDao = doctorMaterialConsumeAvgDao;
        this.doctorWarehouseSnapshotDao = doctorWarehouseSnapshotDao;
    }

    @Override
    public boolean ifHandle(DoctorMaterialConsumeProvider.EVENT_TYPE eventType) {
        return Objects.equals(eventType, DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER);
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context) throws RuntimeException {
        Double lotNumber = context.getLotNumber();
        DoctorMaterialConsumeAvg doctorMaterialConsumeAvg = doctorMaterialConsumeAvgDao.queryByIds(dto.getFarmId(), dto.getWareHouseId(), dto.getMaterialTypeId());
        if(isNull(doctorMaterialConsumeAvg)){
            context.getSnapshot().setMaterialConsumeAvg(null);
            // create consume avg
            doctorMaterialConsumeAvg = DoctorMaterialConsumeAvg.builder()
                    .farmId(dto.getFarmId()).wareHouseId(dto.getWareHouseId()).materialId(dto.getMaterialTypeId())
                    .consumeDate(DateTime.now().withTimeAtStartOfDay().toDate()).consumeCount(dto.getCount())
                    .consumeAvgCount(0D)
                    .build();

            if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey()) && dto.getConsumeDays() != null){
                doctorMaterialConsumeAvg.setConsumeAvgCount(dto.getCount() * 100 / dto.getConsumeDays());   // * 100 默认精度 0.001
                if(doctorMaterialConsumeAvg.getConsumeAvgCount() != 0) {
                    doctorMaterialConsumeAvg.setLotConsumeDay((int) (lotNumber * 100 / doctorMaterialConsumeAvg.getConsumeAvgCount()));
                }
            }
            doctorMaterialConsumeAvgDao.create(doctorMaterialConsumeAvg);
        }else{
            context.getSnapshot().setMaterialConsumeAvg(BeanMapper.map(doctorMaterialConsumeAvg, DoctorMaterialConsumeAvg.class));
            if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
                // calculate current avg rate
                if(dto.getConsumeDays() != null){
                    doctorMaterialConsumeAvg.setConsumeAvgCount(dto.getCount() * 100 / dto.getConsumeDays());
                    doctorMaterialConsumeAvg.setConsumeCount(dto.getCount());
                    if(doctorMaterialConsumeAvg.getConsumeAvgCount() != 0) {
                        doctorMaterialConsumeAvg.setLotConsumeDay((int) (lotNumber * 100 / doctorMaterialConsumeAvg.getConsumeAvgCount()));
                    }
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
        context.setConsumeAvgId(doctorMaterialConsumeAvg.getId());
    }

    @Override
    public void rollback(DoctorMaterialConsumeProvider cp) {
        DoctorMaterialConsumeAvg consumeAvg = doctorMaterialConsumeAvgDao.queryByIds(cp.getFarmId(), cp.getWareHouseId(), cp.getMaterialId());
        // 消耗事件发生后, 一定有这个数据
        if(consumeAvg == null){
            throw new ServiceException("MaterialConsumeAvg.find.fail");
        }
        DoctorWarehouseSnapshot snapshot = doctorWarehouseSnapshotDao.findByEventId(cp.getId());
        if(snapshot == null){
            throw new ServiceException("snapshot.not.found");
        }
        DoctorMaterialConsumeAvg oldAvg = snapshot.json2Snapshot().getMaterialConsumeAvg();
        if(oldAvg == null){
            doctorMaterialConsumeAvgDao.delete(consumeAvg.getId());
        }else{
            doctorMaterialConsumeAvgDao.updateAll(oldAvg);
        }
    }
}
