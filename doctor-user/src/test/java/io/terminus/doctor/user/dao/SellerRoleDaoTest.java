package io.terminus.doctor.user.dao;

import com.google.common.collect.Lists;
import io.terminus.common.model.Paging;
import io.terminus.doctor.user.model.SellerRole;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by cuiwentao on 16/3/7.
 */
public class SellerRoleDaoTest extends BaseDaoTest  {

    @Autowired
    private SellerRoleDao sellerRoleDao;

    /**
     * create a new SellerRole
     *
     * @param id
     * @return
     */
    private SellerRole createOne(Long id) {
        //SellerRole
        SellerRole sellerRole = new SellerRole();
        sellerRole.setId(id);
        sellerRole.setCreatedAt(new Date());
        sellerRole.setUpdatedAt(new Date());
        sellerRole.setName("name");
        sellerRole.setDesc("desc");
        sellerRole.setShopId(id);
        sellerRole.setStatus(1);
        sellerRole.setExtraJson("{\"bussinessId\":1}");
        sellerRole.setAllowJson("[{\"scope\":\"name\"}]");

        return sellerRole;
    }

    private List<Long> createSellerRole() {
        List<Long> ids = Lists.newArrayList();
        for (int i = 0;i < 5; i++) {
            SellerRole model = createOne((long) i);
            sellerRoleDao.create(model);
            ids.add(model.getId());
        }
        return ids;
    }

    @Test
    public void testFindById(){
        List<Long> createdIds = createSellerRole();
        SellerRole model = sellerRoleDao.findById(createdIds.get(3));
        Assert.assertNotNull(model.getId());
    }

    @Test
    public void testUpdate(){
        SellerRole model = createOne(1L);
        model.setName("binjiang");
        Boolean result = sellerRoleDao.update(model);
        Assert.assertEquals(model.getName(), "binjiang");
    }

    @Test
    public void testDelet(){
        List<Long> createdIds = createSellerRole();
        Assert.assertTrue(sellerRoleDao.delete(createdIds.get(1)));
    }

    @Test
    public void testFindByIds() {
        List<Long> createdIds = createSellerRole();
        List<SellerRole> result = sellerRoleDao.findByIds(createdIds);
        Assert.assertTrue(!result.isEmpty());
        Assert.assertEquals(createdIds.size(), result.size());
    }

    @Test
    public void testPaging() {
        List<Long> createdIds = createSellerRole();
        Paging<SellerRole> result = sellerRoleDao.paging(0, 5);
        Assert.assertTrue(!result.isEmpty());
    }

    @Test
    public void testFindByShopId() {
        List<Long> createdIds = createSellerRole();
        List<SellerRole> result = sellerRoleDao.findByShopId(createdIds.get(1));
        Assert.assertNotNull(result);
    }


}
