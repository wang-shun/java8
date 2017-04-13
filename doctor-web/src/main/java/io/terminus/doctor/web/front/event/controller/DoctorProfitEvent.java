package io.terminus.doctor.web.front.event.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;
import io.terminus.doctor.event.service.DoctorProfitMaterOrPigReadServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by terminus on 2017/4/13.
 */

@Slf4j
@RestController
@RequestMapping("/api/doctor/events/profit")
public class DoctorProfitEvent {


    @RpcConsumer
    private DoctorProfitMaterOrPigReadServer doctorParityMonthlyReportReadService;

//    @Autowired
//    public DoctorProfitEvent(DoctorProfitMaterOrPigReadServer doctorParityMonthlyReportReadService) {
//        this.doctorParityMonthlyReportReadService = doctorParityMonthlyReportReadService;
//    }

    @RequestMapping(value = "/materOrPig", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorProfitMaterialOrPig> getMaterOrPig(@RequestParam Map<String, Object> map, @RequestParam Long farmId){
        return RespHelper.or500(doctorParityMonthlyReportReadService.findProfitMaterialOrPig(farmId , map));
    }
}
