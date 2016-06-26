package io.terminus.doctor.web.admin.basic.controller;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBreed;
import io.terminus.doctor.basic.model.DoctorGenetic;
import io.terminus.doctor.basic.model.DoctorUnit;
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

    /********************** 品种相关 *************************/
    /**
     * 根据id查询品种表
     * @param breedId 主键id
     * @return 品种表
     */
    @RequestMapping(value = "/breed/id", method = RequestMethod.GET)
    public DoctorBreed findBreedById(@RequestParam("breedId") Long breedId) {
        return RespHelper.or500(doctorBasicReadService.findBreedById(breedId));
    }


    /**
     * 创建或更新DoctorBreed
     * @return 是否成功
     */
    @RequestMapping(value = "/breed", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateBreed(@RequestBody DoctorBreed breed) {
        checkNotNull(breed, "breed.not.null");

        // TODO: 权限中心校验权限

        if (breed.getId() == null) {
            RespHelper.or500(doctorBasicWriteService.createBreed(breed));
        } else {
            RespHelper.or500(doctorBasicWriteService.updateBreed(breed));
        }
        return Boolean.TRUE;
    }

    /**
     * 根据主键id删除DoctorBreed
     * @return 是否成功
     */
    @RequestMapping(value = "/breed", method = RequestMethod.DELETE)
    public Boolean deleteBreed(@RequestParam("breedId") Long breedId) {
        DoctorBreed breed = RespHelper.or500(doctorBasicReadService.findBreedById(breedId));

        // TODO: 权限中心校验权限

        return RespHelper.or500(doctorBasicWriteService.deleteBreedById(breedId));
    }

    /********************* 品系相关 ***************************/
    /**
     * 根据id查询品系表
     * @param geneticId 主键id
     * @return 品系表
     */
    @RequestMapping(value = "/genetic/id", method = RequestMethod.GET)
    public DoctorGenetic findGeneticById(@RequestParam("geneticId") Long geneticId) {
        return RespHelper.or500(doctorBasicReadService.findGeneticById(geneticId));
    }

    /**
     * 创建或更新DoctorGenetic
     * @return 是否成功
     */
    @RequestMapping(value = "/genetic", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateGenetic(@RequestBody DoctorGenetic genetic) {
        checkNotNull(genetic, "genetic.not.null");

        // TODO: 权限中心校验权限

        if (genetic.getId() == null) {
            RespHelper.or500(doctorBasicWriteService.createGenetic(genetic));
        } else {
            RespHelper.or500(doctorBasicWriteService.updateGenetic(genetic));
        }
        return Boolean.TRUE;
    }

    /**
     * 根据主键id删除DoctorGenetic
     * @return 是否成功
     */
    @RequestMapping(value = "/genetic", method = RequestMethod.DELETE)
    public Boolean deleteGenetic(@RequestParam("geneticId") Long geneticId) {
        DoctorGenetic genetic = RespHelper.or500(doctorBasicReadService.findGeneticById(geneticId));

        // TODO: 权限中心校验权限

        return RespHelper.or500(doctorBasicWriteService.deleteGeneticById(geneticId));
    }

    /********************** 品系相关 ***************************/
    /**
     * 根据id查询计量单位表
     * @param unitId 主键id
     * @return 计量单位表
     */
    @RequestMapping(value = "/unit/id", method = RequestMethod.GET)
    public DoctorUnit findUnitById(@RequestParam("unitId") Long unitId) {
        return RespHelper.or500(doctorBasicReadService.findUnitById(unitId));
    }

    /**
     * 创建或更新DoctorUnit
     * @return 是否成功
     */
    @RequestMapping(value = "/unit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createOrUpdateUnit(@RequestBody DoctorUnit unit) {
        checkNotNull(unit, "unit.not.null");

        // TODO: 权限中心校验权限

        if (unit.getId() == null) {
            RespHelper.or500(doctorBasicWriteService.createUnit(unit));
        } else {
            RespHelper.or500(doctorBasicWriteService.updateUnit(unit));
        }
        return Boolean.TRUE;
    }

    /**
     * 根据主键id删除DoctorUnit
     * @return 是否成功
     */
    @RequestMapping(value = "/unit", method = RequestMethod.DELETE)
    public Boolean deleteUnit(@RequestParam("unitId") Long unitId) {
        DoctorUnit unit = RespHelper.or500(doctorBasicReadService.findUnitById(unitId));

        // TODO: 权限中心校验权限

        return RespHelper.or500(doctorBasicWriteService.deleteUnitById(unitId));
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
