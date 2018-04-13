package io.terminus.doctor.event.dao;

import io.terminus.common.model.Paging;
import io.terminus.doctor.event.dto.DoctorPigSalesExportDto;
import io.terminus.doctor.event.model.DoctorGroup;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xjn on 17/12/19.
 * email:xiaojiannan@terminus.io
 */
public class DoctorGroupDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;

    @Test
    public void listOpenGroupsByTest() {
        List<DoctorGroup> groupList = doctorGroupDao.listOpenGroupsBy("2017-01-01");
        Assert.assertEquals(groupList.size(), 1620);
    }

    @Test
    public void testFindFattenSales() {
        Map<String, Object> map = new HashMap<>();
        map.put("farmId", 404);
        map.put("startDate", "2018-04-01");
        List<DoctorPigSalesExportDto> paging = doctorGroupEventDao.findFattenSales(map);
        Assert.assertEquals(paging.size(), 1);
    }
}
