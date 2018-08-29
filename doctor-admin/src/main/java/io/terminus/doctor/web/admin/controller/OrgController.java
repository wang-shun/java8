package io.terminus.doctor.web.admin.controller;


import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorOrgsLogs;
import io.terminus.doctor.user.service.DoctorOrgWriteService;
import io.terminus.doctor.user.service.DoctorOrgsLogsWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("api/doctor/admin/orgsName")
public class OrgController {

    @RpcConsumer
    private DoctorOrgWriteService doctorOrgWriteService;// 公司表

    @RpcConsumer
    private DoctorOrgsLogsWriteService doctorOrgsLogsWriteService;//修改公司名称日志表

    /**
     * 更新 公司的名称
     * @param
     * @return
     */
    @RequestMapping(value = "/name", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateDoctorOrg(@RequestParam long id,@RequestParam String name,@RequestParam Integer type) {

        Response<DoctorOrg> doctorOrg = doctorOrgWriteService.findName(id);
//        System.out.println("前"+id+doctorOrg.getResult().getName());

        DoctorOrgsLogs doctorOrgsLogs=new DoctorOrgsLogs();
        doctorOrgsLogs.setOrgId(id);
        doctorOrgsLogs.setOrgFrotName(doctorOrg.getResult().getName());
        doctorOrgsLogs.setOrgLaterName(name);
        doctorOrgsLogs.setCreatorId(1L);
//        doctorOrgsLogs.setCreatorName("jsj");
        doctorOrgsLogs.setUpdatorId(1L);
//        doctorOrgsLogs.setUpdatorName("jsj");

        doctorOrgsLogsWriteService.createLog(doctorOrgsLogs);
//        System.out.println("后"+id+name);

        doctorOrgWriteService.updateOrgName(id,name,type);
        doctorOrgWriteService.updateBarnName(id, name);
        doctorOrgWriteService.updateFarmName(id,name);
        doctorOrgWriteService.updateGroupName(id,name);
        doctorOrgWriteService.updateGroupEventName(id,name);
        doctorOrgWriteService.updatePigEventsName(id,name);
        doctorOrgWriteService.updatePigScoreApplyName(id,name);
        doctorOrgWriteService.updatePigName(id,name);
        doctorOrgWriteService.updateGroupDaileName(id,name);
        doctorOrgWriteService.updatePigDailieName(id,name);
//        if (!response.isSuccess()) {
//            throw new JsonResponseException(500, response.getError());
//        }
        return true;
    }


}
