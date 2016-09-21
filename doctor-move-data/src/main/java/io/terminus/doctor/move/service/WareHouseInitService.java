package io.terminus.doctor.move.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.B_WareHouse;
import io.terminus.doctor.move.model.MaterialPurchasedUsed;
import io.terminus.doctor.move.model.View_AssetList;
import io.terminus.doctor.move.model.View_FeedList;
import io.terminus.doctor.move.model.View_MedicineList;
import io.terminus.doctor.move.model.View_RawMaterialList;
import io.terminus.doctor.move.model.View_VaccinationList;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialPriceInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.manager.MaterialInWareHouseManager;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialPriceInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by chenzenghui on 16/8/3.
 */

@Slf4j
@Service
public class WareHouseInitService {
    @Autowired
    private DoctorUserReadService doctorUserReadService;
    @Autowired
    private DoctorStaffDao doctorStaffDao;
    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;
    @Autowired
    private DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    @Autowired
    private SubDao subDao;
    @Autowired
    private DoctorBasicMaterialDao doctorBasicMaterialDao;
    @Autowired
    private DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;
    @Autowired
    private DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;
    @Autowired
    private DoctorMaterialConsumeAvgDao doctorMaterialConsumeAvgDao;
    @Autowired
    private DoctorWareHouseTrackDao doctorWareHouseTrackDao;
    @Autowired
    private DoctorBarnDao doctorBarnDao;
    @Autowired
    private DoctorFarmWareHouseTypeDao doctorFarmWareHouseTypeDao;
    @Autowired
    private DoctorMoveBasicService doctorMoveBasicService;
    @Autowired
    private UserProfileDao userProfileDao;
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private MaterialInWareHouseManager materialInWareHouseManager;
    @Autowired
    private DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao;

