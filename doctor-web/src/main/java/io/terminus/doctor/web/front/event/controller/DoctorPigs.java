package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorPigMessage;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.web.front.event.dto.DoctorBoarDetailDto;
import io.terminus.doctor.web.front.event.dto.DoctorSowDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-06-01
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/pigs")
public class DoctorPigs {

    private final DoctorPigReadService doctorPigReadService;

    @Autowired
    public DoctorPigs(DoctorPigReadService doctorPigReadService){
        this.doctorPigReadService = doctorPigReadService;
    }

    @RequestMapping(value = "/queryByStatus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorPigInfoDto> pagingDoctorPigInfoDetail(@RequestParam("farmId") Long farmId,
                                                              @RequestParam("status") Integer status,
                                                              @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                              @RequestParam(value = "pageSize", required = false) Integer pageSize){
        DoctorPigTrack doctorPigTrack = null;
        try{
            doctorPigTrack = DoctorPigTrack.builder()
                    .farmId(farmId).status(status)
                    .build();
        }catch (Exception e){
            log.error("paging doctor pig info detail fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorPigReadService.pagingDoctorInfoDtoByPigTrack(doctorPigTrack, pageNo, pageSize));
    }

    /**
     * 生成FostersCode
     * @param pigId
     * @return
     */
    @RequestMapping(value = "/generate/fostersCode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String generateFostersCode(@RequestParam("pigId") Long pigId){
        return RespHelper.or500(doctorPigReadService.generateFostersCode(pigId));
    }

    /**
     * pig id 获取 pig track 信息内容
     * @param pigId
     * @return
     */
    @RequestMapping(value = "/getPigInfoDto", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorPigInfoDto queryDoctorInfoDtoById(@RequestParam("pigId") Long pigId){
        return RespHelper.or500(doctorPigReadService.queryDoctorInfoDtoById(pigId));
    }

    @RequestMapping(value = "/getPigDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorPigInfoDetailDto queryPigDetailInfoDto(@RequestParam("farmId") Long farmId,
                                                        @RequestParam("pigId") Long pigId,
                                                        @RequestParam(value = "eventSize", required = false) Integer eventSize){
        return RespHelper.or500(doctorPigReadService.queryPigDetailInfoByPigId(pigId, eventSize));
    }

    @RequestMapping(value = "/getSowPigDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorSowDetailDto querySowPigDetailInfoDto(@RequestParam("farmId") Long farmId,
                                                       @RequestParam("pigId") Long pigId,
                                                       @RequestParam(value = "eventSize", required = false) Integer eventSize){
        return buildSowDetailDto(queryPigDetailInfoDto(farmId, pigId, eventSize));
    }

    @RequestMapping(value = "/getBoarPigDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorBoarDetailDto queryBoarPigDetailInfoDto(@RequestParam("farmId") Long farmId,
                                                         @RequestParam("pigId") Long pigId,
                                                         @RequestParam(value = "eventSize", required = false) Integer eventSize){
        return buildDoctorBoarDetailDto(queryPigDetailInfoDto(farmId, pigId, eventSize));
    }

    private DoctorBoarDetailDto buildDoctorBoarDetailDto(DoctorPigInfoDetailDto dto){
        DoctorBoarDetailDto doctorBoarDetailDto = DoctorBoarDetailDto.builder()
                .pigBoarCode(dto.getDoctorPig().getPigCode()).breedName(dto.getDoctorPig().getBreedName())
                .barnCode(dto.getDoctorPigTrack().getCurrentBarnName()).pigStatus(dto.getDoctorPigTrack().getStatus())
                .field123456("notKnow").doctorPigEvents(dto.getDoctorPigEvents())
                .build();
        return doctorBoarDetailDto;
    }

    private DoctorSowDetailDto buildSowDetailDto(DoctorPigInfoDetailDto dto){
        DoctorSowDetailDto doctorSowDetailDto = DoctorSowDetailDto.builder()
                .pigSowCode(dto.getDoctorPig().getPigCode()).breedName(dto.getDoctorPig().getBreedName()).barnCode(dto.getDoctorPigTrack().getCurrentBarnName())
                .pigStatus(dto.getDoctorPigTrack().getStatus())
                .dayAge(Days.daysBetween(new DateTime(dto.getDoctorPig().getBirthDate()), DateTime.now()).getDays())
                .parity(dto.getDoctorPigTrack().getCurrentParity()).entryDate(dto.getDoctorPig().getInFarmDate())
                .birthDate(dto.getDoctorPig().getBirthDate())
                .doctorPigEvents(dto.getDoctorPigEvents())
                .build();
        return doctorSowDetailDto;
    }

    /**
     * 获取猪只提示的消息
     * @param pigId 猪id
     * @return
     */
    @RequestMapping(value = "/notify/message", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorPigMessage> queryPigNotifyMessages(Long pigId) {
        return RespHelper.or500(doctorPigReadService.findPigMessageByPigId(pigId));
    }
}
