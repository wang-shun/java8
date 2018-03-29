package io.terminus.doctor.web.front.event.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorDemo;
import io.terminus.doctor.event.service.DoctorDemoReadService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by xjn on 18/3/29.
 * email:xiaojiannan@terminus.io
 */
@RestController
@RequestMapping("/api/doctor/demos")
public class DoctorDemos {

    @RpcConsumer
    public DoctorDemoReadService doctorDemoReadService;

    @RequestMapping(value = "/find/by/name", method = RequestMethod.GET)
    public DoctorDemo findByName(@RequestParam String name) {
        return RespHelper.or500(doctorDemoReadService.findByName(name));
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public Boolean createDemo(@RequestBody DoctorDemo doctorDemo) {
        return RespHelper.or500(doctorDemoReadService.createDemo(doctorDemo));
    }
}
