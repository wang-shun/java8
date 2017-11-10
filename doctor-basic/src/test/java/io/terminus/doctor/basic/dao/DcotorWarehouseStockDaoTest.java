package io.terminus.doctor.basic.dao;

import io.terminus.doctor.common.utils.RespHelper;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/11/10.
 */
public class DcotorWarehouseStockDaoTest extends BaseDaoTest {

    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;

    /**
     * 验证有效库存
     */
    @Test
    public void testOnQueryEffectiveStock() {
        Map<String, Object> params = new HashMap<>();
        params.put("effective", "true");
        Assert.assertEquals(1, doctorWarehouseStockDao.advList(params).size());
    }

    @Test
    public void testOnQueryStock(){
        Assert.assertEquals(2, doctorWarehouseStockDao.advList(Collections.emptyMap()).size());
    }
}
