package io.terminus.doctor.basic.service.warehouseV2;

import java.util.Date;

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
     * 结算
     *
     * @param orgId          公司id
     * @param settlementDate 会计年月
     */
    public void settlement(Long orgId, Date settlementDate);


    /**
     * 反结算
     *
     * @param orgId          公司id
     * @param settlementDate 会计年月
     */
    public void antiSettlement(Long orgId, Date settlementDate);
}
