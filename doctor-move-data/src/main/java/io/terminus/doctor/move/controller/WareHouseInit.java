package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.B_WareHouse;
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
import io.terminus.doctor.warehouse.dao.DoctorWareHouseDao;
import io.terminus.doctor.warehouse.manager.MaterialInWareHouseManager;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
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

        //猪场
        List<DoctorFarm> farms = doctorFarmDao.findByIds(farmIds);
        //猪场Map, key = outId, value = farm
        Map<String, DoctorFarm> farmMap = farms.stream().collect(Collectors.toMap(DoctorFarm::getOutId, v -> v));

        //子账号
        List<Sub> subs = subDao.findByConditions(ImmutableMap.of("parentUserId", userId), null);
        //子账号map, key = realName, value = Sub
        Map<String, Sub> subMap = subs.stream().collect(Collectors.toMap(Sub::getRealName, v -> v));


        //1..先初始化仓库, 每种类型的仓库各一个
        List<B_WareHouse> list = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, B_WareHouse.class, DoctorMoveTableEnum.B_WareHouse));
        if(list != null && !list.isEmpty()){
            String managerName = list.get(0).getManager().split(",")[0];
            for(DoctorFarm farm : farmMap.values()){
                DoctorWareHouse wareHouse = new DoctorWareHouse();
                wareHouse.setFarmId(farm.getId());
                wareHouse.setFarmName(farm.getName());
                wareHouse.setManagerId(subMap.get(managerName).getUserId());
                wareHouse.setManagerName(managerName);
                for(WareHouseType type : WareHouseType.values()){
                    wareHouse.setType(type.getKey());
                    wareHouse.setWareHouseName(type.getDesc() + "仓库");
                    doctorWareHouseDao.create(wareHouse);
                }
            }
            // 用户有仓库信息,则应当继续
            this.insertBasicMaterial(dataSourceId);

            //往仓库里添加物料
            this.addMaterial2Warehouse(dataSourceId, farmMap);
        }
        // 这个 if 外面不能写代码
    }

    //往基础物料表加数据
    private void insertBasicMaterial(Long dataSourceId){
        //药品
        List<View_MedicineList> medicines = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_MedicineList.class, DoctorMoveTableEnum.View_MedicineList));
        medicines.stream().filter(medicine -> doctorBasicMaterialDao.findByTypeAndName(WareHouseType.MEDICINE, medicine.getMaterialName()) == null).forEach(medicine -> {
            DoctorBasicMaterial basicMaterial = new DoctorBasicMaterial();
            basicMaterial.setType(WareHouseType.MEDICINE.getKey());
            basicMaterial.setName(medicine.getMaterialName());
            basicMaterial.setSrm(medicine.getSrm());
            basicMaterial.setUnitGroupName(medicine.getUnitGroupText());
            basicMaterial.setUnitName(medicine.getUnitName());
            basicMaterial.setRemark(medicine.getRemark());
            doctorBasicMaterialDao.create(basicMaterial);
        });

        //疫苗
        List<View_VaccinationList> vaccinationLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_VaccinationList.class, DoctorMoveTableEnum.View_VaccinationList));
        vaccinationLists.stream().filter(medicine -> doctorBasicMaterialDao.findByTypeAndName(WareHouseType.VACCINATION, medicine.getMaterialName()) == null).forEach(medicine -> {
            DoctorBasicMaterial basicMaterial = new DoctorBasicMaterial();
            basicMaterial.setType(WareHouseType.VACCINATION.getKey());
            basicMaterial.setName(medicine.getMaterialName());
            basicMaterial.setSrm(medicine.getSrm());
            basicMaterial.setUnitGroupName(medicine.getUnitGroupText());
            basicMaterial.setUnitName(medicine.getUnitName());
            basicMaterial.setRemark(medicine.getRemark());
            doctorBasicMaterialDao.create(basicMaterial);
        });

        //原料
        List<View_RawMaterialList> rawMaterialLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_RawMaterialList.class, DoctorMoveTableEnum.View_RawMaterialList));
        rawMaterialLists.stream().filter(medicine -> doctorBasicMaterialDao.findByTypeAndName(WareHouseType.MATERIAL, medicine.getMaterialName()) == null).forEach(medicine -> {
            DoctorBasicMaterial basicMaterial = new DoctorBasicMaterial();
            basicMaterial.setType(WareHouseType.MATERIAL.getKey());
            basicMaterial.setName(medicine.getMaterialName());
            basicMaterial.setSrm(medicine.getSrm());
            basicMaterial.setUnitGroupName(medicine.getUnitGroupText());
            basicMaterial.setUnitName(medicine.getUnitName());
            basicMaterial.setRemark(medicine.getRemark());
            doctorBasicMaterialDao.create(basicMaterial);
        });

        //饲料
        List<View_FeedList> feedLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_FeedList.class, DoctorMoveTableEnum.View_FeedList));
        feedLists.stream().filter(medicine -> doctorBasicMaterialDao.findByTypeAndName(WareHouseType.FEED, medicine.getMaterialName()) == null).forEach(medicine -> {
            DoctorBasicMaterial basicMaterial = new DoctorBasicMaterial();
            basicMaterial.setType(WareHouseType.FEED.getKey());
            basicMaterial.setName(medicine.getMaterialName());
            basicMaterial.setSrm(medicine.getSrm());
            basicMaterial.setUnitGroupName(medicine.getUnitGroupText());
            basicMaterial.setUnitName(medicine.getUnitName());
            basicMaterial.setRemark(medicine.getRemark());
            doctorBasicMaterialDao.create(basicMaterial);
        });

        // 消耗品
        List<View_AssetList> assetLists = RespHelper.or500(doctorMoveDatasourceHandler.findAllData(dataSourceId, View_AssetList.class, DoctorMoveTableEnum.View_AssetList));
        assetLists.stream().filter(medicine -> doctorBasicMaterialDao.findByTypeAndName(WareHouseType.CONSUME, medicine.getMaterialName()) == null).forEach(medicine -> {
            DoctorBasicMaterial basicMaterial = new DoctorBasicMaterial();
            basicMaterial.setType(WareHouseType.CONSUME.getKey());
            basicMaterial.setName(medicine.getMaterialName());
            basicMaterial.setSrm(medicine.getSrm());
            basicMaterial.setUnitGroupName(medicine.getUnitGroupText());
            basicMaterial.setUnitName(medicine.getUnitName());
            basicMaterial.setRemark(medicine.getRemark());
            doctorBasicMaterialDao.create(basicMaterial);
        });
    }

    //往仓库里添加物料
    private void addMaterial2Warehouse(Long dataSourceId, Map<String, DoctorFarm> farmMap){

    }
}
