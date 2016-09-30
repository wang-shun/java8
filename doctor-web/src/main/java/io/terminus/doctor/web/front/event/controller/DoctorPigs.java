package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.DoctorPigMessage;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.web.front.event.dto.DoctorBoarDetailDto;
import io.terminus.doctor.web.front.event.dto.DoctorFosterDetail;
import io.terminus.doctor.web.front.event.dto.DoctorMatingDetail;
import io.terminus.doctor.web.front.event.dto.DoctorSowDetailDto;
import io.terminus.doctor.web.util.TransFromUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Objects;

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
    private final TransFromUtil transFromUtil;

    @RpcConsumer
    private  DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public DoctorPigs(DoctorPigReadService doctorPigReadService, DoctorPigWriteService doctorPigWriteService, DoctorGroupReadService doctorGroupReadService, TransFromUtil transFromUtil){
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigWriteService = doctorPigWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.transFromUtil = transFromUtil;
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
        transFromUtil.transFromExtraMap(doctorPigInfoDetailDto.getDoctorPigEvents());
        return doctorPigInfoDetailDto;
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
                .dayAge(dto.getDayAge())
                .weight(dto.getDoctorPigTrack().getWeight())
                .canRollback(dto.getCanRollback())
                .build();
        return doctorBoarDetailDto;
    }

    private DoctorSowDetailDto buildSowDetailDto(DoctorPigInfoDetailDto dto){
        DoctorPigTrack doctorPigTrack = dto.getDoctorPigTrack();
        Integer pregCheckResult =null;
        try{
            String extra = doctorPigTrack.getExtra();
            if (doctorPigTrack.getStatus() == PigStatus.KongHuai.getKey() && StringUtils.isNotBlank(extra)){
                Map<String, Object> extraMap = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(extra, JacksonType.MAP_OF_OBJECT);
                Object checkResult = extraMap.get("pregCheckResult");
                if (checkResult != null) {
                    pregCheckResult = Integer.parseInt(checkResult.toString());
                    doctorPigTrack.setStatus(pregCheckResult);
                }
            }
        }catch (Exception e){
            log.error("buildSowDetailDto failed cause by {}", Throwables.getStackTraceAsString(e));
        }

        DoctorSowDetailDto doctorSowDetailDto = DoctorSowDetailDto.builder()
                .pigSowCode(dto.getDoctorPig().getPigCode())
                .warnMessage(dto.getDoctorPigTrack().getExtraMessage())
                .breedName(dto.getDoctorPig().getBreedName()).barnCode(dto.getDoctorPigTrack().getCurrentBarnName())
                .pigStatus(dto.getDoctorPigTrack().getStatus())
                .dayAge(Days.daysBetween(new DateTime(dto.getDoctorPig().getBirthDate()), DateTime.now()).getDays() + 1)
                .parity(dto.getDoctorPigTrack().getCurrentParity()).entryDate(dto.getDoctorPig().getInFarmDate())
                .birthDate(dto.getDoctorPig().getBirthDate())
                .doctorPigEvents(dto.getDoctorPigEvents())
                .pregCheckResult(pregCheckResult)
                .canRollback(dto.getCanRollback())
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

    /**
     * 帮助前台判断参数中的猪群是否都是后备群
     * @param groupIds 猪群id
     * @return
     */
    @RequestMapping(value = "/checkGroupReserve", method = RequestMethod.POST)
    @ResponseBody
    public boolean checkGroupReserve(@RequestParam("groupIds") List<Long> groupIds){
        if(groupIds.isEmpty()){
            return false;
        }
        List<DoctorGroup> list = RespHelper.or500(doctorGroupReadService.findGroupByIds(groupIds));
        if(list.isEmpty()){
            return false;
        }
        for(DoctorGroup group : list){
            if(!Objects.equals(group.getPigType(), PigType.RESERVE_BOAR.getValue()) && !Objects.equals(group.getPigType(), PigType.RESERVE_SOW.getValue())){
                return false;
            }
        }
        return true;
    }

    /**
     * 根据猪id查询当前猪舍
     *
     * @param pigId 猪id
     * @return 猪舍
     */
    @RequestMapping(value = "/currentBarn", method = RequestMethod.GET)
    @ResponseBody
    public DoctorBarn findCurrentBarnByPigId(@RequestParam("pigId") Long pigId){
        DoctorPigTrack pigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
        return RespHelper.or500(doctorBarnReadService.findBarnById(pigTrack.getCurrentBarnId()));
    }
}
