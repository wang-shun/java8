package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.RespWithExHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorEventModifyRequestReadService;
import io.terminus.doctor.event.service.DoctorEventModifyRequestWriteService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
import io.terminus.doctor.web.front.event.dto.DoctorBatchGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorBatchNewGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorGroupDetailEventsDto;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.util.TransFromUtil;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/31
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/events/group")
public class DoctorGroupEvents {

    private final DoctorGroupWebService doctorGroupWebService;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorFarmAuthCenter doctorFarmAuthCenter;
    private final DoctorGroupWriteService doctorGroupWriteService;
    private final DoctorBasicReadService doctorBasicReadService;
    private final TransFromUtil transFromUtil;

    @RpcConsumer
    private DoctorPigReadService doctorPigReadService;
    @RpcConsumer
    private DoctorEventModifyRequestWriteService doctorEventModifyRequestWriteService;
    @RpcConsumer
    private DoctorEventModifyRequestReadService doctorEventModifyRequestReadService;

    @Autowired
    private Exporter exporter;

    @Autowired
    public DoctorGroupEvents(DoctorGroupWebService doctorGroupWebService,
                             DoctorGroupReadService doctorGroupReadService,
                             DoctorFarmAuthCenter doctorFarmAuthCenter,
                             DoctorGroupWriteService doctorGroupWriteService,
                             DoctorBasicReadService doctorBasicReadService, TransFromUtil transFromUtil
                             ) {
        this.doctorGroupWebService = doctorGroupWebService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorFarmAuthCenter = doctorFarmAuthCenter;
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.transFromUtil = transFromUtil;
    }

    /**
     * 校验猪群号是否重复
     *
     * @param farmId    猪场id
     * @param groupCode 猪群号
     * @return true 重复, false 不重复
     */
    @RequestMapping(value = "/check/groupCode", method = RequestMethod.GET)
    public Boolean checkGroupRepeat(@RequestParam("farmId") Long farmId,
                                    @RequestParam("groupCode") String groupCode) {
        return RespHelper.or500(doctorGroupReadService.checkGroupRepeat(farmId, groupCode));
    }

