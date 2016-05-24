package io.terminus.doctor.web.barn.controller;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorBarnWriteService;
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
 * Desc: 猪舍表Controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-24
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor")
public class DoctorBarns {

    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorBarnWriteService doctorBarnWriteService;

    @Autowired
    public DoctorBarns(DoctorBarnReadService doctorBarnReadService,
                       DoctorBarnWriteService doctorBarnWriteService) {
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorBarnWriteService = doctorBarnWriteService;
    }

    /**
     * 根据id查询猪舍表
     * @param barnId 主键id
     * @return 猪舍表
     */
    @RequestMapping(value = "/barn/id", method = RequestMethod.GET)
    public DoctorBarn findBarnById(@RequestParam("barnId") Long barnId) {
        return RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
    }

    /**
    * 根据farmId查询猪舍表
    * @param farmId 猪场id
    * @return 猪舍表列表
    */
    @RequestMapping(value = "/barn/farmId", method = RequestMethod.GET)
    public List<DoctorBarn> findBarnsByfarmId(@RequestParam("farmId") Long farmId) {
        return RespHelper.or500(doctorBarnReadService.findBarnsByFarmId(farmId));
    }

    /**
    * 创建或更新DoctorBarn
    * @return 是否成功
    */
    @RequestMapping(value = "/barn", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateBarn(@RequestBody DoctorBarn barn) {
        checkNotNull(barn, "barn.not.null");

        // TODO: 权限中心校验权限

        if (barn.getId() == null) {

            RespHelper.or500(doctorBarnWriteService.createBarn(barn));
        } else {

            RespHelper.or500(doctorBarnWriteService.updateBarn(barn));
        }
        return Boolean.TRUE;
    }

    /**
     * 根据主键id删除DoctorBarn
     * @return 是否成功
     */
    @RequestMapping(value = "/barn", method = RequestMethod.DELETE)
    public Boolean deleteBarn(@RequestParam("barnId") Long barnId) {
        DoctorBarn barn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));

        // TODO: 权限中心校验权限

        return RespHelper.or500(doctorBarnWriteService.deleteBarnById(barnId));
    }
}