package io.terminus.doctor.warehouse.manager;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeDto;
import io.terminus.doctor.warehouse.enums.WareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe: 物料消耗 manager 信息管理方式
 */
@Slf4j
@Component
public class MaterialInWareHouseManager {

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    private final DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    private final DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao;

    public MaterialInWareHouseManager(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao,
                                      DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao,DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao){
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
        this.doctorMaterialConsumeAvgDao = doctorMaterialConsumeAvgDao;
    }

    /**
     * 用户消耗信息录入内容
     * @param doctorMaterialConsumeDto
     * @return
     */
    @Transactional
    public Boolean consumeMaterialInWareHouse(DoctorMaterialConsumeDto doctorMaterialConsumeDto){

        // 校验库存数量信息
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByIds(doctorMaterialConsumeDto.getFarmId(),
                doctorMaterialConsumeDto.getWareHouseId(),doctorMaterialConsumeDto.getMaterialTypeId());

        checkState(!isNull(doctorMaterialConsumeDto), "query.doctorMaterialConsume.fail");
        checkState(doctorMaterialConsumeDto.getConsumeCount()<=doctorMaterialInWareHouse.getLotNumber(), "consume.not.enough");

        doctorMaterialInWareHouse.setLotNumber(doctorMaterialInWareHouse.getLotNumber() - doctorMaterialConsumeDto.getConsumeCount());
        doctorMaterialInWareHouseDao.update(doctorMaterialInWareHouse);

        //录入事件信息
        doctorMaterialConsumeProviderDao.create(builderMaterialEvent(doctorMaterialConsumeDto));

        //计算平均消耗信息
        updateConsumeCount(doctorMaterialConsumeDto);

        //更新的猪场WareHouse

        //更新猪场整体的数量信息

        return Boolean.FALSE;
    }

    /**
     * 统计对应的原料信息消耗列表信息
     * @param dto
     */
    private void updateConsumeCount(DoctorMaterialConsumeDto dto){
        DoctorMaterialConsumeAvg doctorMaterialConsumeAvg = doctorMaterialConsumeAvgDao.queryByIds(dto.getFarmId(), dto.getWareHouseId(), dto.getMaterialTypeId());
        if(isNull(doctorMaterialConsumeAvg)){
            // create consume avg
            DoctorMaterialConsumeAvg avg = DoctorMaterialConsumeAvg.builder()
                    .farmId(dto.getFarmId()).wareHouseId(dto.getWareHouseId()).materialId(dto.getMaterialTypeId())
                    .consumeDate(DateTime.now().toDate()).consumeCount(dto.getConsumeCount())
                    .build();

            if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
                avg.setConsumeAvgCount(dto.getConsumeCount() / dto.getConsumeDays());
            }
            doctorMaterialConsumeAvgDao.create(avg);
        }else{
            doctorMaterialConsumeAvg.setConsumeCount(dto.getConsumeCount());
            doctorMaterialConsumeAvg.setConsumeDate(DateTime.now().toDate());
            if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
                // calculate current avg rate
                doctorMaterialConsumeAvg.setConsumeAvgCount(dto.getConsumeCount() / dto.getConsumeDays());
            }else {
                // calculate avg date content
                doctorMaterialConsumeAvg.setConsumeAvgCount(
                        doctorMaterialConsumeAvg.getConsumeCount() /
                                Days.daysBetween(new DateTime(doctorMaterialConsumeAvg.getConsumeDate()),DateTime.now()).getDays());
            }
            doctorMaterialConsumeAvgDao.create(doctorMaterialConsumeAvg);
        }
    }

    /**
     * 构造用户消耗事件
     * @param doctorMaterialConsumeDto
     * @return
     */
    private DoctorMaterialConsumeProvider builderMaterialEvent(DoctorMaterialConsumeDto doctorMaterialConsumeDto){
        DoctorMaterialConsumeProvider result = DoctorMaterialConsumeProvider.builder()
                .type(doctorMaterialConsumeDto.getType())
                .farmId(doctorMaterialConsumeDto.getFarmId()).farmName(doctorMaterialConsumeDto.getFarmName())
                .wareHouseId(doctorMaterialConsumeDto.getWareHouseId()).wareHouseName(doctorMaterialConsumeDto.getWareHouseName())
                .materialId(doctorMaterialConsumeDto.getMaterialTypeId()).materialName(doctorMaterialConsumeDto.getMaterialName())
                .eventType(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue()).eventTime(DateTime.now().toDate()).eventCount(doctorMaterialConsumeDto.getConsumeCount())
                .staffId(doctorMaterialConsumeDto.getStaffId()).staffName(doctorMaterialConsumeDto.getStaffName())
                .creatorId(doctorMaterialConsumeDto.getStaffId()).creatorName(doctorMaterialConsumeDto.getStaffName())
                .createdAt(DateTime.now().toDate())
                .build();

        // validate feed 类型， 统计消耗天数
        result.setExtraMap(ImmutableMap.of("consumeDays",doctorMaterialConsumeDto.getConsumeDays()));

        return result;
    }
}
