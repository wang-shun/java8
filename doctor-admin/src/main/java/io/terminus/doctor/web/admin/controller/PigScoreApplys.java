package io.terminus.doctor.web.admin.controller;

import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.PigScoreApply;
import io.terminus.doctor.user.service.PigScoreApplyReadService;
import io.terminus.doctor.user.service.PigScoreApplyWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Desc: 猪场评分功能申请
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

    /**
     * 申请详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public PigScoreApply findPigScoreApply(@PathVariable Long id) {
        Response<PigScoreApply> response =  pigScoreApplyReadService.findById(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 审核列表
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<PigScoreApply> pagingPigScoreApply(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                 @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        Map<String, Object> criteria = Maps.newHashMap();

        Response<Paging<PigScoreApply>> result =  pigScoreApplyReadService.paging(pageNo, pageSize, criteria);
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

    /**
     * 审核申请
     * @param id 申请ID
     * @param status 审核状态
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean checkApply(@RequestParam Long id,
                              @RequestParam Integer status,
                              @RequestParam(value = "remark", required = false) String remark) {
        PigScoreApply apply = new PigScoreApply();
        apply.setId(id);
        apply.setStatus(status);
        Response<Boolean> response = pigScoreApplyWriteService.update(apply);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }
}