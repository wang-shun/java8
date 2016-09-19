package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorMonthlyReportWriteService;
import io.terminus.doctor.event.service.DoctorParityMonthlyReportWriteService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
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

import java.util.Date;
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
    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;
    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorDailyReportWriteService doctorDailyReportWriteService;
    private final DoctorMonthlyReportWriteService doctorMonthlyReportWriteService;
    private final DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService;

    @Autowired
    public DoctorMoveDataController(UserInitService userInitService,
                                    WareHouseInitService wareHouseInitService,
                                    DoctorMoveBasicService doctorMoveBasicService,
                                    DoctorMoveDataService doctorMoveDataService,
                                    DoctorMoveReportService doctorMoveReportService,
                                    DoctorFarmDao doctorFarmDao,
                                    DoctorUserDataPermissionDao doctorUserDataPermissionDao,
                                    DoctorUserReadService doctorUserReadService,
                                    DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService,
                                    DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                    DoctorDailyReportWriteService doctorDailyReportWriteService,
                                    DoctorMonthlyReportWriteService doctorMonthlyReportWriteService,
                                    DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService) {
        this.userInitService = userInitService;
        this.wareHouseInitService = wareHouseInitService;
        this.doctorMoveBasicService = doctorMoveBasicService;
        this.doctorMoveDataService = doctorMoveDataService;
        this.doctorMoveReportService = doctorMoveReportService;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorDailyReportWriteService = doctorDailyReportWriteService;
        this.doctorMonthlyReportWriteService = doctorMonthlyReportWriteService;
        this.doctorParityMonthlyReportWriteService = doctorParityMonthlyReportWriteService;
    }

    /**
     * 更新数据源
     *
     * @return 是否成功
     */
    @RequestMapping(value = "/reload", method = RequestMethod.GET)
    public Boolean reloadMoveId() {
        try {
            doctorMoveDatasourceHandler.init();
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("move datasource connect failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
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
     * @param index  日报数据天数(默认365天)
     * @return 是否成功
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Boolean moveAll(@RequestParam("mobile") String mobile,
                           @RequestParam("moveId") Long moveId,
                           @RequestParam(value = "index", required = false) Integer index,
                           @RequestParam(value = "monthIndex", required = false) Integer monthIndex) {
        try {
            //1.迁移猪场信息
            log.warn("move user farm start, mobile:{}, moveId:{}", mobile, moveId);
            userInitService.init(mobile, moveId);
            log.warn("move user farm end");

            //多个猪场遍历插入
            getFarms(mobile).forEach(farm -> moveAllExclude(moveId, farm, mobile, index, monthIndex));
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
    private void moveAllExclude(Long moveId, DoctorFarm farm, String mobile, Integer index, Integer monthIndex) {
        //2.迁移基础数据(Basic, Customer, ChangeReason, Barn)
        log.warn("move basic start, moveId:{}", moveId);
        doctorMoveBasicService.moveAllBasic(moveId, farm);
        log.warn("move bascic end");

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

        log.warn("move farrow sow start, moveId:{}", moveId);
        doctorMoveDataService.updateFarrowSow(farm);
        log.warn("move farrow sow end");

        //首页统计
        movePigTypeStatistic(farm);

        //6.迁移猪场日报
        log.warn("move daily start, moveId:{}", moveId);
        doctorMoveReportService.moveDailyReport(moveId, farm.getId(), index);
        log.warn("move daily end");

        //7.迁移猪场月报
        log.warn("move monthly start, moveId:{}", moveId);
        doctorMoveReportService.moveMonthlyReport(farm.getId(), monthIndex);
        log.warn("move monthly end");

        //8.迁移猪场胎次分析月报
        log.warn("move parity monthly start, moveId:{}", moveId);
        doctorMoveReportService.moveParityMonthlyReport(farm.getId(), monthIndex);
        log.warn("move parity monthly end");

        //迁移仓库/物料
        log.warn("move warehouse start, mobile:{}, moveId:{}", mobile, moveId);
        wareHouseInitService.init(mobile, moveId, farm);
        log.warn("move warehouse end");
    }

    //统计下首页数据
    private void movePigTypeStatistic(DoctorFarm farm) {
        doctorPigTypeStatisticWriteService.statisticGroup(farm.getOrgId(), farm.getId());
        doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PIG_TYPE.BOAR.getKey());
        doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PIG_TYPE.SOW.getKey());
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
                                 @RequestParam("moveId") Long moveId,
                                 @RequestParam("farmId") Long farmId) {
        try {
            log.warn("move warehouse start, mobile:{}, moveId:{}", mobile, moveId);
            wareHouseInitService.init(mobile, moveId, doctorFarmDao.findById(farmId));
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

    /**
     * 日报
     * @param moveId
     * @param farmId
     * @param index
     * @return
     */
    @RequestMapping(value = "/daily", method = RequestMethod.GET)
    public Boolean moveDailyReport(@RequestParam("moveId") Long moveId,
                                   @RequestParam("farmId") Long farmId,
                                   @RequestParam(value = "index", required = false) Integer index) {
        try {
            log.warn("move daily report start, moveId:{}, farmId:{}, index:{}", moveId, farmId, index);
            doctorMoveReportService.moveDailyReport(moveId, farmId, index);
            log.warn("move daily report end");
            return true;
        } catch (Exception e) {
            log.error("move daily report failed, moveId:{}, farmId:{}, index:{}, cause:{}",
                    moveId, farmId, index, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 月报
     */
    @RequestMapping(value = "/monthly", method = RequestMethod.GET)
    public Boolean moveMonthlyReport(@RequestParam("farmId") Long farmId,
                                     @RequestParam(value = "index", required = false) Integer index) {
        try {
            log.warn("move monthly report start, farmId:{}, index:{}", farmId, index);
            doctorMoveReportService.moveMonthlyReport(farmId, index);
            log.warn("move monthly report end");
            return true;
        } catch (Exception e) {
            log.error("move monthly report failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 月报
     */
    @RequestMapping(value = "/monthly/date", method = RequestMethod.GET)
    public Boolean moveMonthlyReport(@RequestParam("farmId") Long farmId,
                                     @RequestParam("date") String date) {
        try {
            log.warn("move monthly report date start, farmId:{}, date:{}", farmId, date);
            doctorMonthlyReportWriteService.createMonthlyReport(farmId, DateUtil.toDate(date));
            log.warn("move monthly report date end");
            
            return true;
        } catch (Exception e) {
            log.error("move monthly report failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 胎次分析月报
     * @param farmId
     * @param index  往前几个月
     * @return
     */
    @RequestMapping(value = "/parityMonthly", method = RequestMethod.GET)
    public Boolean moveParityMonthlyReport(@RequestParam("farmId") Long farmId,
                                     @RequestParam(value = "index", required = false) Integer index) {
        try {
            log.warn("move parity monthly report start, farmId:{}, index:{}", farmId, index);
            doctorMoveReportService.moveParityMonthlyReport(farmId, index);
            log.warn("move parity monthly report end");
            return true;
        } catch (Exception e) {
            log.error("move parity monthly report failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 胎次分析月报
     */
    @RequestMapping(value = "/parityMonthly/date", method = RequestMethod.GET)
    public Boolean moveParityMonthlyReport(@RequestParam("farmId") Long farmId,
                                     @RequestParam("date") String date) {
        try {
            log.warn("move parity monthly report date start, farmId:{}, date:{}", farmId, date);
            doctorParityMonthlyReportWriteService.createMonthlyReport(farmId, DateUtil.toDate(date));
            log.warn("move parity monthly report date end");

            return true;
        } catch (Exception e) {
            log.error("move parity monthly report failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 分娩母猪
     */
    @RequestMapping(value = "/farrow", method = RequestMethod.GET)
    public Boolean moveFarrowSow(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move farrow sow start, farmId:{}", farmId);
            doctorMoveDataService.updateFarrowSow(farm);
            log.warn("move farrow sow end");
            return true;
        } catch (Exception e) {
            log.error("move farrow sow failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 母猪track extra字段更新
     */
    @RequestMapping(value = "/sowTrackExtra", method = RequestMethod.GET)
    public Boolean moveFSowTrackExtra(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move sow track extra start, farmId:{}", farmId);
            doctorMoveDataService.updateSowTrackExtraMap(farm);
            log.warn("move sow track extra end");
            return true;
        } catch (Exception e) {
            log.error("move sow track extra failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 拆分母猪转舍事件
     */
    @RequestMapping(value = "/sowTransBarn", method = RequestMethod.GET)
    public Boolean moveSowTransBarn(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move sow trans barn start, farmId:{}", farmId);
            doctorMoveDataService.updateSowTransBarn(farm);
            log.warn("move sow trans barn end");
            return true;
        } catch (Exception e) {
            log.error("move sow trans barn failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 实时日报统计
     */
    @RequestMapping(value = "/realtime/daily", method = RequestMethod.GET)
    public Boolean moveRealtimeDailyReport(@RequestParam("farmId") Long farmId,
                                           @RequestParam("date") String date) {
        try {
            log.warn("move realtime daily report start, farmId:{}", farmId);
            doctorDailyReportWriteService.createDailyReports(Lists.newArrayList(farmId), DateUtil.toDate(date));
            log.warn("move realtime daily report end");
            return true;
        } catch (Exception e) {
            log.error("move realtime daily report failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 实时日报统计, 从 date 直到今天的
     */
    @RequestMapping(value = "/realtime/daily/until", method = RequestMethod.GET)
    public Boolean moveRealtimeDailyReportUntilNow(@RequestParam("farmId") Long farmId,
                                                   @RequestParam("date") String date) {
        try {
            log.warn("move realtime daily report until now start, farmId:{}", farmId);

            Date since = DateUtil.toDate(date);

            DateUtil.getBeforeDays(new Date(), DateUtil.getDeltaDaysAbs(since, new Date()) + 1).forEach(d ->
                    doctorDailyReportWriteService.createDailyReports(Lists.newArrayList(farmId), d));
            log.warn("move realtime daily report until now end");
            return true;
        } catch (Exception e) {
            log.error("move realtime daily report until now failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 刷新母猪分娩总重
     */
    @RequestMapping(value = "/farrowWeight", method = RequestMethod.GET)
    public Boolean moveSowFarrowWeight(@RequestParam("farmId") Long farmId,
                                       @RequestParam("moveId") Long moveId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move sow farrow weight start, farmId:{}", farmId);
            doctorMoveDataService.updateSowFarrowWeight(moveId, farm);
            log.warn("move sow farrow weight end");
            return true;
        } catch (Exception e) {
            log.error("move sow farrow weight failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 刷新母猪流产事件
     */
    @RequestMapping(value = "/abort", method = RequestMethod.GET)
    public Boolean moveSowAbortion(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move sow farrow abort start, farmId:{}", farmId);
            doctorMoveDataService.updateSowAbortion(farm);
            log.warn("move sow farrow abort end");
            return true;
        } catch (Exception e) {
            log.error("move sow farrow abort failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 更新猪群内转外转
     */
    @RequestMapping(value = "/transGroupType", method = RequestMethod.GET)
    public Boolean updateTranGroupType(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("updateTranGroupType start, farmId:{}", farmId);
            doctorMoveDataService.updateTranGroupType(farm);
            log.warn("updateTranGroupType end");
            return true;
        } catch (Exception e) {
            log.error("updateTranGroupType failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 修正配种类型
     */
    @RequestMapping(value = "/mateType", method = RequestMethod.GET)
    public Boolean updateMateType(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("updateMateType start, farmId:{}", farmId);
            doctorMoveDataService.updateMateType(farm);
            log.warn("updateMateType end");
            return true;
        } catch (Exception e) {
            log.error("updateMateType failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 修正哺乳母猪数据
     */
    @RequestMapping(value = "/updateBuruTrack", method = RequestMethod.GET)
    public Boolean updateBuruTrack(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("updateBuruSowTrack start, farmId:{}", farmId);
            doctorMoveDataService.updateBuruTrack(farm);
            log.warn("updateBuruSowTrack end");
            return true;
        } catch (Exception e) {
            log.error("updateBuruSowTrack failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 修正产房仔猪数据
     */
    @RequestMapping(value = "/updateFarrowGroupTrack", method = RequestMethod.GET)
    public Boolean updateFarrowGroupTrack(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("updateFarrowGroupTrack start, farmId:{}", farmId);
            doctorMoveDataService.updateFarrowGroupTrack(farm);
            log.warn("updateFarrowGroupTrack end");
            return true;
        } catch (Exception e) {
            log.error("updateFarrowGroupTrack failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 更新猪群事件的目标/来源猪舍
     */
    @RequestMapping(value = "/updateGroupEventOtherBarn", method = RequestMethod.GET)
    public Boolean updateGroupEventOtherBarn(@RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("updateGroupEventOtherBarn start, farmId:{}", farmId);
            doctorMoveDataService.updateGroupEventOtherBarn(farm);
            log.warn("updateGroupEventOtherBarn end");
            return true;
        } catch (Exception e) {
            log.error("updateGroupEventOtherBarn failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 已关闭的猪群生成批次总结
     */
    @RequestMapping(value = "/createClosedGroupSummary", method = RequestMethod.GET)
    public Boolean createClosedGroupSummary(@RequestParam("farmId") Long farmId) {
        try {
            log.warn("createClosedGroupSummary start, farmId:{}", farmId);
            doctorMoveDataService.createClosedGroupSummary(farmId);
            log.warn("createClosedGroupSummary end");
            return true;
        } catch (Exception e) {
            log.error("createClosedGroupSummary failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 已关闭的猪群更新日龄
     */
    @RequestMapping(value = "/updateClosedGroupDayAge", method = RequestMethod.GET)
    public Boolean updateClosedGroupDayAge(@RequestParam("farmId") Long farmId, @RequestParam("moveId") Long moveId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("updateClosedGroupDayAge start, farmId:{}", farmId);
            doctorMoveDataService.updateClosedGroupDayAge(moveId, farm);
            log.warn("updateClosedGroupDayAge end");
            return true;
        } catch (Exception e) {
            log.error("updateClosedGroupDayAge failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }
}
