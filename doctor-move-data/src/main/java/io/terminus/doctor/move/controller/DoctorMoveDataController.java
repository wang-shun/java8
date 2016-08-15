package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.move.service.DoctorMoveBasicService;
import io.terminus.doctor.move.service.DoctorMoveDataService;
import io.terminus.doctor.move.service.DoctorMoveReportService;
import io.terminus.doctor.move.service.UserInitService;
import io.terminus.doctor.move.service.WareHouseInitService;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorUserDataPermissionDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.parana.user.model.LoginType;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 迁移数据总控入口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/4
 */

@Slf4j
@RestController
@RequestMapping("/api/doctor/move/data")
public class DoctorMoveDataController {

    private final UserInitService userInitService;
    private final WareHouseInitService wareHouseInitService;
    private final DoctorMoveBasicService doctorMoveBasicService;
    private final DoctorMoveDataService doctorMoveDataService;
    private final DoctorMoveReportService doctorMoveReportService;
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final DoctorUserReadService doctorUserReadService;

    @Autowired
    public DoctorMoveDataController(UserInitService userInitService,
                                    WareHouseInitService wareHouseInitService,
                                    DoctorMoveBasicService doctorMoveBasicService,
                                    DoctorMoveDataService doctorMoveDataService,
                                    DoctorMoveReportService doctorMoveReportService,
                                    DoctorFarmDao doctorFarmDao,
                                    DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                                    DoctorUserReadService doctorUserReadService) {
        this.userInitService = userInitService;
        this.wareHouseInitService = wareHouseInitService;
        this.doctorMoveBasicService = doctorMoveBasicService;
        this.doctorMoveDataService = doctorMoveDataService;
        this.doctorMoveReportService = doctorMoveReportService;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorUserReadService = doctorUserReadService;
    }