    @Transactional
    public void init(String mobile, Long dataSourceId, DoctorFarm farm){
        User user = RespHelper.or500(doctorUserReadService.findBy(mobile, LoginType.MOBILE));
        Long userId = user.getId();

        UserProfile userProfile = userProfileDao.findByUserId(userId);

        //子账号
        List<Sub> subs = subDao.findByConditions(ImmutableMap.of("parentUserId", userId), null);
        //子账号map, key = realName, value = Sub
        Map<String, Sub> subMap = subs.stream().collect(Collectors.toMap(Sub::getRealName, v -> v));

        // key = realName, value = userId
        Map<String, Long> staffMap = doctorMoveBasicService.getSubMap(farm.getOrgId());

        List<B_WareHouse> list = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, B_WareHouse.class, DoctorMoveTableEnum.B_WareHouse));
        if(list != null && !list.isEmpty()){
            List<String> stopUseMaterial = this.getStopUseMaterial(dataSourceId);
            // 用户有仓库信息,则添加基础物料
            Map<String, DoctorBasicMaterial> basicMaterialMap = this.insertBasicMaterial(dataSourceId, stopUseMaterial);// key = 类型数值 | basicMaterialName, value = basicMaterial

            String managerName = list.get(0).getManager().split(",")[0];
            // 查找该猪场下的所有猪舍, key = outId, value = barn
            Map<String, DoctorBarn> barnMap = this.findBarnMap(dataSourceId, farm);

            // 初始化仓库, 每种类型的仓库各一个
            Map<WareHouseType, DoctorWareHouse> warehouseMap = new HashMap<>(); // key = WareHouseType, value = DoctorWareHouse
            for(WareHouseType type : WareHouseType.values()){
                DoctorWareHouse wareHouse = new DoctorWareHouse();
                wareHouse.setFarmId(farm.getId());
                wareHouse.setFarmName(farm.getName());
                wareHouse.setManagerId(subMap.get(managerName).getUserId());
                wareHouse.setManagerName(managerName);
                wareHouse.setType(type.getKey());
                wareHouse.setWareHouseName(type.getDesc() + "仓库");
                doctorWareHouseDao.create(wareHouse);
                warehouseMap.put(type, wareHouse);
            }
            //往仓库里添加物料
            this.addMaterial2Warehouse(dataSourceId, warehouseMap, basicMaterialMap, staffMap, barnMap, userProfile, stopUseMaterial);

            //TODO 配方
        }
    }

    /**
     *
     * @return key = outId, value = barn
     */
    private Map<String, DoctorBarn> findBarnMap(Long dataSourceId, DoctorFarm farm){
        List<DoctorBarn> barns = doctorBarnDao.findByFarmId(farm.getId());
        if(barns.isEmpty()){
            //throw new ServiceException("please init barn data first");
            doctorMoveBasicService.moveBarn(dataSourceId, farm);
            barns = doctorBarnDao.findByFarmId(farm.getId());
        }
        return barns.stream().collect(Collectors.toMap(DoctorBarn::getOutId, v -> v));
    }

    /**
     * 查询已停用的物料
     */
    public List<String> getStopUseMaterial(Long dataSourceId){
        List<String> result = new ArrayList<>();
        List<Map<String, Object>> list = doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, "StopUseMaterial");
        result.addAll(list.stream().map(map -> map.get("Material").toString()).collect(Collectors.toList()));
        return result;
    }

    //往基础物料表加数据
    private Map<String, DoctorBasicMaterial> insertBasicMaterial(Long dataSourceId, List<String> stopUseMaterial){
        // key = 类型数值 | basicMaterialName, value = basicMaterial
        Map<String, DoctorBasicMaterial> basicMaterialMap = new HashMap<>();
        //药品
        List<View_MedicineList> medicines = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_MedicineList.class, DoctorMoveTableEnum.View_MedicineList));
        for (View_MedicineList medicine : medicines){
            if(stopUseMaterial.contains(medicine.getOID())){
                continue;
            }
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.MEDICINE, medicine.getMaterialName());
            //不存在则新增
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.MEDICINE.getKey());
                basicMaterial.setName(medicine.getMaterialName());
                basicMaterial.setSrm(medicine.getSrm());
                basicMaterial.setUnitGroupName(medicine.getUnitGroupText());
                basicMaterial.setUnitName(medicine.getUnitName());
                basicMaterial.setRemark(medicine.getRemark());
                basicMaterial.setIsValid(1);
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        //疫苗
        List<View_VaccinationList> vaccinationLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_VaccinationList.class, DoctorMoveTableEnum.View_VaccinationList));
        for(View_VaccinationList vaccination : vaccinationLists){
            if(stopUseMaterial.contains(vaccination.getOID())){
                continue;
            }
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.VACCINATION, vaccination.getMaterialName());
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.VACCINATION.getKey());
                basicMaterial.setName(vaccination.getMaterialName());
                basicMaterial.setSrm(vaccination.getSrm());
                basicMaterial.setUnitGroupName(vaccination.getUnitGroupText());
                basicMaterial.setUnitName(vaccination.getUnitName());
                basicMaterial.setRemark(vaccination.getRemark());
                basicMaterial.setIsValid(1);
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        //原料
        List<View_RawMaterialList> rawMaterialLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_RawMaterialList.class, DoctorMoveTableEnum.View_RawMaterialList));
        for(View_RawMaterialList material : rawMaterialLists){
            if(stopUseMaterial.contains(material.getOID())){
                continue;
            }
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.MATERIAL, material.getMaterialName());
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.MATERIAL.getKey());
                basicMaterial.setName(material.getMaterialName());
                basicMaterial.setSrm(material.getSrm());
                basicMaterial.setUnitGroupName(material.getUnitGroupText());
                basicMaterial.setUnitName(material.getUnitName());
                basicMaterial.setRemark(material.getRemark());
                basicMaterial.setIsValid(1);
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        //饲料
        List<View_FeedList> feedLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_FeedList.class, DoctorMoveTableEnum.View_FeedList));
        for(View_FeedList feed : feedLists) {
            if(stopUseMaterial.contains(feed.getOID())){
                continue;
            }
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.FEED, feed.getMaterialName());
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.FEED.getKey());
                basicMaterial.setName(feed.getMaterialName());
                basicMaterial.setSrm(feed.getSrm());
                basicMaterial.setUnitGroupName(feed.getUnitGroupText());
                basicMaterial.setUnitName(feed.getUnitName());
                basicMaterial.setRemark(feed.getRemark());
                basicMaterial.setIsValid(1);
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        // 消耗品
        List<View_AssetList> assetLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_AssetList.class, DoctorMoveTableEnum.View_AssetList));
        for(View_AssetList medicine: assetLists){
            if(stopUseMaterial.contains(medicine.getOID())){
                continue;
            }
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.CONSUME, medicine.getMaterialName());
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.CONSUME.getKey());
                basicMaterial.setName(medicine.getMaterialName());
                basicMaterial.setSrm(medicine.getSrm());
                basicMaterial.setUnitGroupName(medicine.getUnitGroupText());
                basicMaterial.setUnitName(medicine.getUnitName());
                basicMaterial.setRemark(medicine.getRemark());
                basicMaterial.setIsValid(1);
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        return basicMaterialMap;
    }

    //往仓库里添加物料
    private void addMaterial2Warehouse(Long dataSourceId, Map<WareHouseType, DoctorWareHouse> warehouseType,
                                       Map<String, DoctorBasicMaterial> basicMaterialMap, Map<String, Long> staffMap,
                                       Map<String, DoctorBarn> barnMap, UserProfile userProfile, List<String> stopUseMaterial){
        // 易耗品
        List<MaterialPurchasedUsed> consumes = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "AssetPurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.CONSUME), consumes, basicMaterialMap, staffMap, barnMap, userProfile, stopUseMaterial);

        // 饲料
        List<MaterialPurchasedUsed> feeds = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "FeedPurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.FEED), feeds, basicMaterialMap, staffMap, barnMap, userProfile, stopUseMaterial);

        // 原料
        List<MaterialPurchasedUsed> raws = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "RawMaterialPurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.MATERIAL), raws, basicMaterialMap, staffMap, barnMap, userProfile, stopUseMaterial);

        // 药品
        List<MaterialPurchasedUsed> med = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "MedicinePurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.MEDICINE), med, basicMaterialMap, staffMap, barnMap, userProfile, stopUseMaterial);

        // 疫苗
        List<MaterialPurchasedUsed> vaccinationPurchasedUsed = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "VaccinationPurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.VACCINATION), vaccinationPurchasedUsed, basicMaterialMap, staffMap, barnMap, userProfile, stopUseMaterial);
    }

    private void addMaterial2Warehouse(DoctorWareHouse wareHouse, List<MaterialPurchasedUsed> list,
                                       Map<String, DoctorBasicMaterial> basicMaterialMap, Map<String, Long> staffMap,
                                       Map<String, DoctorBarn> barnMap, UserProfile userProfile, List<String> stopUseMaterial){
        // 往表 doctor_material_consume_avgs 写数的Map, key = 类型数值 | materialName, value = [eventCount(最后一次领用数量), 时间]
        Map<String, Object[]> lastMaterialConsumeMap = new HashMap<>();

        // 该仓库最近一次领用发生的时间和数量
        Date lastHouseConsumeDate = null;

        //此仓库中各物资的数量, key = typeAndmaterialName, value = 数量
        Map<String, Double> materialCount = new HashMap<>();

        //猪群Map, key = outId, value = group
        Map<String, DoctorGroup> groupMap = new HashMap<>();
        for(DoctorGroup group : doctorGroupDao.findByFarmId(wareHouse.getFarmId())){
            groupMap.put(group.getOutId(), group);
        }

        // 领用和添加物料的历史记录
        DoctorMaterialConsumeProvider materialCP = new DoctorMaterialConsumeProvider();
        materialCP.setType(wareHouse.getType());
        materialCP.setFarmId(wareHouse.getFarmId());
        materialCP.setFarmName(wareHouse.getFarmName());
        materialCP.setWareHouseId(wareHouse.getId());
        materialCP.setWareHouseName(wareHouse.getWareHouseName());
        for(MaterialPurchasedUsed pu : list){
            if(stopUseMaterial.contains(pu.getMaterialOID())){
                continue;
            }
            String typeAndmaterialName = wareHouse.getType() + "|" + pu.getMaterialName();
            if(!materialCount.containsKey(typeAndmaterialName)){
                materialCount.put(typeAndmaterialName, 0D);
            }
            materialCP.setMaterialId(basicMaterialMap.get(typeAndmaterialName).getId());
            materialCP.setMaterialName(pu.getMaterialName());
            materialCP.setEventTime(pu.getEventDate());
            materialCP.setUnitPrice(Double.valueOf(pu.getUnitPrice() * 100).longValue());
            if(isProvide(pu.getEventType(), WareHouseType.from(wareHouse.getType()))){
                materialCount.put(typeAndmaterialName, materialCount.get(typeAndmaterialName) + pu.getCount());
            }else{
                materialCount.put(typeAndmaterialName, materialCount.get(typeAndmaterialName) - pu.getCount());
            }
            DoctorMaterialConsumeProvider.EVENT_TYPE eventType = this.getEventType(pu, wareHouse.getType());
            if(eventType == null){
                continue;
            }
            materialCP.setEventType(eventType.getValue());

            materialCP.setEventCount(pu.getCount());
            if(pu.getStaff() == null || pu.getStaff().trim().isEmpty()){
                materialCP.setStaffName(pu.getZdr());
            }else{
                materialCP.setStaffName(pu.getStaff());
            }
            if("系统管理员".equals(materialCP.getStaffName()) || staffMap.get(materialCP.getStaffName()) == null){
                materialCP.setStaffName(userProfile.getRealName());
                materialCP.setStaffId(userProfile.getUserId());
            }else{
                materialCP.setStaffId(staffMap.get(materialCP.getStaffName()));
            }

            //如果是领用, 需要设置 extra
            if(isConsume(pu.getEventType(), WareHouseType.from(wareHouse.getType()))){
                DoctorBarn barn = barnMap.get(pu.getBarnOId());
                if(barn != null){
                    Map<String, Object> extraMap = new HashMap<>();
                    extraMap.put("barnId", barn.getId());
                    extraMap.put("barnName", barn.getName());
                    materialCP.setBarnId(barn.getId());
                    materialCP.setBarnName(barn.getName());
                    materialCP.setExtraMap(extraMap);

                    if(pu.getGroupOutId() != null){
                        DoctorGroup group = groupMap.get(pu.getGroupOutId());
                        if(group != null){
                            materialCP.setGroupId(group.getId());
                            materialCP.setGroupCode(group.getGroupCode());
                        }
                    }
                }
            }
            doctorMaterialConsumeProviderDao.create(materialCP);
            // 入库, 则保存价格组成
            if(isProvide(pu.getEventType(), WareHouseType.from(wareHouse.getType()))){
                this.saveMaterialPriceInWarehouse(materialCP);
            }

            if(isConsume(pu.getEventType(), WareHouseType.from(wareHouse.getType()))) {
                lastMaterialConsumeMap.put(typeAndmaterialName, new Object[]{materialCP.getEventCount(), pu.getEventDate()});

                if(lastHouseConsumeDate == null || !pu.getEventDate().before(lastHouseConsumeDate)){
                    lastHouseConsumeDate = pu.getEventDate();
                }
            }
        }

        // 仓库中各种物料的最新数量
        DoctorMaterialInWareHouse materialInWareHouse = new DoctorMaterialInWareHouse();
        materialInWareHouse.setFarmId(wareHouse.getFarmId());
        materialInWareHouse.setFarmName(wareHouse.getFarmName());
        materialInWareHouse.setWareHouseId(wareHouse.getId());
        materialInWareHouse.setWareHouseName(wareHouse.getWareHouseName());
        materialInWareHouse.setType(wareHouse.getType());
        for(DoctorBasicMaterial basicMaterial : basicMaterialMap.values()){
            if(basicMaterial.getType().equals(wareHouse.getType())){
                materialInWareHouse.setMaterialId(basicMaterial.getId());
                materialInWareHouse.setMaterialName(basicMaterial.getName());
                materialInWareHouse.setLotNumber(materialCount.get(wareHouse.getType() + "|" + basicMaterial.getName()));
                if(materialInWareHouse.getLotNumber() == null){
                    continue;
                }
                materialInWareHouse.setUnitGroupName(basicMaterial.getUnitGroupName());
                materialInWareHouse.setUnitName(basicMaterial.getUnitName());
                doctorMaterialInWareHouseDao.create(materialInWareHouse);
            }
        }

        //该仓库最近的一条avg数据
        DoctorMaterialConsumeAvg recentAVG = new DoctorMaterialConsumeAvg();
        // 仓库中各种物料的最后一次领用数量
        DoctorMaterialConsumeAvg avg = new DoctorMaterialConsumeAvg();
        avg.setFarmId(wareHouse.getFarmId());
        avg.setWareHouseId(wareHouse.getId());
        avg.setType(wareHouse.getType());
        for(Map.Entry<String, Object[]> entry : lastMaterialConsumeMap.entrySet()){
            avg.setMaterialId(basicMaterialMap.get(entry.getKey()).getId());
            avg.setConsumeCount((Double) entry.getValue()[0]);
            avg.setConsumeDate((Date) entry.getValue()[1]);
            doctorMaterialConsumeAvgDao.create(avg);
            if(recentAVG.getConsumeDate() == null || !avg.getConsumeDate().before(recentAVG.getConsumeDate())){
                recentAVG.setConsumeDate(avg.getConsumeDate());
                recentAVG.setConsumeCount(avg.getConsumeCount());
            }
        }

        //仓库所有物料的总数量
        double total = 0L;
        Map<String, Object> trackMap = new HashMap<>(); // key = materialId, value = 相应物料在该仓库的数量
        for(DoctorMaterialInWareHouse item : doctorMaterialInWareHouseDao.queryByFarmAndWareHouseId(wareHouse.getFarmId(), wareHouse.getId())){
            if(item.getLotNumber() != null){
                total = total + item.getLotNumber();
            }
            trackMap.put(item.getMaterialId().toString(), item.getLotNumber());
        }
        //最近一次领用时间
        if(lastHouseConsumeDate != null){
            trackMap.put("recentConsumeDate", lastHouseConsumeDate);
        }
        DoctorWareHouseTrack track = new DoctorWareHouseTrack();
        track.setWareHouseId(wareHouse.getId());
        track.setFarmId(wareHouse.getFarmId());
        track.setFarmName(wareHouse.getFarmName());
        track.setManagerId(wareHouse.getManagerId());
        track.setManagerName(wareHouse.getManagerName());
        track.setLotNumber(total);
        track.setExtraMap(trackMap);
        doctorWareHouseTrackDao.create(track);

        // 相应猪场此类型的仓库总物资数量
        DoctorFarmWareHouseType farmWareHouseType = new DoctorFarmWareHouseType();
        farmWareHouseType.setFarmId(wareHouse.getFarmId());
        farmWareHouseType.setFarmName(wareHouse.getFarmName());
        farmWareHouseType.setType(wareHouse.getType());
        farmWareHouseType.setLotNumber(track.getLotNumber());
        Map<String, Object> extramap = new HashMap<>();
        extramap.put("consumeCount", recentAVG.getConsumeCount());
        extramap.put("consumeDate", recentAVG.getConsumeDate());
        farmWareHouseType.setExtraMap(extramap);
        doctorFarmWareHouseTypeDao.create(farmWareHouseType);

        // 更新最新的价格组成
        this.updatePriceInWarehouse(wareHouse.getFarmId(), wareHouse.getId());
    }

    private static final List<String> event_type_provide = Lists.newArrayList("采购", "调入", "盘盈");

    /**
     * 增加
     */
    private boolean isProvide(String eventType, WareHouseType wareHouseType) {
        return event_type_provide.contains(eventType) || (Objects.equals(wareHouseType, WareHouseType.FEED) && "生产".equals(eventType));
    }

    private static final List<String> event_type_consume = Lists.newArrayList("领用", "调出", "盘亏");
    /**
     * 减少
     */
    private boolean isConsume(String eventType, WareHouseType wareHouseType) {
        return event_type_consume.contains(eventType) || (Objects.equals(wareHouseType, WareHouseType.MATERIAL) && "生产".equals(eventType));
    }

    /**
     * 由于逻辑中要求库存不可为负数, 所以调用现成的 manager 走不通
     */
    private void addMaterial2Warehouse2(DoctorWareHouse wareHouse, List<MaterialPurchasedUsed> list,
                                       Map<String, DoctorBasicMaterial> basicMaterialMap, Map<String, Long> staffMap,
                                       Map<String, DoctorBarn> barnMap, UserProfile userProfile, List<String> stopUseMaterial) {
        //猪群Map, key = outId, value = group
        Map<String, DoctorGroup> groupMap = doctorGroupDao.findByFarmId(wareHouse.getFarmId()).stream().collect(Collectors.toMap(DoctorGroup::getOutId, v -> v));
        Integer wareHouseType = wareHouse.getType();
        for(MaterialPurchasedUsed pu : list){
            if(stopUseMaterial.contains(pu.getMaterialOID())){
                continue;
            }
            DoctorMaterialConsumeProvider.EVENT_TYPE eventType = this.getEventType(pu, wareHouseType);
            String typeAndmaterialName = wareHouse.getType() + "|" + pu.getMaterialName();
            DoctorBasicMaterial material = basicMaterialMap.get(typeAndmaterialName);
            // 由于 pu.getBarnOId() 可能为null, 所以 barn 也可能为null
            DoctorBarn barn = barnMap.get(pu.getBarnOId());

            if(eventType == null){
                continue;
            }

            DoctorMaterialConsumeProviderDto dto = DoctorMaterialConsumeProviderDto.builder()
                    .actionType(eventType.getValue()).type(wareHouseType)
                    .farmId(wareHouse.getFarmId()).farmName(wareHouse.getFarmName())
                    .wareHouseId(wareHouse.getId()).wareHouseName(wareHouse.getWareHouseName())
                    .materialTypeId(material.getId()).materialName(material.getName())
                    .barnId(barn == null ? null : barn.getId()).barnName(barn == null ? null : barn.getName())
                    .count(pu.getCount())
                    .build();

            if(pu.getStaff() == null || pu.getStaff().trim().isEmpty()){
                dto.setStaffName(pu.getZdr());
            }else{
                dto.setStaffName(pu.getStaff());
            }
            if("系统管理员".equals(dto.getStaffName()) || staffMap.get(dto.getStaffName()) == null){
                dto.setStaffName(userProfile.getRealName());
                dto.setStaffId(userProfile.getUserId());
            }else{
                dto.setStaffId(staffMap.get(dto.getStaffName()));
            }

            if(eventType.isOut()){
                if(pu.getGroupOutId() != null){
                    DoctorGroup group = groupMap.get(pu.getGroupOutId());
                    if(group != null){
                        dto.setGroupId(group.getId());
                        dto.setGroupCode(group.getGroupCode());
                    }
                }
                dto.setConsumeDays(pu.getUsedDays());
                dto.setUnitId(material.getUnitId());
                dto.setUnitName(material.getUnitName());
                dto.setUnitGroupId(material.getUnitGroupId());
                dto.setUnitGroupName(material.getUnitGroupName());
                materialInWareHouseManager.consumeMaterial(dto);
            }

            if(eventType.isIn()){
                dto.setUnitPrice(Double.valueOf(pu.getUnitPrice() * 100).longValue());
                materialInWareHouseManager.providerMaterialInWareHouse(dto);
            }
        }
    }

    private DoctorMaterialConsumeProvider.EVENT_TYPE getEventType(MaterialPurchasedUsed pu, Integer wareHouseType){
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType;
        switch (pu.getEventType()) {
            case "采购":
                eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER;
                break;
            case "领用":
                eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER;
                break;
            case "调入":
                eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.DIAORU;
                break;
            case "调出":
                eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.DIAOCHU;
                break;
            case "盘盈":
                eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.PANYING;
                break;
            case "盘亏":
                eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.PANKUI;
                break;
            case "生产":
                if (Objects.equals(wareHouseType, WareHouseType.FEED.getKey())) {
                    eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER;
                } else if (Objects.equals(wareHouseType, WareHouseType.MATERIAL.getKey())) {
                    eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER;
                } else {
                    return null;
                }
                break;
            default:
                return null;
        }
        return eventType;
    }

    private void saveMaterialPriceInWarehouse(DoctorMaterialConsumeProvider cp){
        DoctorMaterialPriceInWareHouse model = new DoctorMaterialPriceInWareHouse();
        model.setFarmId(cp.getFarmId());
        model.setFarmName(cp.getFarmName());
        model.setWareHouseId(cp.getWareHouseId());
        model.setWareHouseName(cp.getWareHouseName());
        model.setMaterialId(cp.getMaterialId());
        model.setMaterialName(cp.getMaterialName());
        model.setType(cp.getType());
        model.setProviderId(cp.getId());
        model.setUnitPrice(cp.getUnitPrice());
        model.setRemainder(cp.getEventCount());
        model.setProviderTime(cp.getEventTime());
        model.setCreatorId(cp.getCreatorId());
        model.setUpdatorId(cp.getUpdatorId());
        doctorMaterialPriceInWareHouseDao.create(model);
    }

    private void updatePriceInWarehouse(Long farmId, Long warehouseId){
        for(Map.Entry<Long, Double> entry : doctorMaterialConsumeProviderDao.countConsumeTotal(warehouseId).entrySet()){
            Long materialId = entry.getKey();
            // 累计出库数量
            Double consumeCount = entry.getValue();
            // 累加值
            double plus = 0D;
            for(DoctorMaterialPriceInWareHouse item : doctorMaterialPriceInWareHouseDao.findByWareHouseAndMaterialId(warehouseId, materialId)){
                Double remainder = item.getRemainder();
                if(plus + remainder <= consumeCount){
                    doctorMaterialPriceInWareHouseDao.delete(item.getId());
                    if(plus + remainder == consumeCount){
                        break;
                    }
                    plus += remainder;
                }else{
                    item.setRemainder(remainder - (consumeCount - plus));
                    doctorMaterialPriceInWareHouseDao.update(item);
                    break;
                }
            }
        }
    }
}
