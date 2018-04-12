package io.terminus.doctor.basic.service.warehouseV2;

import org.joda.time.DateTime;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * 结算服务
 * Created by sunbo@terminus.io on 2018/4/9.
 */
public interface DoctorWarehouseSettlementService {


    /**
     * 是否结算中
     *
     * @param orgId 公司id
     * @return
     */
    public boolean isUnderSettlement(Long orgId);


    /**
     * 指定公司在指定会计年月中是否已结算
     *
     * @param orgId          公司id
     * @param settlementDate 会计年月
     * @return
     */
    public boolean isSettled(Long orgId, Date settlementDate);

    /**
     * 结算
     *
     * @param orgId          公司id
     * @param settlementDate 会计年月
     */
    public void settlement(Long orgId, List<Long> farmIds, DateTime settlementDate);


    /**
     * 反结算
     *
     * @param orgId          公司id
     * @param settlementDate 会计年月
     */
    public void antiSettlement(Long orgId, Date settlementDate);
}
