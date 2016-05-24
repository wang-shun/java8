package io.terminus.doctor.web.history.controller;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.web.history.service.DoctorSearchHistoryService;
import io.terminus.pampas.common.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Set;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 搜索记录controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/24
 */
@RestController
@RequestMapping("/api/doctor/search/history")
public class DoctorSearchHistories {

    private final DoctorSearchHistoryService doctorSearchHistoryService;

    @Autowired
    public DoctorSearchHistories(DoctorSearchHistoryService doctorSearchHistoryService) {
        this.doctorSearchHistoryService = doctorSearchHistoryService;
    }

    /**
     * 插入一条历史记录, 如果barnName为空, 直接返回true
     * @param barnName 猪舍名称
     * @return 是否成功
     */
    @RequestMapping(value = "/barn/set", method = RequestMethod.GET)
    public Boolean createBarnSearchHistory(@RequestParam(value = "barnName", required = false) String barnName) {
        if (notEmpty(barnName)) {
            doctorSearchHistoryService.createBarnSearchHistory(UserUtil.getUserId(), barnName);
        }
        return Boolean.TRUE;
    }

    /**
     * 查询当前登录用户的猪舍搜索记录
     * @return 猪舍搜索记录
     */
    @RequestMapping(value = "/barn/get", method = RequestMethod.GET)
    public Set<String> findBarnSearchHistory() {
        return RespHelper.or(doctorSearchHistoryService.findBarnSearchHistory(UserUtil.getUserId()), Collections.emptySet());
    }

    /**
     * 删除当前登录用户的单条猪舍搜索记录
     * @param barnName  猪舍名称
     * @return 猪舍搜索记录
     */
    @RequestMapping(value = "/barn/delete", method = RequestMethod.GET)
    public Boolean deleteBarnSearchHistory(@RequestParam("barnName") String barnName) {
        doctorSearchHistoryService.deleteBarnSearchHistory(UserUtil.getUserId(), barnName);
        return Boolean.TRUE;
    }

    /**
     * 删除当前登录用户的全部猪舍搜索记录
     * @return 猪舍搜索记录
     */
    @RequestMapping(value = "/barn/deleteAll", method = RequestMethod.GET)
    public Boolean deleteAllBarnSearchHistories() {
        doctorSearchHistoryService.deleteAllBarnSearchHistories(UserUtil.getUserId());
        return Boolean.TRUE;
    }
}
