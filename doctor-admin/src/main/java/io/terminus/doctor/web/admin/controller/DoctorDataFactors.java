package io.terminus.doctor.web.admin.controller;

import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorDataFactor;
import io.terminus.doctor.event.service.DoctorDataFactorReadService;
import io.terminus.doctor.event.service.DoctorDataFactorWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Desc: 信用模型计算因子 API
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/12
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/data-factor")
public class DoctorDataFactors {

    @RpcConsumer
    private DoctorDataFactorWriteService doctorDataFactorWriteService;

    @RpcConsumer
    private DoctorDataFactorReadService doctorDataFactorReadService;

    /**
     * 查询详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDataFactor findDoctorDataFactor(@PathVariable Long id) {
        Response<DoctorDataFactor> response =  doctorDataFactorReadService.findById(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 分页
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Paging<DoctorDataFactor> pagingDoctorDataFactor(@RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                 @RequestParam(value = "pageSize", required = false) Integer pageSize) {

        Map<String, Object> criteria = Maps.newHashMap();

        Response<Paging<DoctorDataFactor>> result =  doctorDataFactorReadService.paging(pageNo, pageSize, criteria);
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

    /**
     * 创建
     * @param
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createDoctorDataFactor(@RequestBody DoctorDataFactor factor) {
        if(factor.getFactor() == null){
            throw new JsonResponseException(500,"doctor.data.factor.invalid");
        }
        rangeVerify(factor);
        factor.setIsDelete(0);
        Response<Long> response = doctorDataFactorWriteService.create(factor);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 更新
     * @param
     */
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateDoctorDataFactor(@RequestBody DoctorDataFactor factor) {
        if(factor.getFactor() == null){
            throw new JsonResponseException(500,"doctor.data.factor.invalid");
        }
        rangeVerify(factor);
        Response<Boolean> response = doctorDataFactorWriteService.update(factor);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    private void rangeVerify(DoctorDataFactor factor){
        if(factor.getRangeFrom() == null && factor.getRangeTo() == null){
            factor.setRangeFrom(factor.getFactor());
            factor.setRangeTo(factor.getRangeTo());
        }else if(factor.getRangeFrom() == null){
            factor.setRangeFrom(Double.MIN_VALUE);
        }else if(factor.getRangeTo() == null){
            factor.setRangeFrom(Double.MAX_VALUE);
        }
    }

    /**
     * 逻辑删除
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean deleteDoctorDataFactor(@PathVariable Long id) {
        DoctorDataFactor factor = new DoctorDataFactor();
        factor.setId(id);
        factor.setIsDelete(1);
        Response<Boolean> response = doctorDataFactorWriteService.update(factor);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }
}