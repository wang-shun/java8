package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.RespWithExHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.DoctorTransGroupEvent;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
import io.terminus.doctor.web.front.event.dto.DoctorBatchGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorBatchNewGroupEventDto;
import io.terminus.doctor.web.front.event.dto.DoctorGroupDetailEventsDto;
import io.terminus.doctor.web.front.event.dto.DoctorGroupEventPagingDto;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.doctor.web.util.TransFromUtil;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Autowired
    public DoctorGroupEvents(DoctorGroupWebService doctorGroupWebService,
                             DoctorGroupReadService doctorGroupReadService,
                             DoctorFarmAuthCenter doctorFarmAuthCenter,
                             DoctorGroupWriteService doctorGroupWriteService,
                             DoctorBasicReadService doctorBasicReadService, TransFromUtil transFromUtil) {
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
     * 批量猪群事件
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
    public String generateGroupCodeByPigId(@RequestParam(value = "pigId", required = false) Long pigId) {
        if (pigId == null) {
            return null;
        }
        DoctorPigTrack pigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
        return doctorGroupWebService.generateGroupCode(pigTrack.getCurrentBarnId()).getResult();
    }

    /**
     * 查询猪群详情
     *
     * @param groupId 猪群id
     * @param eventSize 事件大小
     * @return 猪群详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public DoctorGroupDetailEventsDto findGroupDetailByGroupId(@RequestParam("groupId") Long groupId,
                                                               @RequestParam(value = "eventSize", required = false) Integer eventSize) {
        DoctorGroupDetail groupDetail = RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId));

        //查询猪群的事件, 默认3条
        List<DoctorGroupEvent> groupEvents = RespHelper.or500(doctorGroupReadService.pagingGroupEvent(
                groupDetail.getGroup().getFarmId(), groupId, null, null, MoreObjects.firstNonNull(eventSize, 3))).getData();

        transFromUtil.transFromGroupEvents(groupEvents);
        Response<DoctorGroupEvent> groupEventResponse = doctorGroupReadService.canRollbackEvent(groupId);
        Long canRollback = null;
        if (groupEventResponse.isSuccess() && groupEventResponse.getResult() != null){
            canRollback = groupEventResponse.getResult().getId();
        }
        return new DoctorGroupDetailEventsDto(groupDetail.getGroup(), groupDetail.getGroupTrack(), groupEvents, canRollback);
    }

    /**
     * 分页查询猪群历史事件
     *
     * @param farmId  猪场id
     * @param groupId 猪群id
     * @param type    事件类型
     * @param pageNo  当前页码
     * @param size    分页大小
     * @return 分页结果
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public Paging<DoctorGroupEvent> pagingGroupEvent(@RequestParam("farmId") Long farmId,
                                                     @RequestParam(value = "groupId", required = false) Long groupId,
                                                     @RequestParam(value = "type", required = false) Integer type,
                                                     @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                     @RequestParam(value = "size", required = false) Integer size) {
        Paging<DoctorGroupEvent> doctorGroupEventPaging = RespHelper.or500(doctorGroupReadService.pagingGroupEvent(farmId, groupId, type, pageNo, size));

        transFromUtil.transFromGroupEvents(doctorGroupEventPaging.getData());
        return doctorGroupEventPaging;
    }

    /**
     * 分页查询猪群事件,同时带有可回滚事件id
     * @param farmId 猪场id
     * @param groupId 猪群id
     * @param type 事件类型
     * @param pageNo 当前页码
     * @param size 分页大小
     * @return 带有是否可回滚的分页结果
     */
    @RequestMapping(value = "/pagingRollbackGroupEvent", method = RequestMethod.GET)
    public DoctorGroupEventPagingDto pagingGroupEventWithCanRollback(@RequestParam("farmId") Long farmId,
                                                      @RequestParam(value = "groupId", required = false) Long groupId,
                                                      @RequestParam(value = "type", required = false) Integer type,
                                                      @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                      @RequestParam(value = "size", required = false) Integer size) {
        Paging<DoctorGroupEvent> doctorGroupEventPaging = pagingGroupEvent(farmId, groupId, type, pageNo, size);
        Response<DoctorGroupEvent> groupEventResponse = doctorGroupReadService.canRollbackEvent(groupId);
        Long canRollback = null;
        if (groupEventResponse.isSuccess() && groupEventResponse.getResult() != null){
            canRollback = groupEventResponse.getResult().getId();
        }
        return DoctorGroupEventPagingDto.builder().paging(doctorGroupEventPaging).canRollback(canRollback).build();
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
     * 查询猪群镜像信息(猪群, 猪群跟踪, 最新event)
     *
     * @param groupId 猪群id
     * @return 猪群镜像
     */
    @RequestMapping(value = "/snapshot", method = RequestMethod.GET)
    public DoctorGroupSnapShotInfo findGroupSnapShotByGroupId(@RequestParam("groupId") Long groupId) {
        return RespHelper.or500(doctorGroupReadService.findGroupSnapShotInfoByGroupId(groupId));
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
    public Boolean rolllbackGroupEvent(@RequestParam("eventId") Long eventId) {
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
        if(group.getBreedId() != null){
            DoctorBasic breed = RespHelper.or500(doctorBasicReadService.findBasicById(group.getBreedId()));
            if(breed != null){
                return Lists.newArrayList(breed);
            }
        }
        return RespHelper.or500(doctorBasicReadService.findBasicByTypeAndSrmWithCache(DoctorBasic.Type.BREED.getValue(), null));
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
        if (params.get("eventTypes") !=null) {
            params.put("types", Splitters.COMMA.splitToList((String)params.get("eventTypes")));
            params.remove("eventTypes");
        }
        return RespHelper.or500(doctorGroupReadService.queryGroupEventsByCriteria(params, pageNo, pageSize));
    }

    /**
     * 获取猪群新建事件
     * @param groupId 猪群id
     * @return 新建事件
     */
    @RequestMapping(value = "/find/newGroupEvent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorGroupEvent findNewGroupEvent(@RequestParam Long groupId) {
        return RespHelper.or500(doctorGroupReadService.findNewGroupEvent(groupId));
    }

    /**
     * 猪群转群事件的extra中添加groupid(暂时)
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
                        DoctorTransGroupEvent doctorTransGroupEvent = JsonMapper.JSON_NON_EMPTY_MAPPER.fromJson(relEvent.getExtra(), DoctorTransGroupEvent.class);
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
}
