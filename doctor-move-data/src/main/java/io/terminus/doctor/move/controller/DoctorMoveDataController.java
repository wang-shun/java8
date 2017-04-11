package io.terminus.doctor.move.controller;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.RespWithExHelper;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.service.DoctorBoarMonthlyReportWriteService;
import io.terminus.doctor.event.service.DoctorCommonReportWriteService;
import io.terminus.doctor.event.service.DoctorDailyReportWriteService;
import io.terminus.doctor.event.service.DoctorEditGroupEventService;
import io.terminus.doctor.event.service.DoctorEventModifyRequestWriteService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorParityMonthlyReportWriteService;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticWriteService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.move.dto.DoctorFarmWithMobile;
import io.terminus.doctor.move.model.View_FarmMember;
import io.terminus.doctor.move.service.DoctorImportDataService;
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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;
import static io.terminus.common.utils.Arguments.notNull;

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
    private static final JsonMapper MAPPER = JsonMapper.nonEmptyMapper();

    private final UserInitService userInitService;
    private final WareHouseInitService wareHouseInitService;
    private final DoctorMoveBasicService doctorMoveBasicService;
    private final DoctorMoveDataService doctorMoveDataService;
    private final DoctorMoveReportService doctorMoveReportService;
    private final DoctorFarmDao doctorFarmDao;
    private final DoctorUserDataPermissionDao doctorUserDataPermissionDao;
    private final DoctorUserReadService doctorUserReadService;
    private final DoctorPigTypeStatisticWriteService doctorPigTypeStatisticWriteService;
    private final DoctorDailyReportWriteService doctorDailyReportWriteService;
    private final DoctorCommonReportWriteService doctorCommonReportWriteService;
    private final DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService;
    private final DoctorBoarMonthlyReportWriteService doctorBoarMonthlyReportWriteService;
    private final DoctorImportDataService doctorImportDataService;
    private final DoctorPigWriteService doctorPigWriteService;
    private final DoctorEventModifyRequestWriteService doctorEventModifyRequestWriteService;
    private final DoctorEditGroupEventService doctorEditGroupEventService;
    private final DoctorGroupReadService doctorGroupReadService;
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
                                    DoctorDailyReportWriteService doctorDailyReportWriteService,
                                    DoctorCommonReportWriteService doctorCommonReportWriteService,
                                    DoctorParityMonthlyReportWriteService doctorParityMonthlyReportWriteService,
                                    DoctorBoarMonthlyReportWriteService doctorBoarMonthlyReportWriteService,
                                    DoctorImportDataService doctorImportDataService,
                                    DoctorPigWriteService doctorPigWriteService,
                                    DoctorEventModifyRequestWriteService doctorEventModifyRequestWriteService,
                                    DoctorEditGroupEventService doctorEditGroupEventService,
                                    DoctorGroupReadService doctorGroupReadService) {
        this.userInitService = userInitService;
        this.wareHouseInitService = wareHouseInitService;
        this.doctorMoveBasicService = doctorMoveBasicService;
        this.doctorMoveDataService = doctorMoveDataService;
        this.doctorMoveReportService = doctorMoveReportService;
        this.doctorFarmDao = doctorFarmDao;
        this.doctorUserDataPermissionDao = doctorUserDataPermissionDao;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorPigTypeStatisticWriteService = doctorPigTypeStatisticWriteService;
        this.doctorDailyReportWriteService = doctorDailyReportWriteService;
        this.doctorCommonReportWriteService = doctorCommonReportWriteService;
        this.doctorParityMonthlyReportWriteService = doctorParityMonthlyReportWriteService;
        this.doctorBoarMonthlyReportWriteService = doctorBoarMonthlyReportWriteService;
        this.doctorImportDataService = doctorImportDataService;
        this.doctorPigWriteService = doctorPigWriteService;
        this.doctorEventModifyRequestWriteService = doctorEventModifyRequestWriteService;
        this.doctorEditGroupEventService = doctorEditGroupEventService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    /**
     * 测试数据源连接是否正常
     * @param moveId 数据源id
     * @return 是否正常
     */
    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public Boolean testMoveIdConnect(@RequestParam("moveId") Long moveId) {
        try {
            List<View_FarmMember> farmMembers = userInitService.getFarmMember(moveId);
            log.info("test connect info, farmMember:{}", farmMembers);
            return notEmpty(farmMembers);
        } catch (Exception e) {
            log.error("move datasource connect failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
    }

//    /**
//     * 迁移全部数据
//     * @param mobile 注册的手机号
//     * @param moveId 数据源id
//     * @param index  日报数据天数(默认365天)
//     * @return 是否成功
//     */
//    @RequestMapping(value = "/all", method = RequestMethod.GET)
//    public Boolean moveAll(@RequestParam("mobile") String mobile,
//                           @RequestParam("loginName") String loginName,
//                           @RequestParam("moveId") Long moveId,
//                           @RequestParam(value = "index", required = false) Integer index,
//                           @RequestParam(value = "monthIndex", required = false) Integer monthIndex) {
//        try {
//            //1.迁移猪场信息
//            log.warn("move user farm start, mobile:{}, moveId:{}", mobile, moveId);
//            List<DoctorFarmWithMobile> farmList = userInitService.init(loginName, mobile, moveId, null);
//            log.warn("move user farm end");
//
//            //多个猪场遍历插入
//            getFarms(mobile).forEach(farm -> moveAllExclude(moveId, farm, mobile, index, monthIndex));
//
//            //把所有猪舍添加到所有用户的权限里去
//            userInitService.updatePermissionBarn(mobile);
//            log.warn("all data moved successfully, CONGRATULATIONS!!!");
//            return true;
//        } catch (Exception e) {
//            log.error("move all data failed, mobile:{}, moveId:{}, cause:{}", mobile, moveId, Throwables.getStackTraceAsString(e));
//            return false;
//        }
//    }

    /**
     * 迁移全部数据
     * @param mobile 注册的手机号
     * @param moveId 数据源id
     * @param index  日报数据天数(默认365天)
     * @return 是否成功
     */
    @RequestMapping(value = "/moveAllWithExcel", method = RequestMethod.GET)
    public Boolean moveAllWithExcel(@RequestParam(value = "mobile", required = false) String mobile,
                                    @RequestParam(value = "loginName", required = false) String loginName,
                                    @RequestParam("moveId") Long moveId,
                                    @RequestParam("path") String path,
                                    @RequestParam(value = "index", required = false) Integer index,
                                    @RequestParam(value = "monthIndex", required = false) Integer monthIndex) {
        try {
            //1.迁移猪场信息
            log.warn("move user farm start, mobile:{}, moveId:{}, path:{}", mobile, moveId, path);

            List<DoctorFarmWithMobile> farmList = userInitService.init(loginName, mobile, moveId, importFarmInfoExcel(path));
            log.warn("move user farm end");

            //多个猪场遍历插入
            farmList.forEach(farmWithMobile -> moveAllExclude(moveId, farmWithMobile.getDoctorFarm(), farmWithMobile.getMobile(), index, monthIndex));

            //把所有猪舍添加到所有用户的权限里去
            farmList.forEach(farmWithMobile -> userInitService.updatePermissionBarn(farmWithMobile.getDoctorFarm().getId()));
            log.warn("all data moved successfully, CONGRATULATIONS!!!");
            return true;
        } catch (Exception e) {
            log.error("move all data failed, mobile:{}, moveId:{}, cause:{}", mobile, moveId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    private Sheet importFarmInfoExcel(String path) {
        log.info("importFarmInfoExcel path:{}", path);
        InputStream inputStream = null;
        try {
            File file = new File(path);
            String fileType;
            if (file.getName().endsWith(".xlsx")) {
                fileType = "xlsx";
            } else if (file.getName().endsWith(".xls")) {
                fileType = "xls";
            } else {
                throw new ServiceException("file.type.error");
            }
            inputStream = new FileInputStream(file);
            Workbook workbook;
            switch (fileType) {
                case "xlsx":
                    workbook = new XSSFWorkbook(inputStream);  //2007
                    break;
                case "xls":
                    workbook = new HSSFWorkbook(inputStream);  //2003
                    break;
                default:
                    throw new ServiceException("file.type.error");
            }
            return getSheet(workbook, "猪场");
        } catch (Exception e) {
            log.error("import farm info excel failed, path:{}, cause:{}", path, Throwables.getStackTraceAsString(e));
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //ignore this exception
                }
            }
        }
        return null;
    }

    private Sheet getSheet(Workbook wk, String name) {
        Sheet sheet = wk.getSheet(name);
        if (sheet == null) {
            throw new JsonResponseException("sheet.not.found：" + name);
        }
        return sheet;
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

        //4.迁移公猪 母猪
        try {
            Stopwatch watch = Stopwatch.createStarted();
            log.warn("move pig start, moveId:{}", moveId);
            doctorMoveDataService.movePig(moveId, farm);
            watch.stop();
            int minute = Long.valueOf(watch.elapsed(TimeUnit.MINUTES) + 1).intValue();
            log.warn("move pig end, cost {} minutes, now dump ES", minute);
        } catch (Exception e) {
            doctorMoveDataService.deleteAllPigs(farm.getId());
            log.error("move pig failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            throw new ServiceException("move.pig.error");
        }

        //5.迁移猪群
        Stopwatch watch = Stopwatch.createStarted();
        log.warn("move group start, moveId:{}", moveId);
        doctorMoveDataService.moveGroup(moveId, farm);
        watch.stop();
        int minute = Long.valueOf(watch.elapsed(TimeUnit.MINUTES) + 1).intValue();
        log.warn("move group end, cost {} minutes", minute);

        log.warn("move farrow sow start, moveId:{}", moveId);
        doctorMoveDataService.updateFarrowSow(farm);
        log.warn("move farrow sow end");

        log.warn("updateBuruSowTrack start, farmId:{}", farm.getId());
        doctorMoveDataService.updateBuruTrack(farm);
        log.warn("updateBuruSowTrack end");

        //首页统计
        movePigTypeStatistic(farm);

        //6.迁移猪场日报
        log.warn("move daily start, moveId:{}", moveId);
        doctorMoveReportService.moveDailyReport(moveId, farm.getId(), index);
        log.warn("move daily end");

        //7.迁移猪场周报
        log.warn("move weekly start, moveId:{}", moveId);
        doctorMoveReportService.moveWeeklyReport(farm.getId(), monthIndex == null ? null : monthIndex * 4);
        log.warn("move weekly end");


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

        //迁移仓库/物料
        log.warn("move farmBasic start, mobile:{}, moveId:{}", mobile, moveId);
        doctorImportDataService.importFarmBasics(farm.getId());
        log.warn("move farmBasic end");

        log.warn("move xrnm permission, mobile:{}, moveId:{}", mobile, moveId);
        doctorImportDataService.createOrUpdateAdminPermission();
        log.warn("move xrmm permission end");
    }

    //统计下首页数据
    private void movePigTypeStatistic(DoctorFarm farm) {
        doctorPigTypeStatisticWriteService.statisticGroup(farm.getOrgId(), farm.getId());
        doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PigSex.BOAR.getKey());
        doctorPigTypeStatisticWriteService.statisticPig(farm.getOrgId(), farm.getId(), DoctorPig.PigSex.SOW.getKey());
    }

    @RequestMapping(value = "/flushStatistic", method = RequestMethod.GET)
    public Boolean flushStatistic(@RequestParam(value = "farmId", required = false) Long farmId) {
        log.warn("flushStatistic start, farmId:{}", farmId);
        if (farmId == null) {
            doctorFarmDao.findAll().forEach(this::movePigTypeStatistic);
        } else {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            if (farm == null) {
                throw new JsonResponseException("farm.not.found");
            }
            movePigTypeStatistic(farm);
        }
        log.warn("flushStatistic end, farmId:{}", farmId);
        return true;
    }

    /**
     * 迁移猪场信息
     * @param mobile 注册手机号
     * @param moveId 数据源id
     * @return 是否成功
     */
    @RequestMapping(value = "/farm", method = RequestMethod.GET)
    public Boolean moveUserFarm(@RequestParam("mobile") String mobile,
                                @RequestParam("loginName") String loginName,
                                @RequestParam("moveId") Long moveId,
                                @RequestParam("path") String path) {
        try {
            log.warn("move user farm start, mobile:{}, moveId:{}", mobile, moveId);
            userInitService.init(loginName, mobile, moveId, importFarmInfoExcel(path));
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

    @RequestMapping(value = "/updatePigEvents", method = RequestMethod.GET)
    public Boolean updateParityAndBoarCode(@RequestParam(value = "farmId", required = false) Long farmId){
        try {
            List<Long> listFarmIds = Lists.newArrayList();
            if(Arguments.isNull(farmId)){
                listFarmIds = getAllFarmIds();
            }else{
                listFarmIds.add(farmId);
            }
            listFarmIds.forEach(id -> {
                DoctorFarm farm = doctorFarmDao.findById(id);
                log.warn("{} update parity and boarCode start, farmId:{}", DateUtil.toDateTimeString(new Date()), id);
                doctorMoveDataService.updateParityAndBoarCode(farm);
                log.warn("{} update parity and boarCode end", DateUtil.toDateTimeString(new Date()));
            });
            return true;
        } catch (Exception e) {
            log.error("update parity and boarCode failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    @RequestMapping(value = "/updatePigEventsByPigId", method = RequestMethod.GET)
    public Boolean updateParityAndBoarCodeByPigId(@RequestParam("pigId") Long pigId){
        try {
            log.warn("update parity and boarCode start, pigId:{}", pigId);
            doctorMoveDataService.updateParityAndBoarCodeByPigId(pigId);
            log.warn("update parity and boarCode end");
            return true;
        } catch (Exception e) {
            log.error("update parity and boarCode failed, pigId:{}, cause:{}", pigId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    @RequestMapping(value = "/updateExcelImportErrorPigEvents", method = RequestMethod.GET)
    public Boolean updateExcelImportErrorPigEvents(@RequestParam(value = "farmId", required = false) Long farmId){
        try {
            List<Long> listFarmIds = getExcelImportFarmIds();
            if(!Arguments.isNull(farmId) && listFarmIds.contains(farmId)){
                listFarmIds = Lists.newArrayList(farmId);
            }
            if(!Arguments.isNull(farmId) && !listFarmIds.contains(farmId)){
                log.info("farmId not import by excel, farmId: {}", farmId);
                return false;
            }
                listFarmIds.forEach(id -> {
                DoctorFarm farm = doctorFarmDao.findById(id);
                log.warn("{} update parity and boarCode start, farmId:{}", DateUtil.toDateTimeString(new Date()), id);
                doctorMoveDataService.updateExcelMOVEErrorPigEvents(farm);
                log.warn("{} update parity and boarCode end", DateUtil.toDateTimeString(new Date()));
            });
            return true;
        } catch (Exception e) {
            log.error("update parity and boarCode failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    @RequestMapping(value = "/updatePigEventExtra", method = RequestMethod.GET)
    public Boolean updatePigEventExtra(@RequestParam("farmId") Long farmId){
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("update pigevent extra start, farmId:{}", farmId);
            doctorMoveDataService.updateFosterSowCode(farm);
            log.warn("update pigevent extra end");
            return true;
        } catch (Exception e) {
            log.error("update pigevent extra failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
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

    private List<Long> getAllFarmIds() {
        return doctorFarmDao.findAll().stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }

    private List<Long> getExcelImportFarmIds() {
        List<DoctorFarm> farmList = doctorFarmDao.findBySource(SourceType.IMPORT.getValue());
        if(Arguments.isNullOrEmpty(farmList)){
            return Lists.newArrayList();
        }
        return farmList.stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }

    /**
     * 迁移从since开始的日报, only = true, 只迁移 since 这一天的数据, 如果farmId=null, 则全部猪场
     */
    @RequestMapping(value = "/daily/since", method = RequestMethod.GET)
    public Boolean moveDailyReport(@RequestParam(value = "farmId", required = false) Long farmId,
                                   @RequestParam("since") String since,
                                   @RequestParam(value = "only", defaultValue = "false") boolean only) {
        try {
            log.warn("move daily report since start, farmId:{}, since:{}, only:{}", farmId, since, only);

            Date startAt = DateUtil.toDate(since);
            if (startAt == null || startAt.after(new Date())) {
                return false;
            }
            if (farmId == null) {
                if (only) {
                    doctorDailyReportWriteService.createDailyReports(getAllFarmIds(), startAt);
                } else {
                    getAllFarmIds().forEach(fid -> doctorDailyReportWriteService.createDailyReports(startAt, new Date(), fid));
                }
            } else {
                if (only) {
                    doctorDailyReportWriteService.createDailyReports(Lists.newArrayList(farmId), startAt);
                } else {
                    doctorDailyReportWriteService.createDailyReports(startAt, new Date(), farmId);
                }
            }
            log.warn("move daily report since end");
            return true;
        } catch (Exception e) {
            log.error("move daily report since failed, farmId:{}, since:{}, only:{}, cause:{}",
                    farmId, since, only, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 月报/周报
     */
    @RequestMapping(value = "/monthly/since", method = RequestMethod.GET)
    public Boolean moveMonthlyReport(@RequestParam(value = "farmId", required = false) Long farmId,
                                     @RequestParam("since") String since,
                                     @RequestParam(value = "only", defaultValue = "false") boolean only) {
        try {
            log.warn("move monthly report since start, farmId:{}, since:{}, only:{}", farmId, since, only);

            Date startAt = DateUtil.toDate(since);
            if (startAt == null || startAt.after(new Date())) {
                return false;
            }
            int index;
            if (only) {
                index = 1;
            } else {
                index = DateUtil.getDeltaMonthsAbs(startAt, new Date()) + 1;
            }

            if (farmId == null) {
                List<Long> farmIds = getAllFarmIds();
                farmIds.forEach(fid -> {
                    doctorMoveReportService.moveMonthlyReport(fid, index);
                    doctorMoveReportService.moveWeeklyReport(fid, index);
                    // 周数用月数*5
                    doctorMoveReportService.moveParityMonthlyReport(fid, index * 5);
                    doctorMoveReportService.moveBoarMonthlyReport(fid, index);
                });
            } else {
                doctorMoveReportService.moveMonthlyReport(farmId, index);
                doctorMoveReportService.moveWeeklyReport(farmId, index * 5);
                doctorMoveReportService.moveParityMonthlyReport(farmId, index);
                doctorMoveReportService.moveBoarMonthlyReport(farmId, index);
            }
            log.warn("move monthly report since end");
            return true;
        } catch (Exception e) {
            log.error("move monthly report since failed, farmId:{}, since:{}, only:{}", farmId, since, only, Throwables.getStackTraceAsString(e));
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
            doctorCommonReportWriteService.createMonthlyReport(farmId, DateUtil.toDate(date));
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
     * 公猪生产成绩月报
     * @param farmId
     * @param index  往前几个月
     * @return
     */
    @RequestMapping(value = "/boarMonthly", method = RequestMethod.GET)
    public Boolean moveBoarMonthlyReport(@RequestParam("farmId") Long farmId,
                                           @RequestParam(value = "index", required = false) Integer index) {
        try {
            log.warn("move boar monthly report start, farmId:{}, index:{}", farmId, index);
            doctorMoveReportService.moveBoarMonthlyReport(farmId, index);
            log.warn("move boar monthly report end");
            return true;
        } catch (Exception e) {
            log.error("move boar monthly report failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 公猪生产成绩月报
     */
    @RequestMapping(value = "/boarMonthly/date", method = RequestMethod.GET)
    public Boolean moveBoarMonthlyReport(@RequestParam("farmId") Long farmId,
                                           @RequestParam("date") String date) {
        try {
            log.warn("move parity monthly report date start, farmId:{}, date:{}", farmId, date);
            doctorBoarMonthlyReportWriteService.createMonthlyReport(farmId, DateUtil.toDate(date));
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

    /**
     * 刷新猪状态
     * @return
     */
    @RequestMapping(value = "/refresh", method = RequestMethod.GET)
    public Boolean refreshPigStatus(){
        return RespHelper.or500(doctorMoveDataService.refreshPigStatus());
    }

    /**
     * 刷分娩母猪track的group_id
     */
    @RequestMapping(value = "/flushFarrowGroupTrackGroupId", method = RequestMethod.GET)
    public Boolean flushFarrowGroupTrackGroupId(@RequestParam(value = "farmId", required = false) Long farmId) {
        try {
            log.warn("flushFarrowGroupTrackGroupId start, farmId:{}", farmId);
            if (farmId != null) {
                doctorMoveDataService.flushFarrowSowTrackGroupId(farmId);
            } else {
                getAllFarmIds().forEach(doctorMoveDataService::flushFarrowSowTrackGroupId);
            }
            log.warn("flushFarrowGroupTrackGroupId end");
            return true;
        } catch (Exception e) {
            log.error("flushFarrowGroupTrackGroupId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 刷分娩事件的group_id
     */
    @RequestMapping(value = "/flushFarrowEventGroupId", method = RequestMethod.GET)
    public Boolean flushFarrowEventGroupId(@RequestParam(value = "farmId", required = false) Long farmId) {
        try {
            log.warn("flushFarrowEventGroupId start, farmId:{}", farmId);
            if (farmId != null) {
                doctorMoveDataService.flushFarrowEventGroupId(farmId);
            } else {
                getAllFarmIds().forEach(doctorMoveDataService::flushFarrowEventGroupId);
            }
            log.warn("flushFarrowEventGroupId end");
            return true;
        } catch (Exception e) {
            log.error("flushFarrowEventGroupId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 刷断奶事件的group_id
     */
    @RequestMapping(value = "/flushWeanEventGroupId", method = RequestMethod.GET)
    public Boolean flushWeanEventGroupId(@RequestParam(value = "farmId", required = false) Long farmId) {
        try {
            log.warn("flushWeanEventGroupId start, farmId:{}", farmId);
            if (farmId != null) {
                doctorMoveDataService.flushWeanEventGroupId(farmId);
            } else {
                getAllFarmIds().forEach(doctorMoveDataService::flushWeanEventGroupId);
            }
            log.warn("flushWeanEventGroupId end");
            return true;
        } catch (Exception e) {
            log.error("flushWeanEventGroupId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    /**
     * 修改猪的耳号
     */
    @RequestMapping(value = "/updateCodes", method = RequestMethod.GET)
    @ResponseBody
    public boolean updatePigCodes(@RequestParam String pigCodeUpdates) {
        List<DoctorPig> pigs = MAPPER.fromJson(pigCodeUpdates, MAPPER.createCollectionType(List.class, DoctorPig.class));
        return RespHelper.or500(doctorPigWriteService.updatePigCodes(pigs));
    }

    /**
<<<<<<< HEAD
     * 推演track
     * @param pigId 猪id
     * @return
     */
    @RequestMapping(value = "/elicitPigTrack", method = RequestMethod.GET)
    public Boolean elicitPigTrack(@RequestParam Long pigId){
        RespWithExHelper.or500(doctorEventModifyRequestWriteService.elicitPigTrack(pigId));
        return true;
    }

    /**
     * 推演一个猪场的猪track
     * @param farmId 猪场id
     */
    @RequestMapping(value = "/batchElicitPigTrack", method = RequestMethod.GET)
    public Boolean batchElicitPigTrack(@RequestParam(required = false) Long farmId) {
        log.info("batchElicitPigTrack starting, farmId:{}", farmId);
        List<Long> farmIds;
        if (notNull(farmId)) {
            farmIds = Lists.newArrayList(farmId);
        } else {
            farmIds = doctorFarmDao.findAll().stream().map(DoctorFarm::getId).collect(Collectors.toList());
        }
        doctorEventModifyRequestWriteService.batchElicitPigTrack(farmIds);
        log.info("batchElicitPigTrack ending");
        return true;
    }

    /**
     * 猪群推演
     * @param farmId 猪场id
     * @param groupId 猪群id
     * @return
     */
    @RequestMapping(value = "/fix-events", method = RequestMethod.GET)
    public Response<String> reElicitGroupEvent(@RequestParam(required = false) Long farmId, @RequestParam(required = false) Long groupId){
        try{
            if(Arguments.isNull(farmId) && Arguments.isNull(groupId)){
                log.error("farmId, groupId need one ");
                return Response.fail("farmId, groupId need one");
            }
            List<Long> groupIds = Lists.newArrayList();
            if(Arguments.isNull(groupId) && !Arguments.isNull(farmId)){
                List<DoctorGroup> groupList = RespHelper.orServEx(doctorGroupReadService.findGroupsByFarmId(farmId));
                groupIds = groupList.stream().map(DoctorGroup::getId).collect(Collectors.toList());
            }else{
                groupIds.add(groupId);
            }
            if(Arguments.isNullOrEmpty(groupIds)){
                log.error("no groups find, farmId: {}", farmId);
            }
            doctorEditGroupEventService.reElicitGroupEvent(groupIds);
        }catch (Exception e){
            log.error("fix group event error, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail(Throwables.getStackTraceAsString(e));
        }
        return Response.ok("处理成功!!!");
    }

    @RequestMapping(value = "/fix-all-events", method = RequestMethod.GET)
    public Response<String> reElicitAllGroupEvent(@RequestParam(required = false) Long farmId){
        log.info("elicit group start, now is : {}", DateUtil.toDateTimeString(new Date()));
        try{
            List<Long> farmIds = Lists.newArrayList();
            if(Arguments.isNull(farmId)){
                farmIds = getAllFarmIds();
            }else{
                farmIds.add(farmId);
            }
            for(Long id : farmIds){
                log.info("{} elicit group start, farmId : {}", DateUtil.toDateTimeString(new Date()), id);
                List<Long> groupIds = Lists.newArrayList();
                List<DoctorGroup> groupList = RespHelper.orServEx(doctorGroupReadService.findGroupsByFarmId(id));
                groupIds = groupList.stream().map(DoctorGroup::getId).collect(Collectors.toList());

                if(Arguments.isNullOrEmpty(groupIds)){
                    log.error("no groups find, farmId: {}", farmId);
                }
                try{
                    doctorEditGroupEventService.reElicitGroupEvent(groupIds);
                }catch(Exception e){
                    continue;
                }

                log.info("{} elicit group end, farmId : {}", DateUtil.toDateTimeString(new Date()), id);
            }

        }catch (Exception e){
            log.error("fix group event error, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail(Throwables.getStackTraceAsString(e));
        }
        log.info("elicit group end, now is : {}", DateUtil.toDateTimeString(new Date()));
        return Response.ok("处理成功!!!");
    }

    /**
     * 旧数据,生成相对应得猪群断奶事件
     * @param farmId 猪场id -1时所有猪场
     */
    @RequestMapping(value = "/generateGroupWeanEvent", method = RequestMethod.GET)
    public Boolean generateGroupWeanEvent(@RequestParam Long farmId) {
        log.info("generateGroupWeanEvent starting, farmId:{}", farmId);
        doctorMoveDataService.generateGroupWeanEvent(farmId);
        log.info("generateGroupWeanEvent ending");
        return true;
    }


    @RequestMapping(value = "/group/snapshots")
    public Boolean flushGroupDateFormat(@RequestParam(required = false) Long farmId) {
        log.warn("{} flush group dateformat start", DateUtil.toDateTimeString(new Date()));
        List<Long> farmIds = Lists.newArrayList();
        try {
            if (Arguments.isNull(farmId)) {
                farmIds = getAllFarmIds();
            } else {
                farmIds.add(farmId);
            }
            farmIds.forEach(id -> {
                log.warn("{} flush farm {} group dateformat start", DateUtil.toDateTimeString(new Date()), id);
                doctorMoveDataService.flushGroupSnapshotsToInfoDateFormat(id);
            });
        } catch (Exception e) {
            log.error("flush group dateformat failed, cause: {}", Throwables.getStackTraceAsString(e));
            return false;
        }
        log.warn("{} flush group dateformat end", DateUtil.toDateTimeString(new Date()));
        return true;
    }
    /**
     * 更新用户名
     * @param userId 用户id
     * @param userName 新用户名
     * @return 更新是否成功
     */
    @RequestMapping(value = "/updateUserName", method = RequestMethod.GET)
    public Boolean updateUserName(@RequestParam Long userId, @RequestParam String userName) {
        log.info("update user name starting, userId:{}, userName:{}", userId, userName);
        doctorMoveDataService.updateUserName(userId, userName);
        log.info("update user name ending");
        return true;
    }

    @RequestMapping(value = "/deleteFarm", method = RequestMethod.GET)
    public Boolean deleteFarm(@RequestParam Long farmId) {
        log.info("delete farm starting, farmId:{}");
        doctorMoveDataService.deleteFarm(farmId);
        log.info("delete farm ending");
        return true;
    }
    /**
     * 修复之前手动添加的数据有误的断奶
     * @return
     */
    @RequestMapping(value = "/fixAddPigWean", method = RequestMethod.GET)
    public Boolean fixAddPigWean(){
        log.info("fixAddPigWean starting");
        doctorMoveDataService.fixAddPigWean();
        log.info("fixAddPigWean ending");
        return true;
    }

    /**
     * 修复之前有仔猪变动、拼窝触断奶事件
     * @return
     */
    @RequestMapping(value = "/fixTriggerPigWean", method = RequestMethod.GET)
    public Boolean fixTriggerPigWean() {
        log.info("fixTriggerPigWean starting");
        doctorMoveDataService.fixTriggerPigWean();
        log.info("fixTriggerPigWean ending");
        return true;
    }
}
