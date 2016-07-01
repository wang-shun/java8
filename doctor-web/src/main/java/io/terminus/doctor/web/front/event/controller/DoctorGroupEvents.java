package io.terminus.doctor.web.front.event.controller;

import com.google.common.collect.Lists;
import io.terminus.common.model.Paging;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    @Autowired
    public DoctorGroupEvents(DoctorGroupWebService doctorGroupWebService,
                             DoctorGroupReadService doctorGroupReadService,
                             DoctorFarmAuthCenter doctorFarmAuthCenter,
                             DoctorGroupWriteService doctorGroupWriteService) {
        this.doctorGroupWebService = doctorGroupWebService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorFarmAuthCenter = doctorFarmAuthCenter;
        this.doctorGroupWriteService = doctorGroupWriteService;
    }

    /**
     * 新建猪群
     * @return 猪群id
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createNewGroup(@RequestBody DoctorNewGroupInput newGroupDto) {
        //权限中心校验权限
        doctorFarmAuthCenter.checkFarmAuth(newGroupDto.getFarmId());
        return RespHelper.or500(doctorGroupWebService.createNewGroup(newGroupDto));
    }

    /**
     * 录入猪群事件
     * @param groupId 猪群id
     * @param eventType 事件类型
     * @see io.terminus.doctor.event.enums.GroupEventType
     * @param data 入参
     * @see io.terminus.doctor.event.dto.event.group.input.BaseGroupInput
     * @return 是否成功
     */
    @RequestMapping(value = "/other", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createGroupEvent(@RequestParam("groupId") Long groupId,
                                    @RequestParam("eventType") Integer eventType,
                                    @RequestParam("data") String data) {
        return RespHelper.or500(doctorGroupWebService.createGroupEvent(groupId, eventType, data));
    }

    /**
     * 编辑猪群事件
     * @param eventId 猪群事件id
     * @param data 入参
     * @see io.terminus.doctor.event.dto.event.group.edit.BaseGroupEdit
     * @return 是否成功
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean editGroupEvent(@RequestParam("eventId") Long eventId, @RequestParam("data") String data) {
        return RespHelper.or500(doctorGroupWebService.editGroupEvent(eventId, data));
    }

    /**
     * 根据猪群id查询可以操作的事件类型
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
     * @param barnName 猪舍名称
     * @return  猪群号
     */
    @RequestMapping(value = "/code", method = RequestMethod.GET)
    public String generateGroupCode(@RequestParam(value = "barnName", required = false) String barnName) {
        return doctorGroupWebService.generateGroupCode(barnName).getResult();
    }

    /**
     * 查询猪群详情
     * @param groupId 猪群id
     * @return 猪群详情
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public DoctorGroupDetail findGroupDetailByGroupId(@RequestParam("groupId") Long groupId) {
        return RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId));
    }

    /**
     * 分页查询猪群历史事件
     * @param farmId    猪场id
     * @param groupId   猪群id
     * @param type      事件类型
     * @param pageNo    分页大小
     * @param size      当前页码
     * @return  分页结果
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET)
    public Paging<DoctorGroupEvent> pagingGroupEvent(@RequestParam("farmId") Long farmId,
                                                     @RequestParam(value = "groupId", required = false) Long groupId,
                                                     @RequestParam(value = "type", required = false) Integer type,
                                                     @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                     @RequestParam(value = "size", required = false) Integer size) {
        return RespHelper.or500(doctorGroupReadService.pagingGroupEvent(farmId, groupId, type, pageNo, size));
    }

    /**
     * 查询猪群镜像信息(猪群, 猪群跟踪, 最新event)
     * @param groupId 猪群id
     * @return 猪群镜像
     */
    @RequestMapping(value = "/snapshot", method = RequestMethod.GET)
    public DoctorGroupSnapShotInfo findGroupSnapShotByGroupId(@RequestParam("groupId") Long groupId) {
        return RespHelper.or500(doctorGroupReadService.findGroupSnapShotInfoByGroupId(groupId));
    }

    /**
     * 查询已建群的猪群
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
     * 回滚猪群事件
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
}
