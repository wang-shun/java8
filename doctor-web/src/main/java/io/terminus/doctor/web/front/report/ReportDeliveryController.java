package io.terminus.doctor.web.front.report;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.event.service.DoctorDeliveryReadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor/report/")
public class ReportDeliveryController {
    @RpcConsumer
    private DoctorDeliveryReadService doctorDeliveryReadService;

    @RequestMapping(method = RequestMethod.GET, value = "delivery")
    public Map<String,Object> deliveryReport(@RequestParam(required = true) Long farmId,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date beginDate,
                                                   @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                   @RequestParam(required = false) String pigCode,
                                                   @RequestParam(required = false) String operatorName,
                                                   @RequestParam(required = false) int isdelivery) {
        return doctorDeliveryReadService.getMating(farmId,beginDate,endDate,pigCode,operatorName,isdelivery);
    }
}