    /**
     * 新建猪群
     *
     * @return 猪群id
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createNewGroup(@RequestBody DoctorNewGroupInput newGroupDto) {
        return RespWithExHelper.orInvalid(doctorGroupWebService.createNewGroup(newGroupDto));
    }

    /**
     * 批量新建猪群
     *
     * @param batchNewGroupEventDto 批量新建信息
     * @return
     */
    @RequestMapping(value = "/batchNew", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean batchCreateNewGroup(@RequestBody DoctorBatchNewGroupEventDto batchNewGroupEventDto) {
        return RespWithExHelper.orInvalid(doctorGroupWebService.batchNewGroupEvent(batchNewGroupEventDto));
    }

    /**
     * 录入猪群事件
     *
     * @param groupId   猪群id
     * @param eventType 事件类型
     * @param data      入参
     * @return 是否成功
     * @see io.terminus.doctor.event.enums.GroupEventType
     * @see io.terminus.doctor.event.dto.event.group.input.BaseGroupInput
     */
    @RequestMapping(value = "/other", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createGroupEvent(@RequestParam("groupId") Long groupId,
                                    @RequestParam("eventType") Integer eventType,
                                    @RequestParam("data") String data) {
        return RespWithExHelper.orInvalid(doctorGroupWebService.createGroupEvent(groupId, eventType, data));
    }

    /**
     * 创建猪群编辑请求
     * @param groupId 猪群id
     * @param eventType 事件类型
     * @param eventId 事件id
     * @param data 输入数据
     * @return
     */
    @RequestMapping(value = "/createGroupModifyRequest", method = RequestMethod.POST)
    public void createGroupModifyEventRequest(@RequestParam("groupId") Long groupId,
                                                 @RequestParam("eventType") Integer eventType,
                                              @RequestParam("eventId") Long eventId,
                                              @RequestParam("data") String data) {
        RespWithExHelper.orInvalid(doctorGroupWebService.createGroupModifyEventRequest(groupId, eventType, eventId, data));
    }

    /**
     * 批量猪群事件
     *
     * @param batchGroupEventDto 批量事件输入封装
     * @return 是否成功
     */
    @RequestMapping(value = "/batchOther", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean batchCreateGroupEvent(@RequestBody DoctorBatchGroupEventDto batchGroupEventDto) {
        return RespWithExHelper.orInvalid(doctorGroupWebService.batchGroupEvent(batchGroupEventDto));
    }

    /**
     * 根据猪群id查询可以操作的事件类型
     *
     * @param groupIds 猪群ids
     * @return 事件类型s
     * @see io.terminus.doctor.event.enums.GroupEventType
     */
    @RequestMapping(value = "/types", method = RequestMethod.POST)
    public List<Integer> findEventTypesByGroupIds(@RequestParam("groupIds[]") Long[] groupIds) {
        return RespHelper.or500(doctorGroupReadService.findEventTypesByGroupIds(Lists.newArrayList(groupIds)));
    }

    /**
     * 生成猪群号 猪舍名(yyyy-MM-dd)
     *
     * @param barnName 猪舍名称
     * @return 猪群号
     */
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    public String generateGroupCode(@RequestParam(value = "barnName", required = false) String barnName) {
        return doctorGroupWebService.generateGroupCode(barnName).getResult();
    }

    /**
     * 根据id生成猪群号(主要用于分娩舍: 如果当前猪舍存在猪群直接返回此猪群号, 如果不存在, 新生成猪群号
     *
     * @param pigId 猪id
     * @return 猪群号
     */
    @RequestMapping(value = "/pigCode", method = RequestMethod.GET)
    public String generateGroupCodeByPigId(@RequestParam(value = "pigId", required = false) Long pigId, @RequestParam String eventAt) {
        if (pigId == null) {
            return null;
        }
        DoctorPigTrack pigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
        List<DoctorGroup> groupList = RespHelper.or500(doctorGroupReadService.findGroupByCurrentBarnId(pigTrack.getCurrentBarnId()));
        if (Arguments.isNullOrEmpty(groupList)) {
            return pigTrack.getCurrentBarnName() + "(" + eventAt + ")";
        }
        return groupList.get(0).getGroupCode();
    }

    /**
     * 猪所在猪舍是否有猪群
     * @param pigId 猪id
     * @return
     */
    @RequestMapping(value = "/has/group/{pigId}", method = RequestMethod.GET)
    public Boolean hasGroup(@PathVariable Long pigId) {
        DoctorPigTrack pigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
        List<DoctorGroup> groupList = RespHelper.or500(doctorGroupReadService.findGroupByCurrentBarnId(pigTrack.getCurrentBarnId()));
        if (Arguments.isNullOrEmpty(groupList)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    /**
     * 查询猪群详情
     *
     * @param groupId   猪群id
     * @param eventSize 事件大小
     * @return 猪群详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public DoctorGroupDetailEventsDto findGroupDetailByGroupId(@RequestParam("groupId") Long groupId,
                                                               @RequestParam(value = "eventSize", required = false) Integer eventSize) {
        DoctorGroupDetail groupDetail = RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId));

        //查询猪群的事件, 默认3条
        List<DoctorGroupEvent> groupEvents = RespHelper.or500(doctorGroupReadService.pagingGroupEventDelWean(
                groupDetail.getGroup().getFarmId(), groupId, null, null, MoreObjects.firstNonNull(eventSize, 3), null, null)).getData();

        Response<DoctorGroupEvent> response = doctorGroupReadService.findLastGroupEventByType(groupId, GroupEventType.LIVE_STOCK.getValue());
        Double avgWeight = 0.0;
        if (response.isSuccess() && response.getResult() != null) {
            avgWeight = response.getResult().getAvgWeight();
        }
        return new DoctorGroupDetailEventsDto(groupDetail.getGroup(), groupDetail.getGroupTrack()
                , transFromUtil.transFromGroupEvents(groupEvents), avgWeight);
    }

    /**
     * 猪群详情导出
     * @param groupId
     * @param eventSize
     */
    @RequestMapping(value = "/detail/export", method = RequestMethod.GET)
    public void findGroupDetailByGroupIdExport(@RequestParam("groupId") Long groupId,
                                               @RequestParam(value = "eventSize", required = false) Integer eventSize,
                                               HttpServletRequest request, HttpServletResponse res){
        DoctorGroupDetail groupDetail = RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId));
        //计算平均体重
        Response<DoctorGroupEvent> response = doctorGroupReadService.findLastGroupEventByType(groupId, GroupEventType.LIVE_STOCK.getValue());
        Double avgWeight = 0.0;
        if (response.isSuccess() && response.getResult() != null) {
            avgWeight = response.getResult().getAvgWeight();
        }


        //开始导出
        try {
            //导出名称
            exporter.setHttpServletResponse(request,res,"猪群详情");
            XSSFWorkbook workbook = new XSSFWorkbook();
            //表
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(5).setCellValue("猪群详情");

            Row title = sheet.createRow(1);
//            int pos = 2;

            title.createCell(0).setCellValue("猪群号");
            title.createCell(1).setCellValue("猪群种类");
            title.createCell(2).setCellValue("猪场");
            title.createCell(3).setCellValue("猪只数");
            title.createCell(4).setCellValue("平均日龄");
            title.createCell(5).setCellValue("平均体重");
            title.createCell(6).setCellValue("建群日期");
            title.createCell(7).setCellValue("状态");
            title.createCell(8).setCellValue("饲养员");

            Row row = sheet.createRow(3);
            row.createCell(0).setCellValue(String.valueOf(groupDetail.getGroup().getGroupCode()));

            //枚举类型
            String a=String.valueOf(groupDetail.getGroup().getPigType());
            if(a.equals(String.valueOf(PigType.NURSERY_PIGLET.getValue()))){
                row.createCell(1).setCellValue(String.valueOf(PigType.NURSERY_PIGLET.getDesc()));
            }
            if(a.equals(String.valueOf(PigType.FATTEN_PIG.getValue()))){
                row.createCell(1).setCellValue(String.valueOf(PigType.FATTEN_PIG.getDesc()));
            }
            if(a.equals(String.valueOf(PigType.RESERVE.getValue()))){
                row.createCell(1).setCellValue(String.valueOf(PigType.RESERVE.getDesc()));
            }
            if(a.equals(String.valueOf(PigType.MATE_SOW.getValue()))){
                row.createCell(1).setCellValue(String.valueOf(PigType.MATE_SOW.getDesc()));
            }
            if(a.equals(String.valueOf(PigType.PREG_SOW.getValue()))){
                row.createCell(1).setCellValue(String.valueOf(PigType.PREG_SOW.getDesc()));
            }
            if(a.equals(String.valueOf(PigType.DELIVER_SOW.getValue()))){
                row.createCell(1).setCellValue(String.valueOf(PigType.DELIVER_SOW.getDesc()));
            }
            if(a.equals(String.valueOf(PigType.BOAR.getValue()))){
                row.createCell(1).setCellValue(String.valueOf(PigType.BOAR.getDesc()));
            }

            row.createCell(2).setCellValue(String.valueOf(groupDetail.getGroup().getFarmName()));
            row.createCell(3).setCellValue(String.valueOf(groupDetail.getGroupTrack().getQuantity()));
            row.createCell(4).setCellValue(String.valueOf(groupDetail.getGroupTrack().getAvgDayAge()));
            row.createCell(5).setCellValue(String.valueOf(avgWeight+" 公斤"));

            //date类型的转yyyy年MM月dd日格式
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(groupDetail.getGroup().getOpenAt());
            row.createCell(6).setCellValue(String.valueOf(format));

            //枚举类型
            String b=String.valueOf(groupDetail.getGroup().getStatus());
            if(b.equals(DoctorGroup.Status.CREATED.getValue())){
                row.createCell(7).setCellValue(DoctorGroup.Status.CREATED.getDesc());
            }
            if(b.equals(DoctorGroup.Status.CLOSED.getValue())){
                row.createCell(7).setCellValue(DoctorGroup.Status.CLOSED.getDesc());
            }

            row.createCell(8).setCellValue(String.valueOf(groupDetail.getGroup().getStaffName()));

            workbook.write(res.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 分页查询猪群历史事件
     *
     * @param farmId  猪场id
     * @param groupId 猪群id
     * @param type    事件类型
     * @param pageNo  分页大小
     * @param size    当前页码
     * @return 分页结果
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public Paging<DoctorGroupEvent> pagingGroupEvent(@RequestParam("farmId") Long farmId,
                                                     @RequestParam(value = "groupId", required = false) Long groupId,
                                                     @RequestParam(value = "type", required = false) Integer type,
                                                     @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                     @RequestParam(value = "size", required = false) Integer size,
                                                     @RequestParam(value = "startDate", required = false) String startDate,
                                                     @RequestParam(value = "endDate", required = false) String endDate) {

        Paging<DoctorGroupEvent> doctorGroupEventPaging = RespHelper.or500(doctorGroupReadService.pagingGroupEvent(farmId, groupId, type, pageNo, size, startDate, endDate));
        transFromUtil.transFromGroupEvents(doctorGroupEventPaging.getData());
        return doctorGroupEventPaging;
    }

    @RequestMapping(value = "/pagingRollbackGroupEvent", method = RequestMethod.GET)
    public Paging<DoctorGroupEvent> pagingGroupEventWithCanRollback(@RequestParam("farmId") Long farmId,
                                                                     @RequestParam(value = "groupId", required = false) Long groupId,
                                                                     @RequestParam(value = "type", required = false) Integer type,
                                                                     @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                                     @RequestParam(value = "size", required = false) Integer size,
                                                                     @RequestParam(value = "startDate", required = false) String startDate,
                                                                     @RequestParam(value = "endDate", required = false) String endDate) {
        return pagingGroupEvent(farmId, groupId, type, pageNo, size, startDate, endDate);
    }

    /**
     * 查询猪群事件详情
     *
     * @param eventId 事件id
     * @return 猪群事件
     */
    @RequestMapping(value = "/event", method = RequestMethod.GET)
    public DoctorGroupEvent findGroupEventById(@RequestParam("eventId") Long eventId) {
        return RespHelper.or500(doctorGroupReadService.findGroupEventById(eventId));
    }

    /**
     * 查询已建群的猪群
     *
     * @param farmId 猪场id
     * @return 猪群
     */
    @RequestMapping(value = "/open", method = RequestMethod.GET)
    public List<DoctorGroup> findOpenGroupsByFarmId(@RequestParam("farmId") Long farmId) {
        return RespHelper.or500(doctorGroupReadService.findGroupsByFarmId(farmId)).stream()
                .filter(group -> Objects.equals(DoctorGroup.Status.CREATED.getValue(), group.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 根据猪舍id查询已建群的猪群
     *
     * @param barnId 猪舍id
     * @return 猪群
     */
    @RequestMapping(value = "/open/barn", method = RequestMethod.GET)
    public List<DoctorGroup> findOpenGroupsByBarnId(@RequestParam(value = "barnId", required = false) Long barnId) {
        if (barnId == null) {
            return Lists.newArrayList();
        }
        return RespHelper.or500(doctorGroupReadService.findGroupByCurrentBarnId(barnId)).stream()
                .filter(group -> Objects.equals(DoctorGroup.Status.CREATED.getValue(), group.getStatus()))
                .collect(Collectors.toList());
    }

    /**
     * 回滚猪群事件
     *
     * @param eventId 回滚事件的id
     * @return 猪群镜像
     */
    @RequestMapping(value = "/rollback", method = RequestMethod.GET)
    public Boolean rollbackGroupEvent(@RequestParam("eventId") Long eventId) {
        DoctorGroupEvent event = RespHelper.or500(doctorGroupReadService.findGroupEventById(eventId));

        //权限中心校验权限
        doctorFarmAuthCenter.checkFarmAuth(event.getFarmId());
        return RespHelper.or500(doctorGroupWriteService.rollbackGroupEvent(event, UserUtil.getUserId(), UserUtil.getCurrentUser().getName()));
    }

    /**
     * 查询可以转入的品种
     *
     * @param groupId 猪群id
     * @return 可转入品种
     */
    @RequestMapping(value = "/breeds", method = RequestMethod.GET)
    public List<DoctorBasic> findCanBreed(@RequestParam("groupId") Long groupId) {
        DoctorGroup group = RespHelper.or500(doctorGroupReadService.findGroupById(groupId));
        if (group.getBreedId() != null) {
            DoctorBasic breed = RespHelper.or500(doctorBasicReadService.findBasicById(group.getBreedId()));
            if (breed != null) {
                return Lists.newArrayList(breed);
            }
        }
        return RespHelper.or500(doctorBasicReadService.findValidBasicByTypeAndSrm(DoctorBasic.Type.BREED.getValue(), null));
    }

    /**
     * 根据猪场id和猪群号查询猪群
     *
     * @param farmId    猪场id
     * @param groupCode 猪群号
     * @return 猪群
     */
    @RequestMapping(value = "/farmGroupCode", method = RequestMethod.GET)
    public DoctorGroup findGroupByFarmIdAndGroupCode(@RequestParam("farmId") Long farmId,
                                                     @RequestParam("groupCode") String groupCode) {
        return RespHelper.or500(doctorGroupReadService.findGroupByFarmIdAndGroupCode(farmId, groupCode));
    }

    /**
     * 获取猪群事件类型列表
     *
     * @return
     * @see GroupEventType
     */
    @RequestMapping(value = "/groupEvents")
    @ResponseBody
    public List<String> queryGroupEvents() {
        return Arrays.stream(GroupEventType.values()).map(GroupEventType::getDesc).collect(Collectors.toList());
    }

    /**
     * 分页查询某一类型的猪群事件
     *
     * @param params
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/groupPaging", method = RequestMethod.GET)
    @ResponseBody
    public Paging<DoctorGroupEvent> queryGroupEventsByCriteria(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }
        if (Objects.isNull(params.get("endDate"))) {
            params.put("endDate", new DateTime(params.get("endDate")).withTimeAtStartOfDay().plusSeconds(86399).toDate());
        }
        return RespHelper.or500(doctorGroupReadService.queryGroupEventsByCriteria(params, pageNo, pageSize));
    }

    /**
     * 获取猪群初始事件
     *
     * @param groupId 猪群id
     * @return 新建事件
     */
    @RequestMapping(value = "/find/newGroupEvent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorGroupEvent findNewGroupEvent(@RequestParam Long groupId) {
        return RespHelper.or500(doctorGroupReadService.findInitGroupEvent(groupId));
    }

    /**
     * 猪群转群事件的extra中添加groupid(暂时)
     *
     * @return
     */
    @RequestMapping(value = "/fix/groupExtra", method = RequestMethod.GET)
    public Boolean fixGroupEventExtra() {
        try {
            Map<String, Object> map = Maps.newHashMap();
            map.put("type", GroupEventType.NEW.getValue());
            map.put("isAuto", IsOrNot.YES.getValue());
            Paging<DoctorGroupEvent> paging = RespHelper.or500(doctorGroupReadService.queryGroupEventsByCriteria(map, 1, Integer.MAX_VALUE));
            if (paging.isEmpty()) {
                return Boolean.TRUE;
            }
            paging.getData().forEach(doctorGroupEvent -> {
                try {
                    DoctorGroupEvent relEvent = RespHelper.or500(doctorGroupReadService.findGroupEventById(doctorGroupEvent.getRelGroupEventId()));
                    if (Objects.equals(relEvent.getType(), GroupEventType.TRANS_GROUP.getValue())) {
                        DoctorTransGroupInput doctorTransGroupEvent = JsonMapper.JSON_NON_EMPTY_MAPPER.fromJson(relEvent.getExtra(), DoctorTransGroupInput.class);
                        doctorTransGroupEvent.setToGroupId(doctorGroupEvent.getGroupId());
                        relEvent.setExtraMap(doctorTransGroupEvent);
                        doctorGroupWriteService.updateGroupEvent(relEvent);
                    }
                } catch (Exception e) {
                    log.error("eventId {}", doctorGroupEvent.getId());
                }
            });
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("fix group event extra error, cause by {}", Throwables.getStackTraceAsString(e));
            return Boolean.FALSE;
        }
    }

    /**
     * 根据猪舍id查询猪场
     * @param farmId
     * @param barnId
     * @return
     */
    @RequestMapping(value = "/barnId/group", method = RequestMethod.GET)
    public List<DoctorGroup> doctorGroupDetails(@RequestParam Long farmId,@RequestParam Long barnId) {
        return RespHelper.or500(doctorGroupReadService.findGroupId(farmId, barnId));
    }
}
