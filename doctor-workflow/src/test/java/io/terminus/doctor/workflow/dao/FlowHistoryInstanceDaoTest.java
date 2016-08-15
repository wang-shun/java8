package io.terminus.doctor.workflow.dao;

import com.google.common.collect.Maps;
import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.model.FlowHistoryInstance;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.Map;

/**
 * Created by xiao on 16/8/9.
 */
public class FlowHistoryInstanceDaoTest extends BaseServiceTest{
    @Autowired
    private  FlowHistoryInstanceDao flowHistoryInstanceDao;

    @Test
    @Rollback(value = false)
    public void create(){
        FlowHistoryInstance flowHistoryInstance = FlowHistoryInstance.builder().externalHistoryId(1000l).name("母猪状态流图流转方式1466498844247").flowDefinitionId(2l).flowDefinitionKey("sow").
                businessId(6l).build();
        Boolean b = flowHistoryInstanceDao.create(flowHistoryInstance);
        System.out.println(b);
    }

    @Test
    @Rollback(value = false)
    public void update(){
        FlowHistoryInstance flowHistoryInstance = flowHistoryInstanceDao.findById(21l);
        flowHistoryInstance.setExternalHistoryId(1001l);
        Boolean b = flowHistoryInstanceDao.update(flowHistoryInstance);
    }

    @Test
    @Rollback(value = false)
    public void query(){
        Map<String,Object> map = Maps.newHashMap();
        map.put("externalHistoryId",1001);
        System.out.println(flowHistoryInstanceDao.count(map));
    }

    @Test
    @Rollback(value = false)
    public void delete(){
        System.out.println(flowHistoryInstanceDao.delete(21l));
    }
}
