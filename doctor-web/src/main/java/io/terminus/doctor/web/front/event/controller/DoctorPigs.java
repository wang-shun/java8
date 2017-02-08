package io.terminus.doctor.web.front.event.controller;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.event.dto.msg.DoctorMessageSearchDto;
import io.terminus.doctor.event.dto.msg.DoctorPigMessage;
import io.terminus.doctor.event.dto.msg.RuleValue;
import io.terminus.doctor.event.enums.Category;
import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.event.service.DoctorMessageReadService;
import io.terminus.doctor.event.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
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
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

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
    private final DoctorFarmAuthCenter doctorFarmAuthCenter;

    @RpcConsumer
    private DoctorMessageReadService doctorMessageReadService;
    @RpcConsumer
    private DoctorMessageRuleTemplateReadService doctorMessageRuleTemplateReadService;
    @RpcConsumer
    private DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public DoctorPigs(DoctorPigReadService doctorPigReadService,
                      DoctorPigWriteService doctorPigWriteService,
                      DoctorGroupReadService doctorGroupReadService,
                      TransFromUtil transFromUtil,
                      DoctorFarmAuthCenter doctorFarmAuthCenter){
        this.doctorPigReadService = doctorPigReadService;
        this.doctorPigWriteService = doctorPigWriteService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.transFromUtil = transFromUtil;
        this.doctorFarmAuthCenter = doctorFarmAuthCenter;
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
     * 生成窝号
     */
    @RequestMapping(value = "/generate/fostersCode", method = RequestMethod.GET)
    @ResponseBody
    public String generestNest(@RequestParam("farmId") Long farmId,
                               @RequestParam(value = "eventAt", required = false) String eventAt,
                               @RequestParam(value = "size", required = false) Integer size) {
        return RespHelper.or500(doctorPigReadService.genNest(farmId, eventAt, size));
    }

    /**
     * pig id 获取 pig track 信息内容
     * @param pigId
     * @return
     */
    @RequestMapping(value = "/getPigInfoDto", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorPigInfoDto queryDoctorInfoDtoById(@RequestParam("pigId") Long pigId){
        DoctorPigInfoDto dto = RespHelper.or500(doctorPigReadService.queryDoctorInfoDtoById(pigId));
        doctorFarmAuthCenter.checkFarmAuthResponse(dto.getFarmId());
        return dto;
    }

    private DoctorPigInfoDetailDto getPigDetail(Long pigId, Integer eventSize) {
        Response<DoctorPigInfoDetailDto> response = doctorPigReadService.queryPigDetailInfoByPigId(pigId, eventSize);
        if (!response.isSuccess()) {
            return null;
        }
        DoctorPigInfoDetailDto pigDetail = response.getResult();
        doctorFarmAuthCenter.checkFarmAuthResponse(pigDetail.getDoctorPig().getFarmId());
        transFromUtil.transFromExtraMap(pigDetail.getDoctorPigEvents());
        return pigDetail;
    }

    @RequestMapping(value = "/getSowPigDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<DoctorSowDetailDto> querySowPigDetailInfoDto(@RequestParam("pigId") Long pigId,
                                                                 @RequestParam(value = "eventSize", required = false) Integer eventSize){
        try {
            DoctorPigInfoDetailDto pigDetail = getPigDetail(pigId, eventSize);
            if (pigDetail == null) {
                return Response.ok();
            }
            return Response.ok(buildSowDetailDto(pigDetail));
        } catch (JsonResponseException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("query.pigDetailInfo.fail");
        }
    }

    @RequestMapping(value = "/getBoarPigDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<DoctorBoarDetailDto> queryBoarPigDetailInfoDto(@RequestParam("pigId") Long pigId,
                                                                   @RequestParam(value = "eventSize", required = false) Integer eventSize){
        try {
            DoctorPigInfoDetailDto pigDetail = getPigDetail(pigId, eventSize);
            if (pigDetail == null) {
                return Response.ok();
            }
            return Response.ok(buildDoctorBoarDetailDto(pigDetail));
        } catch (JsonResponseException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            return Response.fail("query.pigDetailInfo.fail");
        }    }

    private DoctorBoarDetailDto buildDoctorBoarDetailDto(DoctorPigInfoDetailDto dto){
        return DoctorBoarDetailDto.builder()
                .pigBoarCode(dto.getDoctorPig().getPigCode()).breedName(dto.getDoctorPig().getBreedName())
                .barnCode(dto.getDoctorPigTrack().getCurrentBarnName()).pigStatus(dto.getDoctorPigTrack().getStatus())
                .entryDate(dto.getDoctorPig().getInFarmDate()).birthDate(dto.getDoctorPig().getBirthDate())
                .doctorPigEvents(dto.getDoctorPigEvents())
                .dayAge(dto.getDayAge())
                .weight(dto.getDoctorPigTrack().getWeight())
                .canRollback(dto.getCanRollback())
                .build();
    }

    private DoctorSowDetailDto buildSowDetailDto(DoctorPigInfoDetailDto dto){
        DoctorPigTrack doctorPigTrack = dto.getDoctorPigTrack();
        Integer pregCheckResult = null;
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
            String warnMessage = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().writeValueAsString(queryPigNotifyMessages(dto.getDoctorPig().getId()));
            return DoctorSowDetailDto.builder()
                    .pigSowCode(dto.getDoctorPig().getPigCode())
                    .warnMessage(warnMessage)
                    .breedName(dto.getDoctorPig().getBreedName()).barnCode(dto.getDoctorPigTrack().getCurrentBarnName())
                    .pigStatus(dto.getDoctorPigTrack().getStatus())
                    .dayAge(Days.daysBetween(new DateTime(dto.getDoctorPig().getBirthDate()), DateTime.now()).getDays() + 1)
                    .parity(dto.getDoctorPigTrack().getCurrentParity()).entryDate(dto.getDoctorPig().getInFarmDate())
                    .birthDate(dto.getDoctorPig().getBirthDate())
                    .doctorPigEvents(dto.getDoctorPigEvents())
                    .pregCheckResult(pregCheckResult)
                    .canRollback(dto.getCanRollback())
                    .build();
        } catch (Exception e) {
            log.error("buildSowDetailDto failed cause by {}", Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    /**
     * 获取猪只提示的消息
     * @param pigId 猪id
     * @return
     */
    @RequestMapping(value = "/notify/message", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorPigMessage> queryPigNotifyMessages(Long pigId) {
        DoctorMessageSearchDto doctorMessageSearchDto = new DoctorMessageSearchDto();
        doctorMessageSearchDto.setBusinessId(pigId);
        doctorMessageSearchDto.setBusinessType(DoctorMessage.BUSINESS_TYPE.PIG.getValue());
        List<DoctorMessage> messages =  RespHelper.or500(doctorMessageReadService.findMessageListByCriteria(doctorMessageSearchDto));

        if (notEmpty(messages)) {
            doctorFarmAuthCenter.checkFarmAuthResponse(messages.get(0).getFarmId());
        }

        List<DoctorPigMessage> doctorPigMessageList = Lists.newArrayList();
        Map<Integer, DoctorMessage> map = Maps.newHashMap();
        messages.forEach(doctorMessage -> map.put(doctorMessage.getCategory(), doctorMessage));
        map.values().forEach(doctorMessage -> {
            try {
                DoctorPigMessage pigMessage = DoctorPigMessage.builder()
                        .pigId(pigId)
                        .eventType(doctorMessage.getEventType())
                        .eventTypeName(doctorMessage.getEventType() == null ? null : PigEvent.from(doctorMessage.getEventType()).getName())
                        .timeDiff(doctorMessage.getRuleTimeDiff())
                        .build();
                if (Objects.equals(doctorMessage.getCategory(), Category.SOW_BACK_FAT.getKey())) {
                    DoctorMessageRuleTemplate doctorMessageRuleTemplate = RespHelper.or500(doctorMessageRuleTemplateReadService.findMessageRuleTemplateById(doctorMessage.getTemplateId()));
                    List<RuleValue> ruleValues = doctorMessageRuleTemplate.getRule().getValues().stream().filter(value -> Objects.equals(value.getId(), doctorMessage.getRuleValueId())).collect(Collectors.toList());
                    RuleValue ruleValue = ruleValues.get(0);
                    pigMessage.setMessageCategory(Category.SOW_BACK_FAT.getKey());
                    if (doctorMessage.getRuleValueId() == 4) {
                        pigMessage.setMessageDescribe("断奶");
                    } else {
                        pigMessage.setMessageDescribe(String.valueOf(ruleValue.getValue().intValue()));
                    }
                } else if (Objects.equals(doctorMessage.getCategory(), Category.SOW_BIRTHDATE.getKey())) {
                    pigMessage.setMessageCategory(Category.SOW_BIRTHDATE.getKey());
                    pigMessage.setMessageDescribe("怀孕天数");
                }
                doctorPigMessageList.add(pigMessage);
            } catch (Exception e) {
                log.error("json.analyze.failed");
            }
        });
        return doctorPigMessageList;
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

    @RequestMapping(value = "/getFosterDetail", method = RequestMethod.GET)
    @ResponseBody
    public DoctorFosterDetail getFosterDetailByPigId (@RequestParam("pigId") Long pigId) {

        DoctorPigInfoDto doctorPigInfoDto = RespHelper.or500(doctorPigReadService.queryDoctorInfoDtoById(pigId));

        doctorFarmAuthCenter.checkFarmAuthResponse(doctorPigInfoDto.getFarmId());

        DoctorPigTrack pigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
        if (pigTrack.getGroupId() == null) {
            throw new JsonResponseException(500, "not.exist.farrowing.pig.let.group.Id");
        }
        Response<DoctorGroupDetail> doctorGroupDetailResponse = doctorGroupReadService.findGroupDetailByGroupId(pigTrack.getGroupId());
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
            if(!Objects.equals(group.getPigType(), PigType.RESERVE.getValue())){
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

        doctorFarmAuthCenter.checkFarmAuthResponse(pigTrack.getFarmId());

        return RespHelper.or500(doctorBarnReadService.findBarnById(pigTrack.getCurrentBarnId()));
    }

    /**
     * 修改猪的耳号
     * @param pigId
     * @param code
     * @return
     */
    @RequestMapping(value = "/updateCode", method = RequestMethod.PUT)
    @ResponseBody
    public boolean updatePigCode(@RequestParam Long pigId, @RequestParam String code){
        DoctorPig pig = RespHelper.or500(doctorPigReadService.findPigById(pigId));
        if(pig == null){
            throw new JsonResponseException("pig.not.found");
        }
        if(Objects.equals(pig.getPigType(), DoctorPig.PigSex.BOAR.getKey())){
            throw new JsonResponseException("boar.code.forbid.update");
        }

        boolean notExist = RespHelper.or500(doctorPigReadService.validatePigCodeByFarmId(pig.getFarmId(), code));
        if(!notExist){
            throw new JsonResponseException("validate.pigCode.fail");
        }

        try{
            DoctorPigTrack track = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
            doctorFarmAuthCenter.checkBarnAuth(track.getCurrentBarnId());
        }catch(ServiceException e){
            throw new JsonResponseException(e.getMessage());
        }

        RespHelper.or500(doctorPigWriteService.updatePigCode(pigId, code));
        return true;
    }
}
