package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorPigMessage;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.web.front.event.dto.DoctorBoarDetailDto;
import io.terminus.doctor.web.front.event.dto.DoctorFosterDetail;
import io.terminus.doctor.web.front.event.dto.DoctorMatingDetail;
import io.terminus.doctor.web.front.event.dto.DoctorSowDetailDto;
import io.terminus.parana.user.service.UserProfileReadService;
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

import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private final DoctorPigWriteService doctorPigWriteService;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorBasicReadService doctorBasicReadService;
    private final UserProfileReadService userProfileReadService;
    @Autowired
    public DoctorPigs(DoctorPigReadService doctorPigReadService, DoctorPigWriteService doctorPigWriteService, DoctorGroupReadService doctorGroupReadService,
                      DoctorBasicReadService doctorBasicReadService, UserProfileReadService userProfileReadService){
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigWriteService = doctorPigWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.userProfileReadService = userProfileReadService;
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
     * @param farmId
     * @return
     */
    @RequestMapping(value = "/generate/fostersCode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String generateFostersCode(@RequestParam("farmId") Long farmId){
        return RespHelper.or500(doctorPigReadService.generateFostersCode(farmId));
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
        DoctorPigInfoDetailDto doctorPigInfoDetailDto = RespHelper.or500(doctorPigReadService.queryPigDetailInfoByPigId(pigId, eventSize));
        return transFromType(doctorPigInfoDetailDto);
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
                .entryDate(dto.getDoctorPig().getInFarmDate()).birthDate(dto.getDoctorPig().getBirthDate())
                .doctorPigEvents(dto.getDoctorPigEvents())
                .build();
        return doctorBoarDetailDto;
    }

    private DoctorSowDetailDto buildSowDetailDto(DoctorPigInfoDetailDto dto){
        DoctorSowDetailDto doctorSowDetailDto = DoctorSowDetailDto.builder()
                .pigSowCode(dto.getDoctorPig().getPigCode())
                .warnMessage(dto.getDoctorPigTrack().getExtraMessage())
                .breedName(dto.getDoctorPig().getBreedName()).barnCode(dto.getDoctorPigTrack().getCurrentBarnName())
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

    /**
     * 获取母猪配种次数和第一次配种时间
     * @param farmId 猪场id
     * @param pigId 猪Id
     * @return 母猪配种次数和第一次配种时间
     */
    @RequestMapping(value = "/getMatingDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorMatingDetail getMatingDetail(@RequestParam("farmId") Long farmId,
                                               @RequestParam("pigId") Long pigId) {
        DoctorMatingDetail doctorMatingDetail = new DoctorMatingDetail();
        Response<Integer> respMatingCount = doctorPigReadService.getCountOfMating(pigId);
        if (respMatingCount.isSuccess()) {
            doctorMatingDetail.setMatingCount(respMatingCount.getResult());
        } else {
            throw new JsonResponseException(respMatingCount.getError());
        }
        Response<Date> respFirstMatingTime = doctorPigReadService.getFirstMatingTime(pigId, farmId);
        if (respMatingCount.isSuccess()) {
            doctorMatingDetail.setFirstMatingTime(respFirstMatingTime.getResult());
        } else {
            throw new JsonResponseException(respFirstMatingTime.getError());
        }
        return doctorMatingDetail;
    }

    /* 部署母猪流程
     * @return
        @ResponseBody
 */
    @RequestMapping(value = "/sow/flow/deploy", method = RequestMethod.GET)
    @ResponseBody
    public Boolean deploy() {
        return RespHelper.or500(doctorPigWriteService.deploy());
    }

    @RequestMapping(value = "/getFosterDetail", method = RequestMethod.GET)
    @ResponseBody
    public DoctorFosterDetail getFosterDetailByPigId (@RequestParam("pigId") Long pigId) {

        DoctorPigInfoDto doctorPigInfoDto = RespHelper.or500(doctorPigReadService.queryDoctorInfoDtoById(pigId));

        Map<String,Object> extraMap = doctorPigInfoDto.getExtraTrackMap();
        if (!extraMap.containsKey("farrowingPigletGroupId")) {
            throw new JsonResponseException(500, "not.exist.farrowing.pig.let.group.Id");
        }
        Long groupId = Long.valueOf(extraMap.get("farrowingPigletGroupId").toString());
        Response<DoctorGroupDetail> doctorGroupDetailResponse = doctorGroupReadService.findGroupDetailByGroupId(groupId);
        if (!doctorGroupDetailResponse.isSuccess()) {
            throw new JsonResponseException(500, doctorGroupDetailResponse.getError());
        }

        DoctorGroupTrack doctorGroupTrack = doctorGroupDetailResponse.getResult().getGroupTrack();
        DoctorFosterDetail doctorFosterDetail = new DoctorFosterDetail();
        doctorFosterDetail.setDoctorGroupTrack(doctorGroupTrack);
        doctorFosterDetail.setDoctorPigInfoDto(doctorPigInfoDto);
        return doctorFosterDetail;
    }

    private DoctorPigInfoDetailDto transFromType (DoctorPigInfoDetailDto doctorPigInfoDetailDto) {
        for (DoctorPigEvent doctorPigEvent : doctorPigInfoDetailDto.getDoctorPigEvents()) {
            Map<String,Object> extraMap = doctorPigEvent.getExtraMap();
            if (extraMap != null) {
                if (extraMap.get("matingType") != null) {
                    extraMap.put("matingType", MatingType.from((Integer) extraMap.get("matingType")).getDesc());
                }
                if (extraMap.get("checkResult") != null) {
                    extraMap.put("checkResult", PregCheckResult.from((Integer) extraMap.get("checkResult")).getDesc());
                }
                if (extraMap.get("farrowingType") != null) {
                    extraMap.put("farrowingType", FarrowingType.from((Integer) extraMap.get("farrowingType")).getDesc());
                }
                if (extraMap.get("farrowIsSingleManager") != null) {
                    extraMap.put("farrowIsSingleManager", ((Integer)extraMap.get("farrowIsSingleManager") == 1) ? IsOrNot.YES.getDesc() : IsOrNot.NO.getDesc());
                }
                if (extraMap.get("fosterReason") != null) {
                    extraMap.put("fosterReason", RespHelper.or500(doctorBasicReadService.findBasicById(Long.valueOf((String)extraMap.get("fosterReason")))).getName());
                }
                if (extraMap.get("vaccinationStaffId") != null) {
                    extraMap.put("vaccinationStaffName", RespHelper.or500(userProfileReadService.findProfileByUserId(Long.valueOf((Integer)extraMap.get("vaccinationStaffId")))).getRealName());
                }
            }
        }
        return doctorPigInfoDetailDto;
    }
}
