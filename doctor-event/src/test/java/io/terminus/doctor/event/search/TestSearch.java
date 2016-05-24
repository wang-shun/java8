package io.terminus.doctor.event.search;

import io.terminus.doctor.event.search.pig.PigDumpService;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Desc: 搜索测试类
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public class TestSearch extends BaseServiceTest {

    @Autowired
    private PigDumpService pigDumpService;
    
    @Test
    public void test01() throws InterruptedException {
        pigDumpService.fullDump("2016-01-01");
    }
}
