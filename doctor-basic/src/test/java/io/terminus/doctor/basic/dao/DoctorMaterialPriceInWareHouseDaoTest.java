package io.terminus.doctor.basic.dao;

import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 12:40 17/1/11
 */

public class DoctorMaterialPriceInWareHouseDaoTest extends BaseDaoTest{

    @Autowired
    private DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao;

    @Test
    public void testCurrentStockInfo(){
        Map<String, Object> map = new HashMap<>();
        map = doctorMaterialPriceInWareHouseDao.currentStockInfo(1L, 5L, 5);
        Assert.assertEquals("157.000", Objects.toString(map.get("COUNT")));
    }

    @Test
    public void testStockAmount(){
        Map<Long, Double> map = new HashMap<>();
        map = doctorMaterialPriceInWareHouseDao.stockAmount(1L, 5L, WareHouseType.from(5));
        Assert.assertEquals("109900.0", Objects.toString(map.get(5L)));
    }


    @Test
    public void  test(){
        System.out.println("==========================" + DateTime.now().withDayOfMonth(1).toString(DateUtil.DATE));
        System.out.println("==========================" + DateTime.now().withDayOfMonth(1).plusMonths(1).toString(DateUtil.DATE));
    }
}
