/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.user.dao;

import com.google.common.collect.Lists;
import io.terminus.doctor.user.model.Seller;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SellerDaoTest extends BaseDaoTest {

    @Autowired
    private SellerDao sellerDao;

    private Seller createOne(Long id){
        Seller model = new Seller();
        model.setUserId(id+10);
        model.setUserName(String.valueOf(id+10));
        model.setShopId(id+100);
        model.setShopName(id+100+"name");
        model.setStatus(1);
        model.setExtraJson("{\"Jsonkey\":1}");
        model.setCreatedAt(new Date());
        model.setUpdatedAt(new Date());
        return model;
    }

    @Test
    public void testFinds(){
        Seller toCreate = createOne(1L);
        // create
        Boolean result = sellerDao.create(toCreate);
        assertThat(result, is(true));

        // find by ID
        Seller model = sellerDao.findById(toCreate.getId());
        assertNotNull(model);

        // find by IDs
        List<Long> ids = Lists.newArrayList();
        ids.add(toCreate.getId());
        List<Seller> modelList = sellerDao.findByIds(ids);
        assertTrue(!modelList.isEmpty());
    }
}
