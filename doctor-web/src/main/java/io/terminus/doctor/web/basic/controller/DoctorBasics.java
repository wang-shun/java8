package io.terminus.doctor.web.basic.controller;

import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.basic.service.DoctorBasicWriteService;
import io.terminus.doctor.common.utils.RespHelper;
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
 * Desc: 基础数据Controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/basic")
public class DoctorBasics {

    private final DoctorBasicReadService doctorBasicReadService;
    private final DoctorBasicWriteService doctorBasicWriteService;

    @Autowired
    public DoctorBasics(DoctorBasicReadService doctorBasicReadService,
                        DoctorBasicWriteService doctorBasicWriteService) {
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBasicWriteService = doctorBasicWriteService;
    }

    /************************** 疾病防疫相关 **************************/
    /**
     * 查询疾病详情
     * @param diseaseId 主键id
     * @return 疾病表
     */
    @RequestMapping(value = "/disease/id", method = RequestMethod.GET)
    public DoctorDisease findDiseaseById(@RequestParam("diseaseId") Long diseaseId) {
        return RespHelper.or500(doctorBasicReadService.findDiseaseById(diseaseId));
    }

    /**
     * 根据farmId查询疾病列表
     * @param farmId 猪场id
     * @return 疾病列表
     */
    @RequestMapping(value = "/disease/farmId", method = RequestMethod.GET)
    public List<DoctorDisease> findDiseaseByfarmId(@RequestParam("farmId") Long farmId) {
        return RespHelper.or500(doctorBasicReadService.findDiseasesByFarmId(farmId));
    }

    /**
     * 创建或更新疾病表
     * @return 是否成功
     */
    @RequestMapping(value = "/disease", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateDisease(@RequestBody DoctorDisease disease) {
        checkNotNull(disease, "disease.not.null");

        // TODO: 权限中心校验权限

        if (disease.getId() == null) {
            RespHelper.or500(doctorBasicWriteService.createDisease(disease));
        } else {
            RespHelper.or500(doctorBasicWriteService.updateDisease(disease));
        }
        return Boolean.TRUE;
    }

    /**
     * 根据主键id删除DoctorDisease
     * @return 是否成功
     */
    @RequestMapping(value = "/disease", method = RequestMethod.DELETE)
    public Boolean deleteDisease(@RequestParam("diseaseId") Long diseaseId) {
        DoctorDisease disease = RespHelper.or500(doctorBasicReadService.findDiseaseById(diseaseId));

        // TODO: 权限中心校验权限

        return RespHelper.or500(doctorBasicWriteService.deleteDiseaseById(diseaseId));
    }


}
