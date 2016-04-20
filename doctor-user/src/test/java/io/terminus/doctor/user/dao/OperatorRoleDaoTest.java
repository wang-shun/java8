package io.terminus.doctor.user.dao;

import com.google.common.collect.Lists;
import io.terminus.common.model.Paging;
import io.terminus.doctor.user.model.OperatorRole;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class OperatorRoleDaoTest extends BaseDaoTest  {

    @Autowired
    private OperatorRoleDao operatorRoleDao;

    /**
     * create a new OperatorRole
     *
     * @param id
     * @return
     */
    private OperatorRole createOne(Long id) {
        //OperatorRole
        OperatorRole OperatorRole = new OperatorRole();
        OperatorRole.setId(id);
        OperatorRole.setCreatedAt(new Date());
        OperatorRole.setUpdatedAt(new Date());
        OperatorRole.setName("name");
        OperatorRole.setDesc("desc");
        OperatorRole.setStatus(1);
        OperatorRole.setExtraJson("{\"businessId\":1}");
        OperatorRole.setAllowJson("[{\"scope\":\"name\"}]");

        return OperatorRole;
    }

    private List<Long> createOperatorRole() {
        List<Long> ids = Lists.newArrayList();
        for (int i = 0;i < 5; i++) {
            OperatorRole model = createOne((long) i);
            operatorRoleDao.create(model);
            ids.add(model.getId());
        }
        return ids;
    }

    @Test
    public void testFindById(){
        List<Long> createdIds = createOperatorRole();
        OperatorRole model = operatorRoleDao.findById(createdIds.get(3));
        Assert.assertNotNull(model.getId());
    }

    @Test
    public void testUpdate(){
        OperatorRole model = createOne(1L);
        model.setName("binjiang");
        Boolean result = operatorRoleDao.update(model);
        Assert.assertEquals(model.getName(), "binjiang");
    }

    @Test
    public void testDelet(){
        List<Long> createdIds = createOperatorRole();
        Assert.assertTrue(operatorRoleDao.delete(createdIds.get(1)));
    }

    @Test
    public void testFindByIds() {
        List<Long> createdIds = createOperatorRole();
        List<OperatorRole> result = operatorRoleDao.findByIds(createdIds);
        Assert.assertTrue(!result.isEmpty());
        Assert.assertEquals(createdIds.size(), result.size());
    }

    @Test
    public void testPaging() {
        List<Long> createdIds = createOperatorRole();
        Paging<OperatorRole> result = operatorRoleDao.paging(0, 5);
        Assert.assertTrue(!result.isEmpty());
    }

    @Test
    public void testFindByStatus() {
        List<Long> createdIds = createOperatorRole();
        List<OperatorRole> result = operatorRoleDao.findByStatus(1);
        Assert.assertNotNull(result);
    }


}
