package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Response;
import java.util.Date;
import java.util.List;

/**
 * 结算服务
 * Created by sunbo@terminus.io on 2018/4/9.
 */
public interface DoctorWarehouseSettlementService {

    public Boolean findByOrgId(Long orgId);


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
     * 获取指定日期的会计年月
     *
     * @param date
     * @return
     */
    public Date getSettlementDate(Date date);

    /**
     * 结算
     *
     * @param orgId          公司id
     * @param farmIds        公司下猪场id列表
     * @param settlementDate 会计年月
     */
    public Response<Boolean> settlement(Long orgId, List<Long> farmIds, Date settlementDate);


    /**
     * 反结算
     *
     * @param orgId          公司id
     * @param farmIds        公司下猪场id列表
     * @param settlementDate 会计年月
     */
    public Response<Boolean> antiSettlement(Long orgId, List<Long> farmIds, Date settlementDate);
}
