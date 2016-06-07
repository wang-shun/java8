package io.terminus.doctor.web.front.basic.controller;

import com.google.common.base.Strings;
import io.terminus.doctor.basic.service.DoctorSearchHistoryService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.pampas.common.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Desc: 搜索词历史搜索
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/25
 */
@RestController
@RequestMapping("/api/doctor/search")
public class SearchHistories {

    private final DoctorSearchHistoryService doctorSearchHistoryService;

    @Autowired
    public SearchHistories(DoctorSearchHistoryService doctorSearchHistoryService) {
        this.doctorSearchHistoryService = doctorSearchHistoryService;
    }

    /**
     * 查询历史搜索词
     *
     * @param type 搜索类型
     *             @see io.terminus.doctor.basic.enums.SearchType
     * @return
     */
    @RequestMapping(value = "/history", method = RequestMethod.GET)
    public List<String> findHistoryWords(@RequestParam Integer type,
                                         @RequestParam(required = false) Long size) {
        if (type != null) {
            return RespHelper.or500(doctorSearchHistoryService.findSearchHistory(UserUtil.getUserId(), type, size));
        }
        return Collections.emptyList();
    }

    /**
     * 删除所有的搜索词记录
     *
     * @param type 搜索类型
     *             @see io.terminus.doctor.basic.enums.SearchType
     * @return
     */
    @RequestMapping(value = "/history", method = RequestMethod.DELETE)
    public Boolean deleteAllHistoryWord(@RequestParam Integer type) {
        if (type != null) {
            return RespHelper.or500(doctorSearchHistoryService
                    .deleteAllSearchHistories(UserUtil.getUserId(), type));
        }
        return Boolean.TRUE;
    }

    /**
     * 删除单个搜索词
     *
     * @param type 搜索类型
     *             @see io.terminus.doctor.basic.enums.SearchType
     * @param word 要删除的搜索词
     * @return
     */
    @RequestMapping(value = "/history/word", method = RequestMethod.DELETE)
    public Boolean deleteHistoryWord(@RequestParam Integer type, @RequestParam String word) {
        if (type != null) {
            return RespHelper.or500(doctorSearchHistoryService
                    .deleteSearchHistory(UserUtil.getUserId(), type, Strings.nullToEmpty(word)));
        }
        return Boolean.TRUE;
    }
}
