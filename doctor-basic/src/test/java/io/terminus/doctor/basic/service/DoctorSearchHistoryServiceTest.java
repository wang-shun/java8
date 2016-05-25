package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.base.BaseServiceTest;
import io.terminus.doctor.common.utils.RespHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

/**
 * Desc: 历史搜索记录测试
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/25
 */
public class DoctorSearchHistoryServiceTest extends BaseServiceTest {

    @Autowired
    private DoctorSearchHistoryService doctorSearchHistoryService;

    /**
     * 用户id
     */
    private Long userId = 110L;

    /**
     * 搜索类型
     *
     * @see io.terminus.doctor.basic.enums.SearchType
     */
    private int searchType = 1;

    @Before
    public void before() throws InterruptedException {
        // 1. 删除数据
        doctorSearchHistoryService.deleteAllSearchHistories(userId, searchType);
        // 2. 新增测试数据
        for (int i = 0; i < 12; i++) {
            doctorSearchHistoryService.createSearchHistory(userId, searchType, "搜索词" + (i + 1));
            Thread.sleep(10);
        }
    }

    @After
    public void after() {
        doctorSearchHistoryService.deleteAllSearchHistories(userId, searchType);
    }

    @Test
    public void test_CREATE_HistoryWord() {
        doctorSearchHistoryService.createSearchHistory(userId, searchType, "搜索词13");
        Set<String> words = RespHelper.orServEx(doctorSearchHistoryService.findSearchHistory(userId, searchType, -1L));
        Assert.assertEquals(13, words.size());
    }

    @Test
    public void test_DELETE_HistoryWord() {
        doctorSearchHistoryService.deleteSearchHistory(userId, searchType, "搜索词1");
        Set<String> words = RespHelper.orServEx(doctorSearchHistoryService.findSearchHistory(userId, searchType, -1L));
        Assert.assertEquals(11, words.size());
    }

    @Test
    public void test_SEARCH_HistoryWord() {
        Set<String> words = RespHelper.orServEx(doctorSearchHistoryService.findSearchHistory(userId, searchType));
        Assert.assertEquals(10, words.size());
    }

    @Test
    public void test_DELETE_AllHistoryWord() {
        doctorSearchHistoryService.deleteAllSearchHistories(userId, searchType);
        Set<String> words = RespHelper.orServEx(doctorSearchHistoryService.findSearchHistory(userId, searchType, -1L));
        Assert.assertEquals(0, words.size());
    }
}