    /**
     * 测试数据源连接是否正常
     * @param moveId 数据源id
     * @return 是否正常
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Boolean testMoveIdConnect(@RequestParam("moveId") Long moveId) {
        try {
            return notEmpty(userInitService.getFarmMember(moveId));
        } catch (Exception e) {
            log.error("move datasource connect failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
    }

    /**
     * 迁移全部数据
     * @param mobile 注册的手机号
     * @param moveId 数据源id
     * @return 是否成功
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Boolean moveAll(@RequestParam("mobile") String mobile,
                           @RequestParam("moveId") Long moveId) {
        try {
            //1.迁移猪场信息
            log.warn("move user farm start, mobile:{}, moveId:{}", mobile, moveId);
            userInitService.init(mobile, moveId);
            log.warn("move user farm end");

            //多个猪场遍历插入
            getFarms(mobile).forEach(farm -> moveAllExclude(moveId, farm, mobile));
            return true;
        } catch (Exception e) {
            log.error("move all data failed, mobile:{}, moveId:{}, cause:{}", mobile, moveId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    //获取刚才创建的猪场
    private List<DoctorFarm> getFarms(String mobile) {
        User user = RespHelper.orServEx(doctorUserReadService.findBy(mobile, LoginType.MOBILE));
        DoctorUserDataPermission permission = doctorUserDataPermissionDao.findByUserId(user.getId());
        return permission.getFarmIdsList().stream()
                .map(doctorFarmDao::findById)
                .collect(Collectors.toList());
    }

    //迁移剩下的数据
    private void moveAllExclude(Long moveId, DoctorFarm farm, String mobile) {
        //2.迁移基础数据(Basic, Customer, ChangeReason, Barn)
        log.warn("move basic start, moveId:{}", moveId);
        doctorMoveBasicService.moveAllBasic(moveId, farm);
        log.warn("move bascic end");

        //3.迁移仓库/物料
        log.warn("move warehouse start, mobile:{}, moveId:{}", mobile, moveId);
        wareHouseInitService.init(mobile, moveId);
        log.warn("move warehouse end");

        //4.迁移公猪 母猪 工作流
        try {
            log.warn("move pig start, moveId:{}", moveId);
            doctorMoveDataService.movePig(moveId, farm);
            log.warn("move pig end");
        } catch (Exception e) {
            doctorMoveDataService.deleteAllPigs(farm.getId());
            log.error("move pig failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            throw new ServiceException("move.pig.error");
        }
        log.warn("move workflow start");
        doctorMoveDataService.moveWorkflow(farm);
        log.warn("move workflow end");

        //5.迁移猪群
        log.warn("move group start, moveId:{}", moveId);
        doctorMoveDataService.moveGroup(moveId, farm);
        log.warn("move group end");

        //6.迁移猪场日报
        log.warn("move daily start, moveId:{}", moveId);
        doctorMoveReportService.moveDailyReport(moveId, farm.getId());
        log.warn("move daily end");
    }

    /**
     * 迁移猪场信息
     * @param mobile 注册手机号
     * @param moveId 数据源id
     * @return 是否成功
     */
    @RequestMapping(value = "/farm", method = RequestMethod.GET)
    public Boolean moveUserFarm(@RequestParam("mobile") String mobile,
                                @RequestParam("moveId") Long moveId) {
        try {
            log.warn("move user farm start, mobile:{}, moveId:{}", mobile, moveId);
            userInitService.init(mobile, moveId);
            log.warn("move user farm end");
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("move user farm failed, mobile:{}, moveId:{}, cause:{}", mobile, moveId, Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
    }

    /**
     * 迁移基础事件
     * @param moveId 数据源id
     * @param farmId 猪场id
     * @return 是否成功
     */
    @RequestMapping(value = "/basic", method = RequestMethod.GET)
    public Boolean moveBasic(@RequestParam("moveId") Long moveId, @RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move basic start, moveId:{}", moveId);
            doctorMoveBasicService.moveAllBasic(moveId, farm);
            log.warn("move bascic end");
            return true;
        } catch (Exception e) {
            log.error("move basic failed, moveId:{}, farmId:{}, cause:{}", 
                    moveId, farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 迁移仓库物料
     * @param mobile 注册手机号
     * @param moveId 数据源id
     * @return 是否成功
     */
    @RequestMapping(value = "/warehouse", method = RequestMethod.GET)
    public Boolean moveWareHouse(@RequestParam("mobile") String mobile, 
                                 @RequestParam("moveId") Long moveId) {
        try {
            log.warn("move warehouse start, mobile:{}, moveId:{}", mobile, moveId);
            wareHouseInitService.init(mobile, moveId);
            log.warn("move warehouse end");
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("move warehouse failed, mobile:{}, moveId:{}, cause:{}", mobile, moveId, Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
    }

    /**
     * 迁移猪
     * @param moveId 数据源id
     * @param farmId 猪场id
     * @return 是否成功
     */
    @RequestMapping(value = "/pig", method = RequestMethod.GET)
    public Boolean movePig(@RequestParam("moveId") Long moveId, @RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move pig start, moveId:{}", moveId);
            doctorMoveDataService.movePig(moveId, farm);
            log.warn("move pig end");
            return true;
        } catch (Exception e) {
            doctorMoveDataService.deleteAllPigs(farmId);
            log.error("move pig failed, moveId:{}, farmId:{}, cause:{}",
                    moveId, farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 迁移猪群
     * @param moveId 数据源id
     * @param farmId 猪场id
     * @return 是否成功
     */
    @RequestMapping(value = "/group", method = RequestMethod.GET)
    public Boolean moveGroup(@RequestParam("moveId") Long moveId, @RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move group start, moveId:{}", moveId);
            doctorMoveDataService.moveGroup(moveId, farm);
            log.warn("move group end");
            return true;
        } catch (Exception e) {
            log.error("move group failed, moveId:{}, farmId:{}, cause:{}",
                    moveId, farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    @RequestMapping(value = "/workflow", method = RequestMethod.GET)
    public Boolean moveWorkflow(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move workflow start, farmId:{}", farmId);
            doctorMoveDataService.moveWorkflow(farm);
            log.warn("move workflow end");
            return true;
        } catch (Exception e) {
            log.error("move workflow failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    @RequestMapping(value = "/daily", method = RequestMethod.GET)
    public Boolean moveDailyReport(@RequestParam("moveId") Long moveId, @RequestParam("farmId") Long farmId) {
        try {
            log.warn("move daily report start, moveId:{}, farmId:{}", moveId, farmId);
            doctorMoveReportService.moveDailyReport(moveId, farmId);
            log.warn("move daily report end");
            return true;
        } catch (Exception e) {
            log.error("move daily report failed, moveId:{}, farmId:{}, cause:{}", moveId, farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }
}
