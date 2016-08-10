package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.Preconditions;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorVaccinationPigWarn;
import io.terminus.doctor.event.service.DoctorVaccinationPigWarnReadService;
import io.terminus.doctor.event.service.DoctorVaccinationPigWarnWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desc: 猪只免疫程序录入
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/17
 */
@RestController
@Slf4j
@RequestMapping("/api/doctor/vacc")
public class DoctorVaccinations {

    private final DoctorVaccinationPigWarnReadService doctorVaccinationPigWarnReadService;
    private final DoctorVaccinationPigWarnWriteService doctorVaccinationPigWarnWriteService;
    private final DoctorFarmReadService doctorFarmReadService;

    @Autowired
    public DoctorVaccinations(DoctorVaccinationPigWarnReadService doctorVaccinationPigWarnReadService,
                              DoctorVaccinationPigWarnWriteService doctorVaccinationPigWarnWriteService,
                              DoctorFarmReadService doctorFarmReadService) {
        this.doctorVaccinationPigWarnReadService = doctorVaccinationPigWarnReadService;
        this.doctorVaccinationPigWarnWriteService = doctorVaccinationPigWarnWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
    }

    /**
     * 分页获取猪只免疫程序
     *
     * @param pageNo   页数
     * @param pageSize 页大小
     * @param farmId   猪场id
     */
    @RequestMapping(value = "/warns", method = RequestMethod.GET)
    public Paging<DoctorVaccinationPigWarn> pagingVaccPigWarns(@RequestParam Integer pageNo,
                                                               @RequestParam Integer pageSize,
                                                               @RequestParam Long farmId) {
        return RespHelper.or500(doctorVaccinationPigWarnReadService.pagingVaccPigWarns(pageNo, pageSize, farmId));
    }

    /**
     * 获取免疫程序详情
     *
     * @param id 免疫程序id
     */
    @RequestMapping(value = "/warns/detail", method = RequestMethod.GET)
    public DoctorVaccinationPigWarn findVaccById(@RequestParam Long id) {
        return RespHelper.or500(doctorVaccinationPigWarnReadService.findVaccinationPigWarnById(id));
    }

    /**
     * 创建或更新一个免疫程序
     * @param warn 模板
     */
    @RequestMapping(value = "/warns", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateVaccWarns(@RequestBody DoctorVaccinationPigWarn warn) {
        Preconditions.checkNotNull(warn, "pig.vacc.warn.null");
        if (warn.getId() == null) {
            warn.setCreatorId(UserUtil.getUserId());
            // 获取猪场name
            DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(warn.getFarmId()));
            warn.setFarmName(farm != null ? farm.getName() : "");
            RespHelper.or500(doctorVaccinationPigWarnWriteService.createVaccinationPigWarn(warn));
        } else {
            RespHelper.or500(doctorVaccinationPigWarnWriteService.updateVaccinationPigWarn(warn));
        }
        return Boolean.TRUE;
    }

    /**
     * 删除免疫程序
     * @param id    免疫程序id
     */
    @RequestMapping(value = "/warns", method = RequestMethod.DELETE)
    public Boolean deleteVaccWarns(@RequestParam Long id) {
        return RespHelper.or500(doctorVaccinationPigWarnWriteService.deleteVaccinationPigWarnById(id));
    }
}
