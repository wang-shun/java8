package io.terminus.doctor.web.front.report;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2018/1/3.
 */
@RestController
@RequestMapping("/api/doctor/report/")
public class ReportController {



    @RequestMapping(method = RequestMethod.GET, value = "board")
    public void dailyBoard(@RequestParam Long farmId, @RequestParam Date date) {

    }




}
