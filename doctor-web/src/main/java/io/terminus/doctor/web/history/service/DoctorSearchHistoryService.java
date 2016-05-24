package io.terminus.doctor.web.history.service;

import io.terminus.common.model.Response;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Desc: 搜索历史接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/24
 */

public interface DoctorSearchHistoryService {

    /**
     * 插入猪舍搜索记录
     * @param userId   用户id
     * @param barnName 猪舍号
     * @return 是否成功
     */
    Response<Boolean> createBarnSearchHistory(@NotNull(message = "userId.not.null") Long userId,
                                              @NotEmpty(message = "barnName.not.empty") String barnName);

    /**
     * 查询猪舍搜索记录
     * @param userId  用户id
     * @return 猪舍搜索记录
     */
    Response<Set<String>> findBarnSearchHistory(@NotNull(message = "userId.not.null") Long userId);

    /**
     * 删除单个猪舍搜索记录
     * @param userId 用户id
     * @param barnName 猪舍名称
     * @return 是否成功
     */
    Response<Boolean> deleteBarnSearchHistory(@NotNull(message = "userId.not.null") Long userId,
                                              @NotEmpty(message = "barnName.not.empty") String barnName);
    /**
     * 删除全部猪舍搜索记录
     * @param userId 用户id
     * @return 是否成功
     */
    Response<Boolean> deleteAllBarnSearchHistories(@NotNull(message = "userId.not.null") Long userId);
}
