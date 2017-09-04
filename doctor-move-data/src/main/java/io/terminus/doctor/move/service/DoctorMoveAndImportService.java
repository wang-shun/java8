package io.terminus.doctor.move.service;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.dto.DoctorFarmWithMobile;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.manager.DoctorMoveAndImportManager;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by xjn on 17/8/4.
 * 迁移与导入
 */
@Slf4j
@Service
public class DoctorMoveAndImportService {
    @Autowired
    public DoctorMoveAndImportManager moveAndImportManager;
    @Autowired
    public DoctorMoveDataService moveDataService;
    @Autowired
    public DoctorImportDataService importDataService;
    @Autowired
    public DoctorMoveBasicService moveBasicService;
    @Autowired
    public UserInitService userInitService;

    public void moveData(Long moveId, Sheet sheet) {
        log.info("move data starting");

        //1.猪场与用户
        List<DoctorFarm> farmList = moveFarmAndUser(moveId, sheet);

        farmList.forEach(doctorFarm -> {

            //2.基础数据
            moveBasic(moveId, doctorFarm);

            //3.打包事件依赖基础数据
            DoctorMoveBasicData moveBasicData = packageMoveBasicData(doctorFarm);

            //4.猪群
            moveGroup(moveId, moveBasicData);

            //5.猪
            movePig(moveId, moveBasicData);

            //6.仓库
            moveWareHouse();
        });

        log.info("move data end");

        //7.生成报表
        generateReport();
    }

    @Transactional
    public Long importData(DoctorImportSheet sheet) {
        log.info("import data starting");

        //导入猪场和用户
        DoctorFarm farm = importFarmAndUser(sheet.getFarm(), sheet.getStaff());

        //导入基础数据
        importBasic(farm, sheet.getBarn(), sheet.getBreed());

        //打包数据
        DoctorImportBasicData importBasicData = packageImportBasicData(farm);

        //导入猪事件
        importPig(sheet.getBoar(), sheet.getSow(), importBasicData);

        //导入猪群事件
        importGroup(sheet.getGroup(), importBasicData);

        //导入仓库
        importWareHouse();

        //基础数据与猪场关联
        importFarmBasics(farm.getId());

        log.info("import data end");
        return farm.getId();
    }

    private DoctorFarm importFarmAndUser(Sheet farmShit, Sheet staffShit) {
        log.info("import farm and user staring");
        Object[] results = importDataService.importOrgFarmUser(farmShit, staffShit);
        log.info("import farm and user end");
        return (DoctorFarm) results[1];
    }

    private void importBasic(DoctorFarm farm, Sheet barnSheet, Sheet breedSheet) {
        log.info("import basic staring");
        Map<String, Long> userMap = moveBasicService.getSubMap(farm.getOrgId());
        importDataService.importBarn(farm, userMap, barnSheet);
        userInitService.updatePermissionBarn(farm.getId());
        importDataService.importBreed(breedSheet);
        log.info("import basic end");
    }

    public DoctorImportBasicData packageImportBasicData(DoctorFarm farm) {
        log.info("package import basic staring");
        Map<String, Long> userMap = moveBasicService.getSubMap(farm.getOrgId());
        Map<String, DoctorBarn> barnMap = moveBasicService.getBarnMap2(farm.getId());
        Map<String, Long> breedMap = moveBasicService.getBreedMap();
        return DoctorImportBasicData.builder().doctorFarm(farm).userMap(userMap).barnMap(barnMap)
                .breedMap(breedMap).build();
    }

    public void importPig(Sheet boarSheet, Sheet sowSheet, DoctorImportBasicData importBasicData) {
        log.info("import pig staring");
        moveAndImportManager.importPig(boarSheet, sowSheet, importBasicData);
        log.info("import pig end");
    }

    public void importGroup(Sheet groupSheet, DoctorImportBasicData importBasicData) {
        log.info("import group staring");
        moveAndImportManager.importGroup(groupSheet, importBasicData);
        log.info("import group end");

    }

    public void importWareHouse() {
        // TODO: 17/8/28 导入仓库暂定,等待仓库重写

    }

    public void importFarmBasics(Long farmId) {
        log.info("import from basics staring");
        importDataService.importFarmBasics(farmId);
        log.info("import from basics end");
    }

    public List<DoctorFarm> moveFarmAndUser(Long moveId, Sheet sheet) {
        log.info("move farm and user starting");
        try {
            List<DoctorFarmWithMobile> farmWithMobileList = userInitService.init(null, null, moveId, sheet);
            return farmWithMobileList.stream().map(DoctorFarmWithMobile::getDoctorFarm)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("move farm and user error");
            throw e;
        }
    }

    public void moveBasic(Long moveId, DoctorFarm farm) {
        log.info("move basic staring");
        try {
            moveBasicService.moveAllBasic(moveId, farm);
        } catch (Exception e) {
            log.error("move basic error -- farm:{}", farm);
            throw e;
        }
    }

    public DoctorMoveBasicData packageMoveBasicData(DoctorFarm farm) {
        log.info("package move basic data starting");
        try {
            Map<String, DoctorBarn> barnMap = moveBasicService.getBarnMap(farm.getId());
            Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicService.getBasicMap();
            Map<String, Long> subMap = moveBasicService.getSubMap(farm.getOrgId());
            Map<String, DoctorChangeReason> changeReasonMap = moveBasicService.getReasonMap();
            Map<String, DoctorCustomer> customerMap = moveBasicService.getCustomerMap(farm.getId());
            Map<String, DoctorBasicMaterial> vaccMap = moveBasicService.getVaccMap();

            return DoctorMoveBasicData.builder().doctorFarm(farm).barnMap(barnMap).basicMap(basicMap).subMap(subMap)
                    .changeReasonMap(changeReasonMap).customerMap(customerMap).vaccMap(vaccMap)
                    .build();
        } catch (Exception e) {
            log.error("package move basic data error -- farm:{}", farm);
            throw e;
        }
    }

    /**
     * 注意!!! 猪会依赖猪群,所以迁移猪时确保猪群已经迁移或存在
     * @param moveId 迁移数据源id
     * @param moveBasicData 基础数据
     */
    public void movePig(Long moveId, DoctorMoveBasicData moveBasicData) {
        log.info("move pig starting");
        try {
            moveAndImportManager.movePig(moveId, moveBasicData);
        } catch (Exception e) {
            log.error("move pig error");
            throw e;
        }
    }

    public void moveGroup(Long moveId, DoctorMoveBasicData moveBasicData) {
        log.info("move group starting");
        try {
            moveAndImportManager.moveGroup(moveId, moveBasicData);
        } catch (Exception e) {
            log.error("move group error");
            throw e;
        }
    }

    public void moveWareHouse() {
        // TODO: 17/8/4
    }

    public void generateReport() {
        // TODO: 17/8/4
    }

}
