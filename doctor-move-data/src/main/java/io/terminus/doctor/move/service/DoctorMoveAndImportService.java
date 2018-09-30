package io.terminus.doctor.move.service;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.service.DoctorDailyGroupWriteService;
import io.terminus.doctor.event.service.DoctorDailyReportV2Service;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorReportWriteService;
import io.terminus.doctor.move.dto.DoctorFarmWithMobile;
import io.terminus.doctor.move.dto.DoctorImportBasicData;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.manager.DoctorMoveAndImportManager;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;

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
    @Autowired
    public DoctorDailyReportWriteService doctorDailyReportWriteService;
    @Autowired
    public DoctorDailyGroupWriteService doctorDailyGroupWriteService;
    @Autowired
    public DoctorMoveReportService doctorMoveReportService;
    @Autowired
    public DoctorDailyReportV2Service doctorDailyReportV2Service;
    @Autowired
    public DoctorReportWriteService doctorReportWriteService;

    public List<DoctorFarm> moveData(Long moveId, Sheet sheet) {
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
        return farmList;
    }

    @Transactional
    public Long importData(DoctorImportSheet sheet) {
        log.info("import data starting");
        DoctorFarm farm = null;
        try {
            //导入猪场和用户
            farm = importFarmAndUser(sheet.getFarm(), sheet.getStaff());

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
        } catch (Exception e) {
            importDataService.deleteUser(farm);
            throw e;
        }
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

    @Transactional
    public DoctorImportBasicData packageImportBasicData(DoctorFarm farm) {
        log.info("package import basic staring");
        Map<String, Long> userMap = moveBasicService.getSubMap(farm.getOrgId());
        Map<String, DoctorBarn> barnMap = moveBasicService.getBarnMap2(farm.getId());
        Map<String, Long> breedMap = moveBasicService.getBreedMap();
        return DoctorImportBasicData.builder().doctorFarm(farm).userMap(userMap).barnMap(barnMap)
                .breedMap(breedMap).defaultUser(moveAndImportManager.getPrimaryUser(farm.getId()))
                .build();
    }

    @Transactional
    public void importPig(Sheet boarSheet, Sheet sowSheet, DoctorImportBasicData importBasicData) {
        log.info("import pig staring");
        moveAndImportManager.importPig(boarSheet, sowSheet, importBasicData);
        log.info("import pig end");
    }

    @Transactional
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
            DoctorBarn defaultPregBarn = null;
            DoctorBarn defaultFarrowBarn = null;
            for(DoctorBarn doctorBarn : barnMap.values()) {
                if (isNull(defaultPregBarn) && Objects.equals(doctorBarn.getPigType(), PigType.PREG_SOW.getValue())) {
                    defaultPregBarn = doctorBarn;
                }
                if (isNull(defaultFarrowBarn) && Objects.equals(doctorBarn.getPigType(), PigType.DELIVER_SOW.getValue())) {
                    defaultFarrowBarn = doctorBarn;
                }
            }

            return DoctorMoveBasicData.builder().doctorFarm(farm).barnMap(barnMap).basicMap(basicMap).subMap(subMap)
                    .changeReasonMap(changeReasonMap).customerMap(customerMap).vaccMap(vaccMap)
                    .defaultPregBarn(defaultPregBarn).defaultFarrowBarn(defaultFarrowBarn)
                    .defaultUser(moveAndImportManager.getPrimaryUser(farm.getId())).build();
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

    //生成一年的报表
    public void generateReport(Long farmId){
        try {
            DateTime end = DateTime.now().withTimeAtStartOfDay(); //昨天开始时间
            DateTime begin = end.minusYears(1);
            new Thread(() -> {
                //刷新报表
                doctorDailyReportV2Service.flushFarmDaily(farmId, DateUtil.toDateString(begin.toDate()), DateUtil.toDateString(end.toDate()));
                doctorReportWriteService.flushNPD(Collections.singletonList(farmId), begin.toDate());
                doctorMoveReportService.moveParityMonthlyReport(farmId, 12);
                doctorMoveReportService.moveBoarMonthlyReport(farmId, 12);

                //数据同步
                doctorDailyReportV2Service.synchronizeDelta(farmId, begin.toDate(), OrzDimension.FARM.getValue());
                doctorDailyReportV2Service.syncEfficiency(farmId);

                //旧报表刷新
                doctorDailyReportWriteService.createDailyReports(farmId, begin.toDate(), end.toDate());
                doctorDailyGroupWriteService.createDailyGroupsByDateRange(farmId, begin.toDate(), end.toDate());
                doctorMoveReportService.moveDoctorRangeReport(farmId, 12);

            }).start();
        } catch (Exception e) {
            log.error("generate report error. farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
        }
    }

}
