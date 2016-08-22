package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorGroupCount;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorGroupSnapShotInfo;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Desc: 猪群想过读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupReadService {

    /**
     * 根据id查询猪群卡片表
     * @param groupId 主键id
     * @return 猪群卡片表
     */
    Response<DoctorGroup> findGroupById(Long groupId);

    /**
     * 根据id批量查询猪群卡片表
     * @param groupIds 主键id
     * @return 猪群卡片表
     */
    Response<List<DoctorGroup>> findGroupByIds(List<Long> groupIds);

    /**
     * 根据farmId查询猪群卡片表
     * @param farmId 猪场id
     * @return 猪群卡片表
     */
    Response<List<DoctorGroup>> findGroupsByFarmId(Long farmId);

    /**
     * 查询猪群详情
     * @param groupId 猪群id
     * @return 猪群详情
     */
    Response<DoctorGroupDetail> findGroupDetailByGroupId(@NotNull(message = "groupId.not.null") Long groupId);

    /**
     * 查询猪群镜像信息(猪群, 猪群跟踪, 最新event)
     * @param groupId 猪群id
     * @return 猪群镜像
     */
    Response<DoctorGroupSnapShotInfo> findGroupSnapShotInfoByGroupId(@NotNull(message = "groupId.not.null") Long groupId);

    /**
     * 根据toEventId查询订单镜像
     * @param toEventId toEventId
     * @return 订单镜像
     */
    Response<DoctorGroupSnapshot> findGroupSnapShotByToEventId(@NotNull(message = "toEventId.not.null") Long toEventId);

    /**
     * 根据查询条件分页查询猪群
     * @param groupSearchDto 查询条件dto
     * @param pageNo 当前页码
     * @param size   分页大小
     * @return 分页后的猪群列表
     */
    Response<Paging<DoctorGroupDetail>> pagingGroup(@Valid DoctorGroupSearchDto groupSearchDto, Integer pageNo, Integer size);

    /**
     * 根据查询条件分页猪群
     * @param groupSearchDto 查询条件dto
     * @return 分页后的猪群列表
     */
    Response<List<DoctorGroupDetail>> findGroupDetail(@Valid DoctorGroupSearchDto groupSearchDto);

    /**
     * 根据猪群id查询可以操作的事件类型
     * @param groupIds 猪群ids
     * @return 事件类型s
     */
    Response<List<Integer>> findEventTypesByGroupIds(@NotEmpty(message = "groupIds.not.empty") List<Long> groupIds);

    /**
     * 统计猪群不同种类的猪群数量
     * @param farmId 猪场id
     * @return 统计结果
     */
    Response<DoctorGroupCount> countFarmGroups(@NotNull(message = "orgId.not.null") Long orgId,
                                               @NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 分页查询猪群历史事件
     * @param farmId    猪场id
     * @param groupId   猪群id
     * @param type      事件类型
     * @param pageNo    分页大小
     * @param size      当前页码
     * @return  分页结果
     */
    Response<Paging<DoctorGroupEvent>> pagingGroupEvent(@NotNull(message = "farmId.not.null") Long farmId,
                                                        Long groupId, Integer type, Integer pageNo, Integer size);

    /**
     * 根据事件id查询猪群事件
     * @param eventId    事件id
     * @return 猪群事件
     */
    Response<DoctorGroupEvent> findGroupEventById(@NotNull(message = "evenId.not.null") Long eventId);

    /**
     * 校验猪群号是否重复
     * @param farmId  猪场id
     * @param groupCode 猪群号
     * @return true 重复, false 不重复
     */
    Response<Boolean> checkGroupRepeat(@NotNull(message = "farmId.not.null") Long farmId, @NotEmpty(message = "groupCode.not.empty") String groupCode);

    /**
     * 查询当前猪舍猪群(过滤掉已关闭的群)
     * @param barnId 猪舍id
     * @return 猪群list
     */
    Response<List<DoctorGroup>> findGroupByCurrentBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 根据日期区间和事件类型查询事件列表
     * @param farmId    猪场id
     * @param eventType 事件类型
     * @param startAt   开始事件
     * @param endAt     结束事件
     * @return 猪群事件list
     */
    Response<List<DoctorGroupEvent>> findGroupEventsByEventTypeAndDate(@NotNull(message = "farmId.not.null") Long farmId,
                                                                       Integer eventType, Date startAt, Date endAt);

    /**
     * 根据日期区间和事件类型查询事件列表
     * @param farmId    猪场id
     * @param groupCode 猪群号
     * @return 猪群
     */
    Response<DoctorGroup> findGroupByFarmIdAndGroupCode(@NotNull(message = "farmId.not.null") Long farmId,
                                                        @NotEmpty(message = "groupCode.not.empty") String groupCode);


    /**
     * 查询一个猪舍累计有多少个事件
     * @param barnId 猪舍id
     * @return
     */
    Response<Long> countByBarnId(Long barnId);
}
