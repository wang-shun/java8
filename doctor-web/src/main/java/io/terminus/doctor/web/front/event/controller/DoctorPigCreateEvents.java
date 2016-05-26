package io.terminus.doctor.web.front.event.controller;

import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgFarmDto;
import io.terminus.doctor.event.dto.event.usual.DoctorChgLocationDto;
import io.terminus.doctor.event.dto.event.usual.DoctorConditionDto;
import io.terminus.doctor.event.dto.event.usual.DoctorDiseaseDto;
import io.terminus.doctor.event.dto.event.usual.DoctorRemovalDto;
import io.terminus.doctor.event.dto.event.usual.DoctorVaccinationDto;
import io.terminus.doctor.event.service.DoctorPigEventWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by yaoqijun.
 * Date:2016-05-26
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/events/create")
public class DoctorPigCreateEvents {

    private final DoctorPigEventWriteService doctorPigEventWriteService;

    @Autowired
    public DoctorPigCreateEvents(DoctorPigEventWriteService doctorPigEventWriteService){
        this.doctorPigEventWriteService = doctorPigEventWriteService;
    }

    @RequestMapping(value = "/createChgLocation", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createChangeLocationEvent(@RequestParam("doctorChgLocationDto") DoctorChgLocationDto doctorChgLocationDto,
                                          @RequestParam("doctorBasicInputInfoDto") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                          @RequestParam("pigType") Integer pigType){
        return RespHelper.or500(doctorPigEventWriteService.chgLocationEvent(doctorChgLocationDto, doctorBasicInputInfoDto, pigType));
    }

    @RequestMapping(value = "/createChgFarm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createChangeFarmEvent(@RequestParam("doctorChgFarmDto") DoctorChgFarmDto doctorChgFarmDto,
                                      @RequestParam("doctorBasicInputInfoDto") DoctorBasicInputInfoDto basicInputInfoDto,
                                      @RequestParam("pigType") Integer pigType){
        return RespHelper.or500(doctorPigEventWriteService.chgFarmEvent(doctorChgFarmDto, basicInputInfoDto, pigType));
    }

    @RequestMapping(value = "/createRemovalEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createRemovalEvent(@RequestParam("doctorRemovalDto") DoctorRemovalDto doctorRemovalDto,
                                   @RequestParam("doctorBasicInputInfoDto") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                   @RequestParam("pigType") Integer pigType){
        return RespHelper.or500(doctorPigEventWriteService.removalEvent(doctorRemovalDto, doctorBasicInputInfoDto, pigType));
    }

    @RequestMapping(value = "/createDiseaseEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createRemovalEvent(@RequestParam("doctorDiseaseDto") DoctorDiseaseDto doctorDiseaseDto,
                                   @RequestParam("doctorBasicInputInfoDto") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                   @RequestParam("pigType") Integer pigType){
        return RespHelper.or500(doctorPigEventWriteService.diseaseEvent(doctorDiseaseDto, doctorBasicInputInfoDto, pigType));
    }

    @RequestMapping(value = "/createVaccinationEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createVaccinationEvent(@RequestParam("doctorVaccinationDto") DoctorVaccinationDto doctorVaccinationDto,
                                   @RequestParam("doctorBasicInputInfoDto") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                   @RequestParam("pigType") Integer pigType){
        return RespHelper.or500(doctorPigEventWriteService.vaccinationEvent(doctorVaccinationDto, doctorBasicInputInfoDto, pigType));
    }

    @RequestMapping(value = "/createConditionEvent", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createConditionEvent(@RequestParam("doctorConditionDto") DoctorConditionDto doctorConditionDto,
                                       @RequestParam("doctorBasicInputInfoDto") DoctorBasicInputInfoDto doctorBasicInputInfoDto,
                                       @RequestParam("pigType") Integer pigType){
        return RespHelper.or500(doctorPigEventWriteService.conditionEvent(doctorConditionDto, doctorBasicInputInfoDto, pigType));
    }
}
