package io.terminus.doctor.basic.service.warehouseV2;

import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2018/4/9.
 */
public interface DoctorWarehouseAccountingService {


    public boolean isAccounting(Long orgId);


    public void accounting(Long orgId, Date accountDate);
}
