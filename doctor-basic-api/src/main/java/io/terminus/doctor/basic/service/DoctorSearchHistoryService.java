package io.terminus.doctor.basic.service;

import io.terminus.common.model.Response;

import java.util.Set;

/**
 * Desc: 搜索历史接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/24
 */

public interface DoctorSearchHistoryService {

    /**
     * 插入搜索记录
     * @param userId        用户id
     * @param searchType    搜索类型
     *                      @see io.terminus.doctor.basic.enums.SearchType
     * @param word          搜索词
     * @return
     */
    Response<Boolean> createSearchHistory(Long userId, int searchType, String word);

    /**
     * 查询猪舍搜索记录
     * @param userId        用户id
     * @param searchType    搜索类型
     * @return    历史搜索词
     */
    Response<Set<String>> findSearchHistory(Long userId, int searchType);

    /**
     * 查询猪舍搜索记录
     * @param userId        用户id
     * @param searchType    搜索类型
     * @param size          搜索词数量
     * @return    历史搜索词
     */
    Response<Set<String>> findSearchHistory(Long userId, int searchType, Long size);

    /**
     * 删除单个猪舍搜索记录
     * @param userId        用户id
     * @param searchType    搜索类型
     * @param word          猪舍名称
     * @return      是否成功
     */
    Response<Boolean> deleteSearchHistory(Long userId, int searchType, String word);
    /**
     * 删除全部猪舍搜索记录
     * @param userId        用户id
     * @param searchType    搜索类型
     * @return      是否成功
     */
    Response<Boolean> deleteAllSearchHistories(Long userId, int searchType);
}
