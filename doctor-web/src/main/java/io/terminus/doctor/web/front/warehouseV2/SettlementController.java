package io.terminus.doctor.web.front.warehouseV2;

import javafx.geometry.Pos;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * 结算服务
 * Created by sunbo@terminus.io on 2018/4/10.
 */
@RestController
@RequestMapping("api/doctor/warehouse/settlement")
public class SettlementController {


    /**
     * 结算
     *
     * @param orgId          公司id
     * @param settlementDate 需要结算的会计年月
     */
    @RequestMapping(method = RequestMethod.POST)
    public void settlement(@RequestParam Long orgId, @RequestParam Date settlementDate) {


    }


    /**
     * 反结算
     *
     * @param orgId          公司id
     * @param settlementDate 需要反结算的会计年月
     */
    @RequestMapping(method = RequestMethod.POST, value = "anti")
    public void AntiSettlement(@RequestParam Long orgId, @RequestParam Date settlementDate) {

    }
}
