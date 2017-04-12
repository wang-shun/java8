package io.terminus.doctor.web.admin.user;

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
@RequestMapping
public class DoctorDataFactors {

    @RpcConsumer
    private DoctorDataFactorWriteService doctorDataFactorWriteService;

    @RpcConsumer
    private DoctorDataFactorReadService doctorDataFactorReadService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDataFactor findDoctorDataFactor(@PathVariable Long id) {
        Response<DoctorDataFactor> response =  doctorDataFactorReadService.findById(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

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
    public Long createDoctorDataFactor(@RequestBody DoctorDataFactor doctorDataFactor) {
        Response<Long> response = doctorDataFactorWriteService.create(doctorDataFactor);
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
    public Boolean updateDoctorDataFactor(@RequestBody DoctorDataFactor doctorDataFactor) {
        Response<Boolean> response = doctorDataFactorWriteService.update(doctorDataFactor);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean deleteDoctorDataFactor(@PathVariable Long id) {
        Response<Boolean> response = doctorDataFactorWriteService.delete(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }
}