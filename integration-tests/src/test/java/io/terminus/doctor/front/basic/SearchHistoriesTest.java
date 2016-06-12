package io.terminus.doctor.front.basic;

import com.google.common.collect.ImmutableMap;
import configuration.front.FrontWebConfiguration;
import io.terminus.doctor.front.BaseFrontWebTest;
import org.junit.Test;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Desc: 搜索历史controller测试类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/7
 */
@SpringApplicationConfiguration(FrontWebConfiguration.class)
public class SearchHistoriesTest extends BaseFrontWebTest {

    /**
     * 查询历史搜索词
     */
    @Test
    public void findHistoryWordsTest() {
        String url = "/api/doctor/search/history";
        ResponseEntity<List> result = getForEntity(url, ImmutableMap.of("type", 1, "size", 5), List.class);
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertNotNull(result.getBody());
    }

    /**
     * 删除所有的搜索词记录
     */
    @Test
    public void deleteAllHistoryWordTest() {
        String url = "/api/doctor/search/history";
        delete(url, ImmutableMap.of("type", 1));

    }

    /**
     * 删除单个搜索词
     */
    @Test
    public void deleteHistoryWordTest() {
        String url = "/api/doctor/search/history/word";
        delete(url, ImmutableMap.of("type", 1, "word", "母猪舍"));
    }
}
