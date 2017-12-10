package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.common.utils.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.sql.SQLSyntaxErrorException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by sunbo@terminus.io on 2017/12/10.
 */
@Sql("classpath:stock-monthly-statistics.sql")
public class DoctorWarehouseStockMonthlyDaoTest extends BaseDaoTest {

    @Autowired
    private DoctorWarehouseStockMonthlyDao doctorWarehouseStockMonthlyDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testMonthly() {

        AmountAndQuantityDto amountAndQuantityDto = doctorWarehouseStockMonthlyDao.statistics(1L, DateUtil.toYYYYMM("2017-04"));
        Assert.assertEquals(1006, amountAndQuantityDto.getQuantity().doubleValue(), 2);
        Assert.assertEquals(1406, amountAndQuantityDto.getAmount());
    }

    @Test
    public void test() {
        int year = 2017;
        int month = 4;
        Date d=DateUtil.toYYYYMM(year + "-" + month);
        System.out.println(DateUtil.toDateString(d));
    }

}
