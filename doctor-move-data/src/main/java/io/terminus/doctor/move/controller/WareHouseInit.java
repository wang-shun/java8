package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.B_WareHouse;
import io.terminus.doctor.move.model.MaterialPurchasedUsed;
import io.terminus.doctor.move.model.View_AssetList;
import io.terminus.doctor.move.model.View_FeedList;
import io.terminus.doctor.move.model.View_MedicineList;
import io.terminus.doctor.move.model.View_RawMaterialList;
import io.terminus.doctor.move.model.View_VaccinationList;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.Sub;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.warehouse.dao.DoctorFarmWareHouseTypeDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeAvgDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorWareHouseTrackDao;
import io.terminus.doctor.warehouse.manager.MaterialInWareHouseManager;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by chenzenghui on 16/7/28.
 */

@Slf4j
@RestController
@RequestMapping("/api/data/warehouse")
public class WareHouseInit {
    @Autowired
    private DoctorUserReadService doctorUserReadService;
    @Autowired
    private DoctorOrgDao doctorOrgDao;
    @Autowired
    private DoctorStaffDao doctorStaffDao;
    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;
    @Autowired
    private DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    @Autowired
    private DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    @Autowired
    private DoctorFarmDao doctorFarmDao;
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


    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public String initWareHouse(@RequestParam String mobile, @RequestParam Long dataSourceId){
        log.warn("start to init warehouse data, mobile={}, dataSourceId = {}", mobile, dataSourceId);
        try{
            this.init(mobile, dataSourceId);
            log.warn("init warehouse succeed, mobile={}, dataSourceId = {}", mobile, dataSourceId);
            return "ok";
        }catch(Exception e){
            log.error("init warehouse data, mobile={}, dataSourceId={}, error:{}", mobile, dataSourceId, Throwables.getStackTraceAsString(e));
            return Throwables.getStackTraceAsString(e);
        }
    }

