package io.terminus.doctor.web.admin.controller;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Desc: admin端 公司api
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/org")
public class DoctorAdminOrgs {

    @RpcConsumer
    private DoctorOrgReadService doctorOrgReadService;

    /**
     * 查询全部公司
     * @return 全部公司
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorOrg> findAllOrgs() {
        return RespHelper.or500(doctorOrgReadService.findAllOrgs());
    }
}
