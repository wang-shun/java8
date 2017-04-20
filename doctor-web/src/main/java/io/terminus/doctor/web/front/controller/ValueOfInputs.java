package io.terminus.doctor.web.front.controller;

import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.ValueOfInput;
import io.terminus.doctor.basic.service.DoctorValueOfInputReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Desc: 投入品价值
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/14
 */
@RestController
@RequestMapping("/api/value-of-input")
public class ValueOfInputs {

    @Autowired
    private DoctorValueOfInputReadService doctorValueOfInputReadService;


    /**
     * 根据当前登录用户 获取该用户附加信息
     *
     * @return 用户附加信息
     */
    @RequestMapping(value = "/vaccine/{farmId}", method = RequestMethod.GET)
    public List<ValueOfInput> rankingVaccineValueOfInput(@PathVariable Long farmId) {
        Response<List<ValueOfInput>> resp = doctorValueOfInputReadService.rankingValueOfInput(farmId, 3);
        if(!resp.isSuccess()){
            throw new JsonResponseException(resp.getError());
        }
        return resp.getResult();
    }

    @RequestMapping(value = "/drug/{farmId}", method = RequestMethod.GET)
    public List<ValueOfInput> rankingDrugValueOfInput(@PathVariable Long farmId) {
        Response<List<ValueOfInput>> resp = doctorValueOfInputReadService.rankingValueOfInput(farmId, 4);
        if(!resp.isSuccess()){
            throw new JsonResponseException(resp.getError());
        }
        return resp.getResult();
    }

}


