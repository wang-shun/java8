package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.service.DoctorWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorWareHouseWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final DoctorWareHouseWriteService doctorWareHouseWriteService;

    @Autowired
    public DoctorWareHouseQuery(DoctorWareHouseReadService doctorWareHouseReadService,
                                DoctorWareHouseWriteService doctorWareHouseWriteService){
        this.doctorWareHouseReadService = doctorWareHouseReadService;
        this.doctorWareHouseWriteService = doctorWareHouseWriteService;
    }

    @RequestMapping(value = "/listWareHouseType", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorFarmWareHouseType> pagingDoctorWareHouseType(@RequestParam("farmId") Long farmId){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorFarmWareHouseType(farmId));
    }

    @RequestMapping(value = "/pagingDoctorWareHouseDto", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorWareHouseDto> pagingDoctorWareHouseDto(@RequestParam("farmId") Long farmId,
                                                               @RequestParam(value = "type", required = false) Integer type,
                                                               @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                               @RequestParam(value = "pageSize", required = false) Integer pageSize){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorWarehouseDto(farmId, type, pageNo, pageSize));
    }

    @RequestMapping(value = "/createWareHouse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createWareHouseInfo(@RequestBody DoctorWareHouseDto doctorWareHouseDto){
        DoctorWareHouse doctorWareHouse = null;
        try{
            // TODO convert farm info 信息
            doctorWareHouse = new DoctorWareHouse();
        }catch (Exception e){
            log.error("create ware house info fail, cause:{}", Throwables.getStackTraceAsString(e));
        }
        return RespHelper.or500(doctorWareHouseWriteService.createWareHouse(doctorWareHouse));
    }
}
