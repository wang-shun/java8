package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import io.terminus.doctor.move.service.DoctorMoveBasicService;
import io.terminus.doctor.move.service.DoctorMoveDataService;
import io.terminus.doctor.move.service.UserInitService;
import io.terminus.doctor.move.service.WareHouseInitService;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final DoctorFarmDao doctorFarmDao;

    @Autowired
    public DoctorMoveDataController(UserInitService userInitService,
                                    WareHouseInitService wareHouseInitService, 
                                    DoctorMoveBasicService doctorMoveBasicService,
                                    DoctorMoveDataService doctorMoveDataService, 
                                    DoctorFarmDao doctorFarmDao) {
        this.userInitService = userInitService;
        this.wareHouseInitService = wareHouseInitService;
        this.doctorMoveBasicService = doctorMoveBasicService;
        this.doctorMoveDataService = doctorMoveDataService;
        this.doctorFarmDao = doctorFarmDao;
    }

    /**
     * 测试数据源连接是否正常
     * @param moveId 数据源id
     * @return 是否正常
     */
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
        //1.迁移猪场信息

        //2.迁移基础数据(Basic, Customer, ChangeReason, Barn)
        
        //3.迁移仓库/物料

        //4.迁移公猪母猪

        //5.迁移猪群

        return Boolean.TRUE;
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
            log.warn("move basci end");
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

    public Boolean movePig(@RequestParam("moveId") Long moveId, @RequestParam("farmId") Long farmId) {
        try {
            DoctorFarm farm = doctorFarmDao.findById(farmId);
            log.warn("move pig start, moveId:{}", moveId);
            doctorMoveDataService.movePig(moveId, farm);
            log.warn("move pig end");
            return true;
        } catch (Exception e) {
            // TODO: 16/8/4 失败了 删除之 
            log.error("move pig failed, moveId:{}, farmId:{}, cause:{}",
                    moveId, farmId, Throwables.getStackTraceAsString(e));
            return false;
        }
    }
}
