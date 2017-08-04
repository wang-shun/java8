package io.terminus.doctor.move.service;

import io.terminus.doctor.move.dto.DoctorFarmWithMobile;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.manager.DoctorMoveAndImportManager;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
            DoctorMoveBasicData moveBasicData = packageMoveBasicData();

            //4.猪
            movePig(moveBasicData);

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
        List<DoctorFarmWithMobile> farmWithMobileList = userInitService.init(null, null, moveId, sheet);
        return farmWithMobileList.stream().map(DoctorFarmWithMobile::getDoctorFarm)
                .collect(Collectors.toList());
    }

    public void moveBasic(Long moveId, DoctorFarm farm) {
        moveBasicService.moveAllBasic(moveId, farm);
    }

    public DoctorMoveBasicData packageMoveBasicData() {
        // TODO: 17/8/4
        return null;
    }

    public void movePig(DoctorMoveBasicData moveBasicData) {

        //获取所有猪事件的原始数据

        //按猪维度分组

        //循环执行事件
        // TODO: 17/8/4 调用 moveAndImportManager

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
