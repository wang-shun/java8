package io.terminus.doctor.web.front.report;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesReadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/1/3.
 */
@RestController
@RequestMapping("/api/doctor/report/board/{farmId}/")
public class ReportBoardController {

    @RpcConsumer
    private DoctorReportFieldCustomizesReadService doctorReportFieldCustomizesReadService;

    @RequestMapping(method = RequestMethod.GET, value = "daily")
    public Response<List<DoctorReportFieldTypeDto>> dailyBoard(@PathVariable Long farmId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        Response<List<DoctorReportFieldTypeDto>> result = doctorReportFieldCustomizesReadService.getAllWithSelected(farmId);

        return result;
    }


    @RequestMapping(method = RequestMethod.GET, value = "weekly")
    public void weeklyBoard() {

    }

    @RequestMapping(method = RequestMethod.GET, value = "monthly")
    public void monthlyBoard() {

    }

    @RequestMapping(method = RequestMethod.GET, value = "yearly")
    public void yearlyBoard() {

    }

}