    private void init(String mobile, Long dataSourceId){
        User user = RespHelper.or500(doctorUserReadService.findBy(mobile, LoginType.MOBILE));
        Long userId = user.getId();
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(userId);
        List<Long> farmIds = permission.getFarmIdsList();
        //主账号staff
        DoctorStaff staff = doctorStaffDao.findByUserId(userId);

        //猪场
        List<DoctorFarm> farms = doctorFarmDao.findByIds(farmIds);
        //猪场Map, key = outId, value = farm
        Map<String, DoctorFarm> farmMap = farms.stream().collect(Collectors.toMap(DoctorFarm::getOutId, v -> v));

        //子账号
        List<Sub> subs = subDao.findByConditions(ImmutableMap.of("parentUserId", userId), null);
        //子账号map, key = realName, value = Sub
        Map<String, Sub> subMap = subs.stream().collect(Collectors.toMap(Sub::getRealName, v -> v));

        // key = realName, value = staff
        Map<String, DoctorStaff> staffMap = new HashMap<>();
        for(Sub sub : subs){
            staffMap.put(sub.getRealName(), doctorStaffDao.findByUserId(sub.getUserId()));
        }

        List<B_WareHouse> list = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, B_WareHouse.class, DoctorMoveTableEnum.B_WareHouse));
        if(list != null && !list.isEmpty()){
            // 用户有仓库信息,则添加基础物料
            Map<String, DoctorBasicMaterial> basicMaterialMap = this.insertBasicMaterial(dataSourceId);// key = 类型数值 | basicMaterialName, value = basicMaterial

            // 初始化仓库, 每个猪场每种类型的仓库各一个
            String managerName = list.get(0).getManager().split(",")[0];
            for(DoctorFarm farm : farmMap.values()){
                // 查找该猪场下的所有猪舍
                // key = outId, value = barn
                Map<String, DoctorBarn> barnMap = this.findBarnMap(farm.getId());

                Map<WareHouseType, DoctorWareHouse> warehouseMap = new HashMap<>(); // key = WareHouseType, value = DoctorWareHouse
                DoctorWareHouse wareHouse = new DoctorWareHouse();
                wareHouse.setFarmId(farm.getId());
                wareHouse.setFarmName(farm.getName());
                wareHouse.setManagerId(subMap.get(managerName).getUserId());
                wareHouse.setManagerName(managerName);
                for(WareHouseType type : WareHouseType.values()){
                    wareHouse.setType(type.getKey());
                    wareHouse.setWareHouseName(type.getDesc() + "仓库");
                    doctorWareHouseDao.create(wareHouse);
                    warehouseMap.put(type, wareHouse);
                }
                //往仓库里添加物料
                this.addMaterial2Warehouse(dataSourceId, warehouseMap, basicMaterialMap, staffMap, barnMap);

                //TODO 配方
            }
        }
    }

    /**
     *
     * @param farmId
     * @return key = outId, value = barn
     */
    private Map<String, DoctorBarn> findBarnMap(Long farmId){
        List<DoctorBarn> barns = doctorBarnDao.findByFarmId(farmId);
        if(barns == null || barns.isEmpty()){
            throw new ServiceException("请先导入猪舍数据");
        }
        return barns.stream().collect(Collectors.toMap(DoctorBarn::getOutId, v -> v));
    }

    //往基础物料表加数据
    private Map<String, DoctorBasicMaterial> insertBasicMaterial(Long dataSourceId){
        // key = 类型数值 | basicMaterialName, value = basicMaterial
        Map<String, DoctorBasicMaterial> basicMaterialMap = new HashMap<>();
        //药品
        List<View_MedicineList> medicines = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_MedicineList.class, DoctorMoveTableEnum.View_MedicineList));
        for (View_MedicineList medicine : medicines){
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
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        //疫苗
        List<View_VaccinationList> vaccinationLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_VaccinationList.class, DoctorMoveTableEnum.View_VaccinationList));
        for(View_VaccinationList vaccination : vaccinationLists){
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.VACCINATION, vaccination.getMaterialName());
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.VACCINATION.getKey());
                basicMaterial.setName(vaccination.getMaterialName());
                basicMaterial.setSrm(vaccination.getSrm());
                basicMaterial.setUnitGroupName(vaccination.getUnitGroupText());
                basicMaterial.setUnitName(vaccination.getUnitName());
                basicMaterial.setRemark(vaccination.getRemark());
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        //原料
        List<View_RawMaterialList> rawMaterialLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_RawMaterialList.class, DoctorMoveTableEnum.View_RawMaterialList));
        for(View_RawMaterialList material : rawMaterialLists){
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.MATERIAL, material.getMaterialName());
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.MATERIAL.getKey());
                basicMaterial.setName(material.getMaterialName());
                basicMaterial.setSrm(material.getSrm());
                basicMaterial.setUnitGroupName(material.getUnitGroupText());
                basicMaterial.setUnitName(material.getUnitName());
                basicMaterial.setRemark(material.getRemark());
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        //饲料
        List<View_FeedList> feedLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_FeedList.class, DoctorMoveTableEnum.View_FeedList));
        for(View_FeedList feed : feedLists) {
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.FEED, feed.getMaterialName());
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.FEED.getKey());
                basicMaterial.setName(feed.getMaterialName());
                basicMaterial.setSrm(feed.getSrm());
                basicMaterial.setUnitGroupName(feed.getUnitGroupText());
                basicMaterial.setUnitName(feed.getUnitName());
                basicMaterial.setRemark(feed.getRemark());
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        // 消耗品
        List<View_AssetList> assetLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_AssetList.class, DoctorMoveTableEnum.View_AssetList));
        for(View_AssetList medicine: assetLists){
            DoctorBasicMaterial basicMaterial = doctorBasicMaterialDao.findByTypeAndName(WareHouseType.CONSUME, medicine.getMaterialName());
            if(basicMaterial == null){
                basicMaterial = new DoctorBasicMaterial();
                basicMaterial.setType(WareHouseType.CONSUME.getKey());
                basicMaterial.setName(medicine.getMaterialName());
                basicMaterial.setSrm(medicine.getSrm());
                basicMaterial.setUnitGroupName(medicine.getUnitGroupText());
                basicMaterial.setUnitName(medicine.getUnitName());
                basicMaterial.setRemark(medicine.getRemark());
                doctorBasicMaterialDao.create(basicMaterial);
            }
            basicMaterialMap.put(basicMaterial.getType() + "|" + basicMaterial.getName(), basicMaterial);
        }

        return basicMaterialMap;
    }

    //往仓库里添加物料
    private void addMaterial2Warehouse(Long dataSourceId, Map<WareHouseType, DoctorWareHouse> warehouseType, Map<String, DoctorBasicMaterial> basicMaterialMap, Map<String, DoctorStaff> staffMap, Map<String, DoctorBarn> barnMap){
        // 易耗品
        List<MaterialPurchasedUsed> consumes = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "AssetPurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.CONSUME), consumes, basicMaterialMap, staffMap, barnMap);

        // 饲料
        List<MaterialPurchasedUsed> feeds = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "FeedPurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.FEED), feeds, basicMaterialMap, staffMap, barnMap);

        // 原料
        List<MaterialPurchasedUsed> raws = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "RawMaterialPurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.MATERIAL), raws, basicMaterialMap, staffMap, barnMap);

        // 药品
        List<MaterialPurchasedUsed> med = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "MedicinePurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.MEDICINE), med, basicMaterialMap, staffMap, barnMap);

        // 疫苗
        List<MaterialPurchasedUsed> vaccinationPurchasedUsed = RespHelper.or500(doctorMoveDatasourceHandler.findByHbsSql(dataSourceId, MaterialPurchasedUsed.class, "VaccinationPurchasedUsed"));
        this.addMaterial2Warehouse(warehouseType.get(WareHouseType.VACCINATION), vaccinationPurchasedUsed, basicMaterialMap, staffMap, barnMap);
    }

    private void addMaterial2Warehouse(DoctorWareHouse wareHouse, List<MaterialPurchasedUsed> list, Map<String, DoctorBasicMaterial> basicMaterialMap, Map<String, DoctorStaff> staffMap, Map<String, DoctorBarn> barnMap){
        // 往表 doctor_material_in_ware_houses 写数的Map, key = 类型数值 | materialName, value = lotNumber(最新数量)
        Map<String, Long> materialInWarehouseMap = new HashMap<>();

        // 往表 doctor_material_consume_avgs 写数的Map, key = 类型数值 | materialName, value = [eventCount(最后一次领用数量), 时间]
        Map<String, Object[]> lastConsumeMap = new HashMap<>();

        // 领用和添加物料的历史记录
        DoctorMaterialConsumeProvider materialCP = new DoctorMaterialConsumeProvider();
        materialCP.setType(wareHouse.getType());
        materialCP.setFarmId(wareHouse.getFarmId());
        materialCP.setFarmName(wareHouse.getFarmName());
        materialCP.setWareHouseId(wareHouse.getId());
        materialCP.setWareHouseName(wareHouse.getWareHouseName());
        for(MaterialPurchasedUsed pu : list){
            String typeAndmaterialName = wareHouse.getType() + "|" + pu.getMaterialName();
            materialCP.setMaterialId(basicMaterialMap.get(typeAndmaterialName).getId());
            materialCP.setMaterialName(pu.getMaterialName());
            materialCP.setEventTime(pu.getEventDate());
            if("采购".equals(pu.getEventType())){
                materialCP.setEventType(DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue());
            }else{
                materialCP.setEventType(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
            }
            materialCP.setEventCount(pu.getCount().longValue());
            if(pu.getStaff() == null || pu.getStaff().trim().isEmpty()){
                materialCP.setStaffName(pu.getZdr());
            }else{
                materialCP.setStaffName(pu.getStaff());
            }
            materialCP.setStaffId(staffMap.get(materialCP.getStaffName()).getId());

            //如果是饲料领用, 需要设置 extra
            if("领用".equals(pu.getEventType()) && Objects.equals(wareHouse.getType(), WareHouseType.FEED.getKey())){
                DoctorBarn barn = barnMap.get(pu.getBarnOId());
                if(barn != null){
                    Map<String, Object> extraMap = new HashMap<>();
                    extraMap.put("barnId", barn.getId());
                    extraMap.put("barnName", barn.getName());
                    materialCP.setExtraMap(extraMap);
                }
            }
            doctorMaterialConsumeProviderDao.create(materialCP);

            // todo materialInWarehouseMap.put(typeAndmaterialName, materialCP.);
            if("领用".equals(pu.getEventType())) {
                lastConsumeMap.put(typeAndmaterialName, new Object[]{materialCP.getEventCount(), pu.getEventDate()});
            }
        }

        // 仓库中各种物料的最新数量 TODO
        DoctorMaterialInWareHouse materialInWareHouse = new DoctorMaterialInWareHouse();
        materialInWareHouse.setFarmId(wareHouse.getFarmId());
        materialInWareHouse.setFarmName(wareHouse.getFarmName());
        materialInWareHouse.setWareHouseId(wareHouse.getId());
        materialInWareHouse.setWareHouseName(wareHouse.getWareHouseName());
        materialInWareHouse.setType(wareHouse.getType());
        for(Map.Entry<String, Long> entry : materialInWarehouseMap.entrySet()){
            DoctorBasicMaterial basicMaterial = basicMaterialMap.get(entry.getKey());
            materialInWareHouse.setMaterialId(basicMaterial.getId());
            materialInWareHouse.setMaterialName(basicMaterial.getName());
            materialInWareHouse.setLotNumber(entry.getValue());
            materialInWareHouse.setUnitGroupName(basicMaterial.getUnitGroupName());
            materialInWareHouse.setUnitName(basicMaterial.getUnitName());
            doctorMaterialInWareHouseDao.create(materialInWareHouse);
        }

        // 仓库中各种物料的最后一次领用数量
        DoctorMaterialConsumeAvg avg = new DoctorMaterialConsumeAvg();
        avg.setFarmId(wareHouse.getFarmId());
        avg.setWareHouseId(wareHouse.getId());
        avg.setType(wareHouse.getType());
        for(Map.Entry<String, Object[]> entry : lastConsumeMap.entrySet()){
            avg.setMaterialId(basicMaterialMap.get(entry.getKey()).getId());
            avg.setConsumeCount((Long) entry.getValue()[0]);
            avg.setConsumeDate((Date) entry.getValue()[1]);
            doctorMaterialConsumeAvgDao.create(avg);
        }

        //仓库所有物料的总数量
        long total = 0L;
        Map<String, Object> trackMap = new HashMap<>(); // key = materialId, value = 相应物料在该仓库的数量
        for(DoctorMaterialInWareHouse item : doctorMaterialInWareHouseDao.queryByFarmAndWareHouseId(wareHouse.getFarmId(), wareHouse.getId())){
            if(item.getLotNumber() != null){
                total = total + item.getLotNumber();
            }
            trackMap.put(item.getMaterialId().toString(), item.getLotNumber());
        }
        //最近一次领用事件
        DoctorMaterialConsumeProvider lastConsumeEvent = doctorMaterialConsumeProviderDao.findLastEvent(wareHouse.getId(), DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER);
        if(lastConsumeEvent != null){
            trackMap.put("recentConsumeDate", lastConsumeEvent.getEventTime());
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
        DoctorMaterialConsumeAvg lastAVG = doctorMaterialConsumeAvgDao.findLastByFarmId(wareHouse.getFarmId());
        if(lastAVG != null){
            Map<String, Object> extramap = new HashMap<>();
            extramap.put("consumeCount", lastAVG.getConsumeCount());
            extramap.put("consumeDate", lastAVG.getConsumeDate());
            farmWareHouseType.setExtraMap(extramap);
        }
        doctorFarmWareHouseTypeDao.create(farmWareHouseType);
    }
}
