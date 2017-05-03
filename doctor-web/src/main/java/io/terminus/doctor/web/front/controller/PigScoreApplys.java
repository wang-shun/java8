package io.terminus.doctor.web.front.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorUser;
import io.terminus.doctor.user.model.PigScoreApply;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.PigScoreApplyReadService;
import io.terminus.doctor.user.service.PigScoreApplyWriteService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * Desc: 
 * Mail: hehaiyang@terminus.io
 * Date: 2017/05/02
 */
@Slf4j
@RestController
@RequestMapping("/api/pig-score/apply")
public class PigScoreApplys {

    @RpcConsumer
    private PigScoreApplyWriteService pigScoreApplyWriteService;
    @RpcConsumer
    private PigScoreApplyReadService pigScoreApplyReadService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    /**
     * 创建申请
     * @param
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createPigScoreApply(HttpServletRequest request) {

        DoctorUser loginUser = UserUtil.getCurrentUser();

        PigScoreApply apply = new PigScoreApply();

        // 先获取 orgId 和 farmId
        if(loginUser.getFarmId() != null){
            apply.setFarmId(loginUser.getFarmId());
        }else{
            throw new JsonResponseException(500, "farm.id.not.exist");
        }
        if(loginUser.getOrgId() != null){
            apply.setOrgId(loginUser.getOrgId());
        }else{
            throw new JsonResponseException(500, "org.id.not.exist");
        }

        apply.setUserId(loginUser.getId());
        apply.setUserName(loginUser.getName());
        apply.setStatus(0);

        // 查询猪场详情
        Response<DoctorFarm> farmResp = doctorFarmReadService.findFarmById(apply.getFarmId());
        if (!farmResp.isSuccess()) {
            throw new JsonResponseException(500, farmResp.getError());
        }
        if (farmResp.getResult() == null) {
            throw new JsonResponseException(500, "farm.not.exist");
        }
        apply.setOrgName(farmResp.getResult().getOrgName());
        apply.setFarmName(farmResp.getResult().getName());

        // 判断是否存在申请记录
        Response<PigScoreApply> applyResp = pigScoreApplyReadService.findByFarmIdAndUserId(apply.getFarmId(), apply.getUserId());
        if (!applyResp.isSuccess()) {
            throw new JsonResponseException(500, applyResp.getError());
        }
        if(applyResp.getResult() != null){
            apply = applyResp.getResult();
            apply.setStatus(0);
            apply.setRemark("");
            Response<Boolean> response = pigScoreApplyWriteService.update(apply);
            if (!response.isSuccess()) {
                throw new JsonResponseException(500, response.getError());
            }
            return applyResp.getResult().getId();
        }else {
            Response<Long> response = pigScoreApplyWriteService.create(apply);
            if (!response.isSuccess()) {
                throw new JsonResponseException(500, response.getError());
            }
            return response.getResult();
        }
    }
}