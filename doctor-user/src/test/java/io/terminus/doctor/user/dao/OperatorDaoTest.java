/*
 * Copyright (c) 2016. 杭州端点网络科技有限公司.  All rights reserved.
 */

package io.terminus.doctor.user.dao;

import com.google.common.collect.Lists;
import io.terminus.doctor.user.model.Operator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 用户子账户测试
 *
 * Author  : panxin
 * Date    : 2:53 PM 3/12/16
 * Mail    : panxin@terminus.io
 */
public class OperatorDaoTest extends BaseDaoTest {

    @Autowired
    private OperatorDao operatorDao;

    private Operator createOne(Long id){
        Operator model = new Operator();
        model.setUserId(id+10);
        model.setRoleId(id+100);
        model.setStatus(1);
        model.setExtraJson("{\"Jsonkey\":1}");
        model.setCreatedAt(new Date());
        model.setUpdatedAt(new Date());
        return model;
    }

    @Test
    public void testFinds(){
        // create
        Operator toCreate = createOne(1L);
        Boolean result = operatorDao.create(toCreate);
        assertThat(result, is(true));

        // find by ID
        Operator model = operatorDao.findById(toCreate.getId());
        assertNotNull(model);

        // find by IDs
        List<Long> ids = Lists.newArrayList();
        ids.add(toCreate.getId());
        List<Operator> modelList = operatorDao.findByIds(ids);
        assertTrue(!modelList.isEmpty());
    }

    @Test
    public void testFindByUserId(){
        operatorDao.create(createOne(1L));

        assertNotNull(operatorDao.findByUserId(11L));
    }
}
