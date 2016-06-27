package io.terminus.doctor.web.admin.basic.controller;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Desc: 基础数据后台人员操作controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/basic")
public class DoctorBasics {

    private final DoctorBasicReadService doctorBasicReadService;
    private final DoctorBasicWriteService doctorBasicWriteService;

    @Autowired
    public DoctorBasics(DoctorBasicReadService doctorBasicReadService,
                        DoctorBasicWriteService doctorBasicWriteService) {
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBasicWriteService = doctorBasicWriteService;
    }

    /**
     * 根据基础数据类型和输入码查询基础数据
     * @param type  类型
     * @see io.terminus.doctor.basic.model.DoctorBasic.Type
     * @param srm   输入码
     * @return 基础数据信息
     */
    @RequestMapping(value = "/basic/type", method = RequestMethod.GET)
    public List<DoctorBasic> findBasicByTypeAndSrm(@RequestParam("type") Integer type,
                                                   @RequestParam(value = "srm", required = false) String srm) {
        return RespHelper.or500(doctorBasicReadService.findBasicByTypeAndSrm(type, srm));
    }

    /**
     * 创建或更新DoctorBasic
     * @return 是否成功
     */
    @RequestMapping(value = "/basic", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateBasic(@RequestBody DoctorBasic basic) {
        checkNotNull(basic, "basic.not.null");
        if (basic.getId() == null) {

            RespHelper.or500(doctorBasicWriteService.createBasic(basic));
        } else {
            basic.setUpdatorId(UserUtil.getUserId());
            basic.setUpdatorName(UserUtil.getCurrentUser().getName());
            RespHelper.or500(doctorBasicWriteService.updateBasic(basic));
        }
        return Boolean.TRUE;
    }

    /**
     * 根据主键id删除DoctorBasic
     * @return 是否成功
     */
    @RequestMapping(value = "/basic", method = RequestMethod.DELETE)
    public Boolean deleteBasic(@RequestParam("basicId") Long basicId) {
        return RespHelper.or500(doctorBasicWriteService.deleteBasicById(basicId));
    }
}
