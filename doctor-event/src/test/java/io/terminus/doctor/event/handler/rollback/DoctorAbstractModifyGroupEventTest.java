package io.terminus.doctor.event.handler.rollback;

import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.dao.BaseDaoTest;
import io.terminus.doctor.event.editHandler.group.DoctorAbstractModifyGroupEventHandler;
import io.terminus.doctor.event.editHandler.group.DoctorModifyGroupCloseEventHandler;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 18/3/2.
 * email:xiaojiannan@terminus.io
 */
public class DoctorAbstractModifyGroupEventTest extends BaseDaoTest{

    @Autowired
    private DoctorModifyGroupCloseEventHandler handler;


    @Test
    public void validGroupLiveStockForNew() {
        handler.validGroupLiveStock(28882L, "", DateUtil.toDate("2018-02-28"), -4);
    }

    @Test
    public void validGroupLiveStockForModify() {
        //日期不变
        handler.validGroupLiveStock(28882L, "", 1199551L, DateUtil.toDate("2018-03-01"),DateUtil.toDate("2018-03-01"), 80, -94, -4);
        //编辑日期
//        handler.validGroupLiveStock(28882L, "", 1199551L, DateUtil.toDate("2018-03-02"),DateUtil.toDate("2018-02-28"), 90, -29, -4);
    }

}
