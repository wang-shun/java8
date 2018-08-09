package io.terminus.doctor.web.front.event.controller;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.RespWithExHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.msg.DoctorMessageSearchDto;
import io.terminus.doctor.event.dto.msg.DoctorPigMessage;
import io.terminus.doctor.event.dto.msg.RuleValue;
import io.terminus.doctor.event.enums.Category;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorChgFarmInfo;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.model.DoctorMessageRuleTemplate;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorMessageReadService;
import io.terminus.doctor.event.service.DoctorMessageRuleTemplateReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.service.DoctorPigWriteService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
import io.terminus.doctor.web.front.event.dto.DoctorBoarDetailDto;
import io.terminus.doctor.web.front.event.dto.DoctorFosterDetail;
import io.terminus.doctor.web.front.event.dto.DoctorMatingDetail;
import io.terminus.doctor.web.front.event.dto.DoctorSowDetailDto;
import io.terminus.doctor.web.util.TransFromUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
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

    private static final JsonMapper MAPPER = JsonMapper.nonEmptyMapper();

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
    @RpcConsumer
    private DoctorPigEventReadService doctorPigEventReadService;

    @Autowired
    private Exporter exporter;

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

    private DoctorPigInfoDetailDto getPigDetail(Long farmId, Long pigId, Integer eventSize) {
        DoctorPigInfoDetailDto pigDetail = RespWithExHelper.orInvalid(doctorPigReadService.queryPigDetailInfoByPigId(farmId, pigId, eventSize));

        doctorFarmAuthCenter.checkFarmAuthResponse(pigDetail.getDoctorPig().getFarmId());
        transFromUtil.transFromExtraMap(pigDetail.getDoctorPigEvents());
        return pigDetail;
    }

    @RequestMapping(value = "/getSowPigDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorSowDetailDto querySowPigDetailInfoDto(@RequestParam("farmId") Long farmId,
                                                       @RequestParam("pigId") Long pigId,
                                                       @RequestParam(value = "eventSize", required = false) Integer eventSize){

            return buildSowDetailDto(getPigDetail(farmId, pigId, eventSize));
    }

    /**
     * 与母猪详情导出配合用的
     * @param farmId
     * @param pigId
     * @param eventSize
     * @return
     */
    private DoctorPigInfoDetailDto getPigDetail2(Long farmId, Long pigId, Integer eventSize) {
        DoctorPigInfoDetailDto pigDetail = RespWithExHelper.orInvalid(doctorPigReadService.queryPigDetailInfoByPigId(farmId, pigId, eventSize));
        return pigDetail;

    }

    /**
     * 母猪详情导出
     * @param farmId
     * @param pigId
     * @param eventSize
     */
    @RequestMapping(value = "/getSowPigDetail/export", method = RequestMethod.GET)
    public void querySowPigDetailInfoDtoExpotr(@RequestParam("farmId") Long farmId,
                                               @RequestParam("pigId") Long pigId,
                                               @RequestParam(value = "eventSize", required = false) Integer eventSize,
                                               HttpServletRequest request, HttpServletResponse response){

        DoctorSowDetailDto doctorSowDetailDto = buildSowDetailDto(getPigDetail2(farmId, pigId, eventSize));

        //开始导出
        try{
            //导出名称
            exporter.setHttpServletResponse(request,response,"母猪详情导出");
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                //表
                Sheet sheet = workbook.createSheet();
                sheet.createRow(0).createCell(5).setCellValue("母猪详情");

                //
                Row row2 = sheet.createRow(2);
                row2.createCell(0).setCellValue("猪号");
                row2.createCell(1).setCellValue("猪只RFID");
                row2.createCell(2).setCellValue("母猪状态");
                row2.createCell(3).setCellValue("胎次");
                row2.createCell(4).setCellValue("品种");
                row2.createCell(5).setCellValue("舍号");
                row2.createCell(6).setCellValue("体重-kg");
                row2.createCell(7).setCellValue("进场日期");
                row2.createCell(8).setCellValue("出生日期");
                row2.createCell(9).setCellValue("状态天数");

                Row row3 = sheet.createRow(3);
                row3.createCell(0).setCellValue(String.valueOf(doctorSowDetailDto.getPigSowCode()));
                String rfid=String.valueOf(doctorSowDetailDto.getRfid());
                if(rfid.equals("null")){
                    rfid="";
                }
                row3.createCell(1).setCellValue(rfid);
                String pigStatus = String.valueOf(doctorSowDetailDto.getPigStatus());
                if(pigStatus.equals(String.valueOf(PigStatus.Entry.getKey()))){
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.Entry.getName()));
                }if (pigStatus.equals(String.valueOf(PigStatus.Removal.getKey()))) {
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.Removal.getName()));
                }
                if (pigStatus.equals(String.valueOf(PigStatus.Mate.getKey()))) {
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.Mate.getName()));
                }
                if (pigStatus.equals(String.valueOf(PigStatus.Pregnancy.getKey()))) {
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.Pregnancy.getName()));
                }
                if (pigStatus.equals(String.valueOf(PigStatus.KongHuai.getKey()))) {
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.KongHuai.getName()));
                }
                if (pigStatus.equals(String.valueOf(PigStatus.Farrow.getKey()))) {
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.Farrow.getName()));
                }
                if (pigStatus.equals(String.valueOf(PigStatus.FEED.getKey()))) {
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.FEED.getName()));
                }
                if (pigStatus.equals(String.valueOf(PigStatus.Wean.getKey()))) {
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.Wean.getName()));
                }
                if (pigStatus.equals(String.valueOf(PigStatus.CHG_FARM.getKey()))) {
                    row3.createCell(2).setCellValue(String.valueOf(PigStatus.CHG_FARM.getName()));
                }
                row3.createCell(3).setCellValue(String.valueOf(doctorSowDetailDto.getParity()));
                row3.createCell(4).setCellValue(String.valueOf(doctorSowDetailDto.getBreedName()));
                row3.createCell(5).setCellValue(String.valueOf(doctorSowDetailDto.getBarnCode()));
                String pigWeight=String.valueOf(doctorSowDetailDto.getPigWeight());
                if(pigWeight.equals("null")){
                    pigWeight="";
                }
                row3.createCell(6).setCellValue(pigWeight);
                //date类型的转yyyy年MM月dd日格式
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String format = sdf.format(doctorSowDetailDto.getEntryDate());
                row3.createCell(7).setCellValue(String.valueOf(format));

                //date类型的转yyyy年MM月dd日格式
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
                String format1 = sdf1.format(doctorSowDetailDto.getBirthDate());
                row3.createCell(8).setCellValue(String.valueOf(format1));

                row3.createCell(9).setCellValue(String.valueOf(doctorSowDetailDto.getStatusDay()));

                Row row4 = sheet.createRow(5);
                row4.createCell(0).setCellValue("历史事件");

                Row row5 = sheet.createRow(6);
                row5.createCell(0).setCellValue("胎次");
                row5.createCell(1).setCellValue("事件名称");
                row5.createCell(2).setCellValue("事件时间");
                row5.createCell(3).setCellValue("事件描述");
                row5.createCell(4).setCellValue("所属猪场");
                row5.createCell(5).setCellValue("所属猪舍");


                int addRow=7;
                for (DoctorPigEvent s: doctorSowDetailDto.getDoctorPigEvents()){
                    Row rowadd = sheet.createRow(addRow++);
                    rowadd.createCell(0).setCellValue(String.valueOf(s.getParity()));
                    rowadd.createCell(1).setCellValue(String.valueOf(s.getName()));

                    //date类型的转yyyy年MM月dd日格式
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                    String format2 = sdf2.format(s.getEventAt());
                    rowadd.createCell(2).setCellValue(String.valueOf(format2));

                    rowadd.createCell(3).setCellValue(String.valueOf(s.getDesc()));
                    rowadd.createCell(4).setCellValue(String.valueOf(s.getFarmName()));
                    rowadd.createCell(5).setCellValue(String.valueOf(s.getBarnName()));
                }

                workbook.write(response.getOutputStream());
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @RequestMapping(value = "/getBoarPigDetail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorBoarDetailDto queryBoarPigDetailInfoDto(@RequestParam("farmId") Long farmId,
                                                         @RequestParam("pigId") Long pigId,
                                                         @RequestParam(value = "eventSize", required = false) Integer eventSize){
            return buildDoctorBoarDetailDto(getPigDetail(farmId, pigId, eventSize));
          }

    private DoctorBoarDetailDto buildDoctorBoarDetailDto(DoctorPigInfoDetailDto dto){
        return DoctorBoarDetailDto.builder()
                .pigBoarCode(dto.getDoctorPig().getPigCode()).breedName(dto.getDoctorPig().getBreedName())
                .barnCode(dto.getDoctorPigTrack().getCurrentBarnName()).pigStatus(dto.getDoctorPigTrack().getStatus())
                .entryDate(dto.getDoctorPig().getInFarmDate()).birthDate(dto.getDoctorPig().getBirthDate())
                .doctorPigEvents(dto.getDoctorPigEvents())
                .dayAge(dto.getDayAge())
                .weight(dto.getDoctorPigTrack().getWeight())
                .boarType(dto.getDoctorPig().getBoarType())
                .rfid(dto.getDoctorPig().getRfid())
                .build();
    }

    private DoctorSowDetailDto buildSowDetailDto(DoctorPigInfoDetailDto dto){
        DoctorPigTrack doctorPigTrack = dto.getDoctorPigTrack();
        Integer pregCheckResult = null;
        try{
            String warnMessage = null;
            Integer statusDay;
            if (Objects.equals(dto.getIsChgFarm(), true)) {
                DoctorChgFarmInfo doctorChgFarmInfo = RespHelper.or500(doctorPigReadService.findByFarmIdAndPigId(doctorPigTrack.getFarmId(), doctorPigTrack.getPigId()));
                DoctorPigEvent chgFarm = RespHelper.or500(doctorPigEventReadService.findById(doctorChgFarmInfo.getEventId()));
                statusDay = DateUtil.getDeltaDays(chgFarm.getEventAt(), new Date()) + 1;
            } else {
                String extra = doctorPigTrack.getExtra();
                Integer status = dto.getDoctorPigTrack().getStatus();
                Date eventAt = null;
                Long pigId = dto.getDoctorPig().getId();
                // 最近一次的配种时间
                if (status == 4 || status == 7){
                    eventAt = RespHelper.or500(doctorPigEventReadService.findMateEventToPigId(pigId));
                } else {
                    eventAt = RespHelper.or500(doctorPigEventReadService.findEventAtLeadToStatus(pigId, status));
                }
//                Date eventAt = RespHelper.or500(doctorPigEventReadService.findEventAtLeadToStatus(dto.getDoctorPig().getId()
//                        , dto.getDoctorPigTrack().getStatus()));
                statusDay = DateUtil.getDeltaDays(eventAt, new Date()) + 1;

                if (doctorPigTrack.getStatus() == PigStatus.KongHuai.getKey() && StringUtils.isNotBlank(extra)) {
                    Map<String, Object> extraMap = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(extra, JacksonType.MAP_OF_OBJECT);
                    Object checkResult = extraMap.get("pregCheckResult");
                    if (checkResult != null) {
                        pregCheckResult = Integer.parseInt(checkResult.toString());
                        doctorPigTrack.setStatus(pregCheckResult);
                    }
                }
                warnMessage = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().writeValueAsString(queryPigNotifyMessages(dto.getDoctorPig().getId()));
            }
            return DoctorSowDetailDto.builder()
                    .pigSowCode(dto.getDoctorPig().getPigCode())
                    .warnMessage(warnMessage)
                    .breedName(dto.getDoctorPig().getBreedName())
                    .barnId(dto.getDoctorPigTrack().getCurrentBarnId())
                    .barnCode(dto.getDoctorPigTrack().getCurrentBarnName())
                    .pigStatus(dto.getDoctorPigTrack().getStatus())
                    .dayAge(Days.daysBetween(new DateTime(dto.getDoctorPig().getBirthDate()), DateTime.now()).getDays() + 1)
                    .parity(doctorPigTrack.getCurrentParity())
                    .entryDate(dto.getDoctorPig().getInFarmDate())
                    .birthDate(dto.getDoctorPig().getBirthDate())
                    .doctorPigEvents(dto.getDoctorPigEvents())
                    .pregCheckResult(pregCheckResult)
                    .rfid(dto.getDoctorPig().getRfid())
                    .statusDay(statusDay)
                    .pigWeight(dto.getDoctorPigTrack().getWeight())
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
                    pigMessage.setTimeDiff(doctorMessage.getTimeDiff());
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
     */
    @RequestMapping(value = "/updateCodes", method = RequestMethod.GET)
    @ResponseBody
    public boolean updatePigCodes(@RequestParam String pigCodeUpdates) {
        List<DoctorPig> pigs = MAPPER.fromJson(pigCodeUpdates, MAPPER.createCollectionType(List.class, DoctorPig.class));
        return RespHelper.or500(doctorPigWriteService.updatePigCodes(pigs));
    }

}
