package io.terminus.doctor.web.front.event.controller;

import com.google.common.collect.Lists;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @Autowired
    public DoctorGroupEvents(DoctorGroupWebService doctorGroupWebService,
                             DoctorGroupReadService doctorGroupReadService) {
        this.doctorGroupWebService = doctorGroupWebService;
        this.doctorGroupReadService = doctorGroupReadService;
    }

    /**
     * 新建猪群
     * @return 猪群id
     */
    @RequestMapping(value = "/new", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createNewGroup(@RequestBody DoctorNewGroupInput newGroupDto) {
        return RespHelper.or500(doctorGroupWebService.createNewGroup(newGroupDto));
    }

    /**
     * 录入其他事件
     * @return 是否成功
     */
    @RequestMapping(value = "/other", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean createGroupEvent(@RequestParam("groupId") Long groupId,
                                    @RequestParam("eventType") Integer eventType,
                                    @RequestParam Map<String, Object> params) {
        return RespHelper.or500(doctorGroupWebService.createGroupEvent(groupId, eventType, params));
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
}
