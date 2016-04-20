/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.user.dao;

import com.google.common.collect.Lists;
import io.terminus.doctor.user.model.SubSeller;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SubSellerDaoTest extends BaseDaoTest {

    @Autowired
    private SubSellerDao subSellerDao;

    private SubSeller createOne(Long id){
        SubSeller model = new SubSeller();
        model.setUserId(id+10);
        model.setUserName(String.valueOf(id+10));
        model.setShopId(id+100);
        model.setStatus(1);
        model.setRolesJson("[{\"id\":1,\"name\":\"name\"}]");
        model.setExtraJson("{\"Jsonkey\":1}");
        model.setCreatedAt(new Date());
        model.setUpdatedAt(new Date());
        return model;
    }

    @Test
    public void testFinds(){
        SubSeller toCreate = createOne(1L);
        // create
        Boolean result = subSellerDao.create(toCreate);
        assertThat(result, is(true));

        // find by ID
        SubSeller model = subSellerDao.findById(toCreate.getId());
        assertNotNull(model);

        // find by IDs
        List<Long> ids = Lists.newArrayList();
        ids.add(toCreate.getId());
        List<SubSeller> modelList = subSellerDao.findByIds(ids);
        assertTrue(!modelList.isEmpty());
    }

    @Test
    public void testFindByShopIdAndUserId(){
        SubSeller toCreate = createOne(1L);
        subSellerDao.create(toCreate);

        // find by userId
        SubSeller subSeller = subSellerDao.findByShopIdAndUserId(toCreate.getShopId(), toCreate.getUserId());
        assertNotNull(subSeller);
    }
}
