package io.terminus.doctor.move.service;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.dto.DoctorFarmWithMobile;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.manager.DoctorMoveAndImportManager;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

            //4.猪
            movePig(moveId, moveBasicData);

            //5.猪群
            moveGroup();

            //6.仓库
            moveWareHouse();
        });

        log.info("move data end");

        //7.生成报表
        generateReport();
    }

    public void importData() {
        // TODO: 17/8/4
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

            return DoctorMoveBasicData.builder().barnMap(barnMap).basicMap(basicMap).subMap(subMap)
                    .changeReasonMap(changeReasonMap).customerMap(customerMap).vaccMap(vaccMap)
                    .build();
        } catch (Exception e) {
            log.error("package move basic data error -- farm:{}", farm);
            throw e;
        }
    }

    public void movePig(Long moveId, DoctorMoveBasicData moveBasicData) {
        log.info("move pig starting");
        try {
            moveAndImportManager.movePig(moveId, moveBasicData);
        } catch (Exception e) {
            log.error("move pig error");
            throw e;
        }
    }

    public void moveGroup() {
        // TODO: 17/8/4
    }

    public void moveWareHouse() {
        // TODO: 17/8/4
    }

    public void generateReport() {
        // TODO: 17/8/4
    }

}
