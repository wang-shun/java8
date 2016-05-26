package io.terminus.doctor.user.dao;

import com.google.common.collect.Lists;
import io.terminus.common.model.Paging;
import io.terminus.doctor.user.model.SellerRole;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by cuiwentao on 16/3/7.
 */
public class SellerRoleDaoTest extends BaseDaoTest {

    @Autowired
    private SellerRoleDao sellerRoleDao;

    private SellerRole mock(String appKey, Long shopId, Integer status) {
        //SellerRole
        SellerRole role = new SellerRole();
        role.setName("name");
        role.setDesc("desc");
        role.setShopId(shopId);
        role.setAppKey(appKey);
        role.setStatus(status);
        role.setAllowJson("[\"k1\",\"k2\"]");
        role.setExtraJson("{\"key\":1}");
        return role;
    }

    @Test
    public void testFindById() {
        SellerRole role = mock("key1", 1L, 1);
        sellerRoleDao.create(role);

        SellerRole actual = sellerRoleDao.findById(role.getId());
        assertNotNull(actual);

        SellerRole role2 = mock("key2", 1L, 2);
        sellerRoleDao.create(role2);

        List<SellerRole> founds = sellerRoleDao.findByIds(Lists.newArrayList(role.getId(), role2.getId()));
        assertEquals(2, founds.size());
        List<Long> actualIds = Lists.newArrayList(founds.get(0).getId(), founds.get(1).getId());
        assertThat(actualIds, containsInAnyOrder(role2.getId(), role.getId()));
    }

    @Test
    public void testUpdate() {
        SellerRole role = mock("toUpdate", 1L, 1);
        sellerRoleDao.create(role);

        SellerRole toUpdate = new SellerRole();
        toUpdate.setId(role.getId());
        toUpdate.setName("_name");
        toUpdate.setDesc("_desc");
        toUpdate.setShopId(2L);
        toUpdate.setAppKey("_app_key");
        toUpdate.setAllowJson("[\"_changed\"]");
        toUpdate.setExtraJson("{\"changed\":1}");

        sellerRoleDao.update(toUpdate);

        SellerRole actual = sellerRoleDao.findById(role.getId());
        assertEquals(toUpdate.getName(), actual.getName());
        assertEquals(toUpdate.getDesc(), actual.getDesc());
        assertEquals(toUpdate.getShopId(), actual.getShopId());
        assertEquals(toUpdate.getAppKey(), actual.getAppKey());
        assertEquals(toUpdate.getAllowJson(), actual.getAllowJson());
        assertEquals(toUpdate.getExtraJson(), actual.getExtraJson());
    }

    @Test
    public void testDelete() {
        SellerRole role = mock("key1", 2L, 1);
        sellerRoleDao.create(role);
        assertNotNull(sellerRoleDao.findById(role.getId()));

        sellerRoleDao.delete(role.getId());
        assertNull(sellerRoleDao.findById(role.getId()));
    }

    @Test
    public void testPaging() {
        List<SellerRole> roles = Lists.newArrayList(
                mock("key1", 1L, 1),
                mock("key2", 1L, 1),
                mock("key1", 1L, 2),
                mock("key2", 1L, 2),
                mock("key1", 1L, 1),
                mock("key1", 2L, 1)
        );
        for (SellerRole role : roles) {
            sellerRoleDao.create(role);
        }
        SellerRole criteria = new SellerRole();
        criteria.setAppKey("key1");
        criteria.setShopId(1L);
        Paging<SellerRole> result = sellerRoleDao.paging(0, 10, criteria);
        assertThat(result.getTotal(), is(3L));
        List<Long> ids = Lists.newArrayList();
        for (SellerRole o : result.getData()) {
            ids.add(o.getId());
        }
        assertThat(ids, containsInAnyOrder(roles.get(0).getId(), roles.get(2).getId(), roles.get(4).getId()));


        criteria.setStatus(1);
        result = sellerRoleDao.paging(0, 10, criteria);
        assertThat(result.getTotal(), is(2L));
        assertThat(
                Lists.newArrayList(result.getData().get(0).getId(), result.getData().get(1).getId()),
                containsInAnyOrder(roles.get(0).getId(), roles.get(4).getId())
        );
    }

    @Test
    public void testFindByShopIdAndStatus() {
        List<SellerRole> roles = Lists.newArrayList(
                mock("key1", 1L, 1),
                mock("key2", 1L, 1),
                mock("key1", 1L, 2),
                mock("key2", 1L, 2),
                mock("key1", 1L, 1),
                mock("key1", 2L, 1)
        );
        for (SellerRole role : roles) {
            sellerRoleDao.create(role);
        }

        List<SellerRole> result = sellerRoleDao.findByShopIdAndStatus("key1", 1L, 1);
        assertThat(result.size(), is(2));
        assertThat(
                Lists.newArrayList(result.get(0).getId(), result.get(1).getId()),
                containsInAnyOrder(roles.get(0).getId(), roles.get(4).getId())
        );
    }
}
