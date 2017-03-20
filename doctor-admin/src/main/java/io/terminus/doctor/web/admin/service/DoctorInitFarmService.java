package io.terminus.doctor.web.admin.service;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorUserReadService;
import io.terminus.parana.common.utils.RespHelper;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 17:38 17/3/20
 */

@Slf4j
@Service
public class DoctorInitFarmService {

    @RpcConsumer
    private DoctorUserReadService doctorUserReadService;

    @RpcConsumer
    private DoctorWareHouseTypeWriteService doctorWareHouseTypeWriteService;


    @RpcConsumer
    private DoctorFarmBasicWriteService doctorFarmBasicWriteService;

    @RpcConsumer
    private DoctorBasicReadService doctorBasicReadService;

    @RpcConsumer
    private DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    public Response<Boolean> initFarm(DoctorFarm farm, Long userId){
        try{
            Response<User> response = doctorUserReadService.findById(userId);
            String userName = RespHelper.orServEx(response).getName();
            //初始化仓库
            doctorWareHouseTypeWriteService.initDoctorWareHouseType(farm.getId(), farm.getName(), userId, userName);
            //初始化基础数据
            DoctorFarmBasic doctorFarmBasic = new DoctorFarmBasic();
            doctorFarmBasic.setFarmId(farm.getId());
            List<DoctorBasic> basicList = RespHelper.orServEx(doctorBasicReadService.findAllBasics());
            List<DoctorChangeReason> reasonList = RespHelper.orServEx(doctorBasicReadService.findAllChangeReasons());
            List<DoctorBasicMaterial> materialList = RespHelper.orServEx(doctorBasicMaterialReadService.findAllBasicMaterials());
            String basicIds = basicList.stream().map(doctorBasic -> String.valueOf(doctorBasic.getId())).collect(Collectors.joining(","));
            String reasonIds = reasonList.stream().map(doctorChangeReason -> String.valueOf(doctorChangeReason.getId())).collect(Collectors.joining(","));
            String materialIds = materialList.stream().map(doctorBasicMaterial -> String.valueOf(doctorBasicMaterial.getId())).collect(Collectors.joining(","));
            doctorFarmBasic.setBasicIds(basicIds);
            doctorFarmBasic.setReasonIds(reasonIds);
            doctorFarmBasic.setMaterialIds(materialIds);
            doctorFarmBasicWriteService.createFarmBasic(doctorFarmBasic);
            return Response.ok();
        }catch(Exception e){
            log.error("init farm failed, farmId: {}, userId:{}", farm.getId(), userId);
            return Response.fail("farm.init.fail");
        }
    }
}
