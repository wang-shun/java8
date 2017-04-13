package io.terminus.doctor.web.front.event.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;
import io.terminus.doctor.event.service.DoctorProfitMaterOrPigReadServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * Created by terminus on 2017/4/13.
 */

@Slf4j
@RestController
@RequestMapping("/api/doctor/events/profit")
public class DoctorProfitEvent {


    @RpcConsumer
    private DoctorProfitMaterOrPigReadServer doctorParityMonthlyReportReadService;
    
    @RequestMapping(value = "/materOrPig", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorProfitMaterialOrPig> getMaterOrPig(@RequestParam Map<String, Object> map, @RequestParam String date, @RequestParam Long farmId){
        map = Params.filterNullOrEmpty(map);
        Date startDate = DateUtil.toYYYYMM(date);
        map.put("date", startDate);
        return RespHelper.or500(doctorParityMonthlyReportReadService.findProfitMaterialOrPig(farmId , map));
    }
}
