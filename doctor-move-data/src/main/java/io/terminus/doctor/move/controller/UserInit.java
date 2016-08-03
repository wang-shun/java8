package io.terminus.doctor.move.controller;

import com.google.common.base.Throwables;
import io.terminus.doctor.move.service.UserInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by chenzenghui on 16/7/27.
 */

@Slf4j
@RestController
@RequestMapping("/api/data/user")
public class UserInit {

    @Autowired
    private UserInitService userInitService;

    @RequestMapping(value = "/init", method = RequestMethod.GET)
    public String userInit(@RequestParam String mobile, @RequestParam Long dataSourceId) {
        log.warn("start to init user and farm data, mobile = {}, dataSourceId = {}", mobile, dataSourceId);
        try{
            userInitService.init(mobile, dataSourceId);
            log.warn("init user and farm data succeed, mobile = {}, dataSourceId = {}", mobile, dataSourceId);
            return "ok";
        }catch(Exception e){
            log.error("init user data, mobile={}, dataSourceId={}, error:{}", mobile, dataSourceId, Throwables.getStackTraceAsString(e));
            return Throwables.getStackTraceAsString(e);
        }
    }

}
