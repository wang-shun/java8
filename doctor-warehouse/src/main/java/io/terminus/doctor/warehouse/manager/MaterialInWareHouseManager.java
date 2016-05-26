package io.terminus.doctor.warehouse.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.constants.DoctorFarmWareHouseTypeConstants;
import io.terminus.doctor.warehouse.constants.DoctorWareHouseTrackConstants;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
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

    private final DoctorWareHouseDao doctorWareHouseDao;

    private final DoctorWareHouseTrackDao doctorWareHouseTrackDao;

    private final DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;

    private final DoctorMaterialInfoDao doctorMaterialInfoDao;

    @Autowired
    public MaterialInWareHouseManager(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao,
                                      DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao,
                                      DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao,
                                      DoctorWareHouseTrackDao doctorWareHouseTrackDao,
                                      DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao,
                                      DoctorMaterialInfoDao doctorMaterialInfoDao,
                                      DoctorWareHouseDao doctorWareHouseDao){
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
        this.doctorMaterialConsumeAvgDao = doctorMaterialConsumeAvgDao;
        this.doctorWareHouseDao = doctorWareHouseDao;
        this.doctorWareHouseTrackDao = doctorWareHouseTrackDao;
        this.doctorFarmWareHouseTypeDao = doctorFarmWareHouseTypeDao;
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
    }

    // 生产对应的物料内容
    @Transactional
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
                .providerCount(materialProduce.getTotal()).unitId(targetMaterial.getUnitId()).unitName(targetMaterial.getUnitName())
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
        long totalConsumeCount= materialProduceEntry.getMaterialCount();
        int index = 0;

        while (totalConsumeCount!=0){
            DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouses.get(index);
            index+=1;
            long toConsume = doctorMaterialInWareHouse.getLotNumber();

            if(toConsume>=totalConsumeCount){
                toConsume = totalConsumeCount;
                totalConsumeCount = 0;
            }else {
                totalConsumeCount = totalConsumeCount - toConsume;
            }

            consumeMaterialInner(DoctorMaterialConsumeProviderDto.builder()
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
    public Long consumeMaterial(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){

        return consumeMaterialInner(doctorMaterialConsumeProviderDto);
    }

    /**
     * 用户信息的提供操作
     * @param doctorMaterialConsumeProviderDto
     * @return
     */
    @Transactional
    public Long providerMaterialInWareHouse(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        // validate ware house type
        // 仓库 仅仅存储一种Type的 物料信息
        DoctorWareHouse doctorWareHouse = doctorWareHouseDao.findById(doctorMaterialConsumeProviderDto.getWareHouseId());
        // validate type
        Preconditions.checkState(Objects.equals(doctorWareHouse.getType(), doctorMaterialConsumeProviderDto.getType()),
                "wareHouse.provider.illegalType");

        return providerMaterialInWareHouseInner(doctorMaterialConsumeProviderDto);
    }

    private Long providerMaterialInWareHouseInner(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        // 录入事件信息
        DoctorMaterialConsumeProvider providerEvent = builderMaterialEvent(doctorMaterialConsumeProviderDto, DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue());
        doctorMaterialConsumeProviderDao.create(providerEvent);

        // 修改数量信息
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(doctorMaterialConsumeProviderDto.getFarmId(),
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
            Map<String,Object> trackExtraMap = doctorWareHouseTrack.getExtraMap();
            if(trackExtraMap.containsKey(key)){
                trackExtraMap.put(key, doctorMaterialConsumeProviderDto.getProviderCount() + Params.getWithConvert(trackExtraMap, key, a->Long.valueOf(a.toString())));
            }else {
                trackExtraMap.put(key, doctorMaterialConsumeProviderDto.getProviderCount());
            }
            doctorWareHouseTrack.setExtraMap(trackExtraMap);
            doctorWareHouseTrackDao.update(doctorWareHouseTrack);
        }

        // 修改猪场仓库类型的数量信息
        DoctorFarmWareHouseType doctorFarmWareHouseType = doctorFarmWareHouseTypeDao.findByFarmIdAndType(
                doctorMaterialConsumeProviderDto.getFarmId(), doctorMaterialConsumeProviderDto.getType());
        if(isNull(doctorFarmWareHouseType)){
            doctorFarmWareHouseTypeDao.create(doctorFarmWareHouseType);
        }else {
            doctorFarmWareHouseType.setLogNumber(doctorFarmWareHouseType.getLogNumber() + doctorMaterialConsumeProviderDto.getProviderCount());
            doctorFarmWareHouseType.setUpdatorId(doctorMaterialConsumeProviderDto.getStaffId());
            doctorFarmWareHouseType.setUpdatorName(doctorMaterialConsumeProviderDto.getStaffName());
            doctorFarmWareHouseTypeDao.update(doctorFarmWareHouseType);
        }

        return providerEvent.getId();
    }

    private Long consumeMaterialInner(DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto){
        // 校验库存数量信息
        consumeMaterialInWareHouse(doctorMaterialConsumeProviderDto);

        //录入事件信息
        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider =
                builderMaterialEvent(doctorMaterialConsumeProviderDto, DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
        doctorMaterialConsumeProviderDao.create(doctorMaterialConsumeProvider);

        //计算平均消耗信息
        updateConsumeCount(doctorMaterialConsumeProviderDto);

        //更新猪场整体的数量信息
        updateLotNumberInWareHouseType(doctorMaterialConsumeProviderDto);

        return doctorMaterialConsumeProvider.getId();
    }

    /**
     * 修改MaterialInWareHouse 数量信息
     * @param dto
     */
    private void consumeMaterialInWareHouse(DoctorMaterialConsumeProviderDto dto){
        // 校验库存数量信息
        DoctorMaterialInWareHouse doctorMaterialInWareHouse = doctorMaterialInWareHouseDao.queryByFarmHouseMaterial(
                dto.getFarmId(), dto.getWareHouseId(), dto.getMaterialTypeId());
        checkState(!isNull(doctorMaterialInWareHouse), "query.doctorMaterialConsume.fail");
        checkState(dto.getConsumeCount()<=doctorMaterialInWareHouse.getLotNumber(), "consume.not.enough");
        doctorMaterialInWareHouse.setLotNumber(doctorMaterialInWareHouse.getLotNumber() - dto.getConsumeCount());
        doctorMaterialInWareHouseDao.update(doctorMaterialInWareHouse);
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
                .managerId(doctorMaterialConsumeProviderDto.getStaffId()).managerName(doctorMaterialConsumeProviderDto.getStaffName())
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

        // track中 存放 不同material 数量信息
        Map<String, Object> consumeMap = doctorWareHouseTrack.getExtraMap();
        Long count = Long.valueOf(consumeMap.get(doctorMaterialConsumeProviderDto.getMaterialTypeId().toString()).toString());
        consumeMap.put(doctorMaterialConsumeProviderDto.getMaterialTypeId().toString(), count - doctorMaterialConsumeProviderDto.getConsumeCount());
        consumeMap.put(DoctorWareHouseTrackConstants.RECENT_CONSUME_DATE, DateTime.now().toDate());
        doctorWareHouseTrack.setExtraMap(consumeMap);
        doctorWareHouseTrackDao.update(doctorWareHouseTrack);

        // update ware house type count
        DoctorFarmWareHouseType doctorFarmWareHouseType = doctorFarmWareHouseTypeDao.findByFarmIdAndType(doctorMaterialConsumeProviderDto.getFarmId(), doctorMaterialConsumeProviderDto.getType());
        checkState(!isNull(doctorFarmWareHouseType), "doctorFarm.wareHouseType.emoty");
        doctorFarmWareHouseType.setLogNumber(doctorFarmWareHouseType.getLogNumber()- doctorMaterialConsumeProviderDto.getConsumeCount());
        Map<String,Object> extraMap = isNull(doctorFarmWareHouseType.getExtraMap())? Maps.newHashMap() :doctorFarmWareHouseType.getExtraMap();
        if(extraMap.containsKey(DoctorFarmWareHouseTypeConstants.CONSUME_DATE) &&
                DateTime.now().withTimeAtStartOfDay().isEqual(Long.valueOf(extraMap.get(DoctorFarmWareHouseTypeConstants.CONSUME_DATE).toString()))){
            extraMap.put(DoctorFarmWareHouseTypeConstants.CONSUME_COUNT,
                    Long.valueOf(extraMap.get(DoctorFarmWareHouseTypeConstants.CONSUME_COUNT).toString()) + doctorMaterialConsumeProviderDto.getConsumeCount());
        }else {
            extraMap.put(DoctorFarmWareHouseTypeConstants.CONSUME_DATE, DateTime.now().withTimeAtStartOfDay().getMillis());
            extraMap.put(DoctorFarmWareHouseTypeConstants.CONSUME_COUNT, doctorMaterialConsumeProviderDto.getConsumeCount());
        }
        doctorFarmWareHouseType.setExtraMap(extraMap);
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
                    .consumeDate(DateTime.now().withTimeAtStartOfDay().toDate()).consumeCount(dto.getConsumeCount())
                    .consumeAvgCount(0l)
                    .build();

            if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
                avg.setConsumeAvgCount(dto.getConsumeCount() / dto.getConsumeDays());
            }
            doctorMaterialConsumeAvgDao.create(avg);
        }else{
            if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
                // calculate current avg rate
                doctorMaterialConsumeAvg.setConsumeAvgCount(dto.getConsumeCount() / dto.getConsumeDays());
                doctorMaterialConsumeAvg.setConsumeCount(dto.getConsumeCount());
            }else {
                Integer dayRange = Days.daysBetween(new DateTime(doctorMaterialConsumeAvg.getConsumeDate()),DateTime.now()).getDays();
                if(dayRange == 0){
                    // 同一天领用 0
                    doctorMaterialConsumeAvg.setConsumeCount(doctorMaterialConsumeAvg.getConsumeCount() + dto.getConsumeCount());
                }else {
                    // calculate avg date content
                    doctorMaterialConsumeAvg.setConsumeAvgCount(
                            doctorMaterialConsumeAvg.getConsumeCount() /dayRange);
                    doctorMaterialConsumeAvg.setConsumeCount(dto.getConsumeCount());
                }
            }
            doctorMaterialConsumeAvg.setConsumeDate(DateTime.now().withTimeAtStartOfDay().toDate());
            doctorMaterialConsumeAvgDao.update(doctorMaterialConsumeAvg);
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
                .eventType(eventType).eventTime(DateTime.now().toDate())
                .staffId(doctorMaterialConsumeProviderDto.getStaffId()).staffName(doctorMaterialConsumeProviderDto.getStaffName())
                .creatorId(doctorMaterialConsumeProviderDto.getStaffId()).creatorName(doctorMaterialConsumeProviderDto.getStaffName())
                .build();

        // consume provider
        if(Objects.equals(eventType, DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue())){
            result.setEventCount(doctorMaterialConsumeProviderDto.getConsumeCount());
        }else {
            result.setEventCount(doctorMaterialConsumeProviderDto.getProviderCount());
        }

        // 消耗事件计算方式 统计消耗天数
        if(Objects.equals(eventType, DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue()) &&
                Objects.equals(doctorMaterialConsumeProviderDto.getType(), WareHouseType.FEED.getKey())){
            result.setExtraMap(ImmutableMap.of(
                    "consumeDays", doctorMaterialConsumeProviderDto.getConsumeDays(),
                    "barnId", doctorMaterialConsumeProviderDto.getBarnId(),
                    "barnName", doctorMaterialConsumeProviderDto.getBarnName()));
        }
        return result;
    }
}
