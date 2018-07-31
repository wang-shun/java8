package io.terminus.doctor.web.front.report;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by sunbo@terminus.io on 2017/12/13.
 */
@RestController("pigReportController")
@RequestMapping("/api/doctor/report/v2/")
public class ReportController {


    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @RequestMapping(method = RequestMethod.GET, value = "{type}/daily")
    public void daily(@PathVariable String type) {

        if ("farm".equals(type)) {

        } else if ("company".equals(type)) {

        } else if ("group".equals(type)) {

        } else {

        }


    }


}
