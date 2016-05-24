package io.terminus.doctor.warehouse.manager;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.warehouse.enums.WareHouseType;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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

    private final DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    private final DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;

    private final DoctorMaterialInfoDao doctorMaterialInfoDao;

    @Autowired
    public MaterialInWareHouseManager(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao,
                                      DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao,
                                      DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao,
                                      DoctorWareHouseTrackDao doctorWareHouseTrackDao,
                                      DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao,
                                      DoctorMaterialInfoDao doctorMaterialInfoDao){
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
        this.doctorMaterialConsumeAvgDao = doctorMaterialConsumeAvgDao;
        this.doctorWareHouseTrackDao = doctorWareHouseTrackDao;
        this.doctorFarmWareHouseTypeDao = doctorFarmWareHouseTypeDao;
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
    }

    // 生产对应的物料内容
    public Boolean produceMaterialInfo(DoctorWareHouseBasicDto basicDto, DoctorWareHouse targetHouse, DoctorMaterialInfo targetMaterial, DoctorMaterialInfo.MaterialProduce materialProduce){

        // consume each source
        materialProduce.getMaterialProduceEntries().forEach(m->
            produceMaterialConsumeEntry(basicDto, m));

        materialProduce.getMedicalProduceEntries().forEach(m->
            produceMaterialConsumeEntry(basicDto, m));

        // provider source
        providerMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto.builder()
                .type(targetMaterial.getType()).farmId(targetMaterial.getFarmId()).farmName(targetMaterial.getFarmName())
                .materialTypeId(targetMaterial.getId()).materialName(targetMaterial.getMaterialName())
                .wareHouseId(targetHouse.getId()).wareHouseName(targetHouse.getWareHouseName())
                .barnId(basicDto.getBarnId()).barnName(basicDto.getBarnName()).staffId(basicDto.getStaffId()).staffName(basicDto.getStaffName())
                .consumeCount(materialProduce.getTotal()).unitId(targetMaterial.getUnitId()).unitName(targetMaterial.getUnitName())
                .build());

        return Boolean.TRUE;
    }

    private void produceMaterialConsumeEntry(DoctorWareHouseBasicDto basicDto, DoctorMaterialInfo.MaterialProduceEntry materialProduceEntry){
        // get material
        DoctorMaterialInfo materialInfo = doctorMaterialInfoDao.findById(materialProduceEntry.getMaterialId());
        checkState(!isNull(materialInfo), "produce.sourceMaterial.empty");

        List<DoctorMaterialInWareHouse> doctorMaterialInWareHouses = doctorMaterialInWareHouseDao.queryByFarmMaterial(basicDto.getFarmId(), materialInfo.getId());

        long totalCount = doctorMaterialInWareHouses.stream().map(DoctorMaterialInWareHouse::getLotNumber).reduce(Math::addExact).orElse(0l);
        checkState(totalCount >= materialProduceEntry.getMaterialCount(), "not.enough.source");

        // consume
        int index = 0;

        while (totalCount!=0){
            DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouses.get(index);
            long toConsume = doctorMaterialInWareHouse.getLotNumber();

            if(toConsume>=totalCount){
                toConsume = totalCount;
                totalCount = 0;
            }else {
                totalCount = totalCount - toConsume;
            }

            consumeMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto.builder()
                    .type(materialInfo.getType()).farmId(materialInfo.getFarmId()).farmName(materialInfo.getFarmName())
                    .materialTypeId(materialInfo.getId()).materialName(materialInfo.getMaterialName())
                    .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                    .barnId(basicDto.getBarnId()).barnName(basicDto.getBarnName()).staffId(basicDto.getStaffId()).staffName(basicDto.getStaffName())
                    .consumeCount(toConsume).unitId(materialInfo.getUnitId()).unitName(materialInfo.getUnitName())
                    .build());
        }

        return;
    }

    /**
     * 用户消耗信息录入内容
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    @Transactional
    public Boolean consumeMaterialInWareHouse(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){

        consumeMaterialInWareHouseInner(doctorMaterialConsumeProviderDto);

        return Boolean.TRUE;
    }

    /**
     * 用户信息的提供操作
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    @Transactional
    public Boolean providerMaterialInWareHouse(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        providerMaterialInWareHouseInner(doctorMaterialConsumeProviderDto);
        return Boolean.TRUE;
    }

    private void providerMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        // 录入事件信息
        doctorMaterialConsumeProviderDao.create(builderMaterialEvent(doctorMaterialConsumeProviderDto,DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue()));

        // 修改数量信息
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByIds(doctorMaterialConsumeProviderDto.getFarmId(),
                doctorMaterialConsumeProviderDto.getWareHouseId(), doctorMaterialConsumeProviderDto.getMaterialTypeId());
        if(isNull(doctorMaterialInWareHouse)){
            // create material in warehouse
            doctorMaterialInWareHouse = buildDoctorMaterialInWareHouse(doctorMaterialConsumeProviderDto);
            doctorMaterialInWareHouseDao.create(doctorMaterialInWareHouse);
        }else {
            doctorMaterialInWareHouse.setLotNumber(doctorMaterialInWareHouse.getLotNumber() + doctorMaterialConsumeProviderDto.getProviderCount());
            doctorMaterialInWareHouse.setUpdatorId(doctorMaterialConsumeProviderDto.getStaffId());
            doctorMaterialInWareHouse.setUpdatorName(doctorMaterialConsumeProviderDto.getStaffName());
            doctorMaterialInWareHouseDao.update(doctorMaterialInWareHouse);
        }


        // 修改仓库数量信息
        DoctorWareHouseTrack doctorWareHouseTrack = doctorWareHouseTrackDao.findById(doctorMaterialConsumeProviderDto.getWareHouseId());
        if(isNull(doctorWareHouseTrack)){
            doctorWareHouseTrackDao.create(buildDoctorWreHouseTrack(doctorMaterialConsumeProviderDto));
        }else {
            doctorWareHouseTrack.setLotNumber(doctorWareHouseTrack.getLotNumber() + doctorMaterialConsumeProviderDto.getProviderCount());
            String key = doctorMaterialConsumeProviderDto.getMaterialTypeId().toString();
            doctorWareHouseTrack.setExtraMap(ImmutableMap.of(key, Params.getNullDefualt(doctorWareHouseTrack.getExtraMap(), key, 0l) + doctorMaterialConsumeProviderDto.getProviderCount()));
            doctorWareHouseTrackDao.update(doctorWareHouseTrack);
        }

        // 修改猪场仓库类型的数量信息
        DoctorFarmWareHouseType doctorFarmWareHouseType = doctorFarmWareHouseTypeDao.findByFarmIdAndType(doctorMaterialConsumeProviderDto.getFarmId(), doctorMaterialConsumeProviderDto.getType());
        if(isNull(doctorFarmWareHouseType)){
            doctorFarmWareHouseTypeDao.create(doctorFarmWareHouseType);
        }else {
            doctorFarmWareHouseType.setLogNumber(doctorFarmWareHouseType.getLogNumber() + doctorMaterialConsumeProviderDto.getProviderCount());
            doctorFarmWareHouseType.setUpdatorId(doctorMaterialConsumeProviderDto.getStaffId());
            doctorFarmWareHouseType.setUpdatorName(doctorMaterialConsumeProviderDto.getStaffName());
            doctorFarmWareHouseTypeDao.update(doctorFarmWareHouseType);
        }
    }

    private void consumeMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        // 校验库存数量信息
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByIds(doctorMaterialConsumeProviderDto.getFarmId(),
                doctorMaterialConsumeProviderDto.getWareHouseId(), doctorMaterialConsumeProviderDto.getMaterialTypeId());

        checkState(!isNull(doctorMaterialConsumeProviderDto), "query.doctorMaterialConsume.fail");
        checkState(doctorMaterialConsumeProviderDto.getConsumeCount()<=doctorMaterialInWareHouse.getLotNumber(), "consume.not.enough");

        doctorMaterialInWareHouse.setLotNumber(doctorMaterialInWareHouse.getLotNumber() - doctorMaterialConsumeProviderDto.getConsumeCount());
        doctorMaterialInWareHouseDao.update(doctorMaterialInWareHouse);

        //录入事件信息
        doctorMaterialConsumeProviderDao.create(builderMaterialEvent(doctorMaterialConsumeProviderDto, DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue()));

        //计算平均消耗信息
        updateConsumeCount(doctorMaterialConsumeProviderDto);

        //更新的猪场WareHouseTrack

        //更新猪场整体的数量信息
        updateLotNumberInWareHouseType(doctorMaterialConsumeProviderDto);
    }

    /**
     * build 对应的猪场类型
     * @param dto
     * @return
     */
    private DoctorFarmWareHouseType buildDoctorWareHouseType(DoctorMaterialConsumeProviderDto dto){
         return DoctorFarmWareHouseType.builder()
                .farmId(dto.getFarmId()).farmName(dto.getFarmName()).type(dto.getType())
                .logNumber(dto.getProviderCount()).creatorId(dto.getStaffId()).creatorName(dto.getStaffName())
                .build();
    }

    /**
     * 构建仓库的信息
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    private DoctorWareHouseTrack buildDoctorWreHouseTrack(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        DoctorWareHouseTrack track = DoctorWareHouseTrack.builder()
                .farmId(doctorMaterialConsumeProviderDto.getFarmId()).farmName(doctorMaterialConsumeProviderDto.getFarmName()).wareHouseId(doctorMaterialConsumeProviderDto.getWareHouseId())
                .managerId(null).managerName("")    // TODO 每种物料是否设定对应的管理员信息
                .lotNumber(doctorMaterialConsumeProviderDto.getProviderCount())
                .build();

        track.setExtraMap(ImmutableMap.of(doctorMaterialConsumeProviderDto.getMaterialTypeId().toString(),doctorMaterialConsumeProviderDto.getProviderCount()));
        return track;
    }

    /**
     * 构建仓库原料信息
     * @param dto
     */
    private DoctorMaterialInWareHouse buildDoctorMaterialInWareHouse(DoctorMaterialConsumeProviderDto dto){
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = DoctorMaterialInWareHouse.builder()
                .farmId(dto.getFarmId()).farmName(dto.getFarmName()).wareHouseId(dto.getWareHouseId()).wareHouseName(dto.getWareHouseName())
                .materialId(dto.getMaterialTypeId()).materialName(dto.getMaterialName()).lotNumber(dto.getProviderCount()).type(dto.getType())
                .creatorId(dto.getStaffId()).creatorName(dto.getStaffName())
                .build();

        DoctorMaterialInfo materialInfo = doctorMaterialInfoDao.findById(dto.getMaterialTypeId());

        doctorMaterialInWareHouse.setUnitGroupName(materialInfo.getUnitGroupName());
        doctorMaterialInWareHouse.setUnitName(materialInfo.getUnitName());
        return doctorMaterialInWareHouse;
    }

    /**
     * 修改仓库Track， 猪场type 数量信息内容
     * @param doctorMaterialConsumeProviderDto
     */
    private void updateLotNumberInWareHouseType(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        // update warehouse track
        DoctorWareHouseTrack doctorWareHouseTrack = this.doctorWareHouseTrackDao.findById(doctorMaterialConsumeProviderDto.getWareHouseId());
        checkState(!isNull(doctorWareHouseTrack), "not.find.doctorWareHouse");
        doctorWareHouseTrack.setLotNumber(doctorWareHouseTrack.getLotNumber() - doctorMaterialConsumeProviderDto.getConsumeCount());

        Map<String, Object> consumeMap = doctorWareHouseTrack.getExtraMap();
        Long count = Params.getWithOutNull(consumeMap, doctorMaterialConsumeProviderDto.getMaterialTypeId().toString());
        consumeMap.put(doctorMaterialConsumeProviderDto.getMaterialTypeId().toString(), count - doctorMaterialConsumeProviderDto.getConsumeCount());
        doctorWareHouseTrack.setExtraMap(consumeMap);

        doctorWareHouseTrackDao.update(doctorWareHouseTrack);

        // update ware house type count
        DoctorFarmWareHouseType doctorFarmWareHouseType = doctorFarmWareHouseTypeDao.findByFarmIdAndType(doctorMaterialConsumeProviderDto.getFarmId(), doctorMaterialConsumeProviderDto.getType());
        checkState(!isNull(doctorFarmWareHouseType), "doctorFarm.wareHouseType.emoty");
        doctorFarmWareHouseType.setLogNumber(doctorFarmWareHouseType.getLogNumber()- doctorMaterialConsumeProviderDto.getConsumeCount());

        doctorFarmWareHouseTypeDao.update(doctorFarmWareHouseType);
    }

    /**
     * 统计对应的原料信息消耗列表信息
     * @param dto
     */
    private void updateConsumeCount(DoctorMaterialConsumeProviderDto dto){
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
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    private DoctorMaterialConsumeProvider builderMaterialEvent(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto, Integer eventType){
        DoctorMaterialConsumeProvider result = DoctorMaterialConsumeProvider.builder()
                .type(doctorMaterialConsumeProviderDto.getType())
                .farmId(doctorMaterialConsumeProviderDto.getFarmId()).farmName(doctorMaterialConsumeProviderDto.getFarmName())
                .wareHouseId(doctorMaterialConsumeProviderDto.getWareHouseId()).wareHouseName(doctorMaterialConsumeProviderDto.getWareHouseName())
                .materialId(doctorMaterialConsumeProviderDto.getMaterialTypeId()).materialName(doctorMaterialConsumeProviderDto.getMaterialName())
                .eventType(eventType).eventTime(DateTime.now().toDate()).eventCount(doctorMaterialConsumeProviderDto.getConsumeCount())
                .staffId(doctorMaterialConsumeProviderDto.getStaffId()).staffName(doctorMaterialConsumeProviderDto.getStaffName())
                .creatorId(doctorMaterialConsumeProviderDto.getStaffId()).creatorName(doctorMaterialConsumeProviderDto.getStaffName())
                .createdAt(DateTime.now().toDate())
                .build();

        // 消耗事件计算方式 统计消耗天数
        if(Objects.equals(eventType, DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue())){
            result.setExtraMap(ImmutableMap.of("consumeDays", doctorMaterialConsumeProviderDto.getConsumeDays(),
                    "barnId", doctorMaterialConsumeProviderDto.getBarnId(),
                    "barnName", doctorMaterialConsumeProviderDto.getBarnName()));
        }
        return result;
    }
}
