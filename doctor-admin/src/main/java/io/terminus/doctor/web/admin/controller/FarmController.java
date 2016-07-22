package io.terminus.doctor.web.admin.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorFarmWriteService;
import io.terminus.doctor.user.service.DoctorServiceStatusReadService;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.doctor.warehouse.service.DoctorWareHouseTypeWriteService;
import io.terminus.doctor.web.admin.dto.UserApplyServiceDetailDto;
import io.terminus.doctor.web.admin.service.DoctorInitBarnService;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by chenzenghui on 16/7/15.
 */

@Slf4j
@RestController
@RequestMapping("/api/admin/farm")
public class FarmController {
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorFarmWriteService doctorFarmWriteService;
    private final DoctorUserReadService doctorUserReadService;
    private final DoctorServiceStatusReadService doctorServiceStatusReadService;
    private final DoctorInitBarnService doctorInitBarnService;

    @RpcConsumer
    private DoctorWareHouseTypeWriteService doctorWareHouseTypeWriteService;

    @Autowired
    public FarmController(DoctorFarmReadService doctorFarmReadService,
                          DoctorUserReadService doctorUserReadService,
                          DoctorServiceStatusReadService doctorServiceStatusReadService,
                          DoctorFarmWriteService doctorFarmWriteService,
                          DoctorInitBarnService doctorInitBarnService){
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorUserReadService = doctorUserReadService;
        this.doctorServiceStatusReadService = doctorServiceStatusReadService;
        this.doctorFarmWriteService = doctorFarmWriteService;
        this.doctorInitBarnService = doctorInitBarnService;
    }

    /**
     * 运营人员给猪场主账户添加猪场, 仅在相应用户已开通猪场软件后才能用
     * @param dto userId:主账号的用户id , farms: 猪场,其中的名称(name字段)必填
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Boolean addFarm(@RequestBody UserApplyServiceDetailDto dto){
        //先检查下参数
        if(dto.getUserId() == null){
            throw new JsonResponseException(500, "user.id.invalid");
        }
        if(dto.getFarms() == null || dto.getFarms().isEmpty()){
            throw new JsonResponseException(500, "need.at.least.one.farm"); //需要至少一个猪场信息
        }

        //检查当前登录用户权限
        checkUserTypeOperator();

        //检查参数中的用户是否为主账号
        User primaryUser = RespHelper.or500(doctorUserReadService.findById(dto.getUserId()));
        if(!Objects.equals(primaryUser.getType(), UserType.FARM_ADMIN_PRIMARY.value())){
            throw new JsonResponseException("user.not.primary"); // 该用户不是猪场主账号
        }

        //检查参数中的用户是否已开通猪场软件
        DoctorServiceStatus serviceStatus = RespHelper.or500(doctorServiceStatusReadService.findByUserId(primaryUser.getId()));
        if(!Objects.equals(serviceStatus.getPigdoctorStatus(), DoctorServiceStatus.Status.OPENED.value())){
            throw new JsonResponseException("user.service.pigdoctor.not.opened"); //用户尚未开通猪场软件服务
        }

        //查出原先的猪场
        List<DoctorFarm> farms = RespHelper.or500(doctorFarmReadService.findFarmsByUserId(primaryUser.getId()));

        //检查猪场名称是否重复
        Set<String> dbFarmName = farms.stream().map(DoctorFarm::getName).collect(Collectors.toSet());
        Set<String> addFarmName = dto.getFarms().stream().map(farm -> {
            if (farm.getName() == null || farm.getName().trim().isEmpty()) {
                throw new JsonResponseException("farm.name.not.null"); //猪场名称不可为空
            }
            if (dbFarmName.contains(farm.getName())) {
                throw new JsonResponseException("farm.name.duplicate"); //猪场名称重复
            }
            return farm.getName();
        }).collect(Collectors.toSet());
        if(addFarmName.size() < dto.getFarms().size()){
            throw new JsonResponseException("farm.name.duplicate"); //猪场名称重复
        }

        //终于可以添加猪场了...
        List<DoctorFarm> newFarms = RespHelper.or500(doctorFarmWriteService.addFarms4PrimaryUser(primaryUser.getId(), dto.getFarms()));

        newFarms.forEach(f -> doctorWareHouseTypeWriteService.initDoctorWareHouseType(f.getId(), f.getName(), dto.getUserId(), "initUser"));

        log.info("init barn start, userId:{}, farms:{}", dto.getUserId(), newFarms);

        //初始化猪舍
        newFarms.forEach(farm -> doctorInitBarnService.initBarns(farm, dto.getUserId()));

        log.info("init barn end");
        return Boolean.TRUE;
    }

    /**
     * 检查当前用户是否为运营人员, 若不是将抛出无权限异常
     * @return
     */
    private BaseUser checkUserTypeOperator(){
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(!Objects.equals(UserType.ADMIN.value(), baseUser.getType())){
            throw new JsonResponseException("authorize.fail");
        }
        return baseUser;
    }
}
