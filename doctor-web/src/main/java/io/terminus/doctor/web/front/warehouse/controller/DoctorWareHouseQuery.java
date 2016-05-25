package io.terminus.doctor.web.front.warehouse.controller;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.service.DoctorWareHouseReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 猪场信息获取查询
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/warehouse/query")
public class DoctorWareHouseQuery {

    private final DoctorWareHouseReadService doctorWareHouseReadService;

    @Autowired
    public DoctorWareHouseQuery(DoctorWareHouseReadService doctorWareHouseReadService){
        this.doctorWareHouseReadService = doctorWareHouseReadService;
    }

    @RequestMapping(value = "/listWareHouseType", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorFarmWareHouseType> pagingDoctorWareHouseType(@RequestParam("farmId") Long farmId){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorFarmWareHouseType(farmId));
    }
}
