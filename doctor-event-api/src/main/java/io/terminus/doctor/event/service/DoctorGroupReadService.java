package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
import io.terminus.doctor.event.dto.search.DoctorGroupCountDto;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Desc: 猪群想过读服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupReadService {

    /**
     * 根据id查询猪群卡片表
     *
     * @param groupId 主键id
     * @return 猪群卡片表
     */
    Response<DoctorGroup> findGroupById(Long groupId);

    /**
     * 根据id批量查询猪群卡片表
     *
     * @param groupIds 主键id
     * @return 猪群卡片表
     */
    Response<List<DoctorGroup>> findGroupByIds(List<Long> groupIds);

    /**
     * 根据farmId查询猪群卡片表
     *
     * @param farmId 猪场id
     * @return 猪群卡片表
     */
    Response<List<DoctorGroup>> findGroupsByFarmId(Long farmId);

    /**
     * 查询猪群详情
     *
     * @param groupId 猪群id
     * @return 猪群详情
     */
    Response<DoctorGroupDetail> findGroupDetailByGroupId(@NotNull(message = "groupId.not.null") Long groupId);

    /**
     * 根据查询条件分页查询猪群
     *
     * @param groupSearchDto 查询条件dto
     * @param pageNo         当前页码
     * @param size           分页大小
     * @return 分页后的猪群列表
     */
    Response<Paging<DoctorGroupDetail>> pagingGroup(@Valid DoctorGroupSearchDto groupSearchDto, Integer pageNo, Integer size);

    Response<List<DoctorGroup>> findGroup(Map<String, Object> params);

    /**
     * 获取猪群数量
     */
    Response<Long> getGroupCount(@Valid DoctorGroupSearchDto groupSearchDto);

    /**
     * 获取断奶仔猪数量
     */
    Response<Long> getWeanCount(@Valid DoctorGroupSearchDto groupSearchDto);

    /**
     * 根据查询条件分页猪群
     *
     * @param groupSearchDto 查询条件dto
     * @return 分页后的猪群列表
     */
    Response<List<DoctorGroupDetail>> findGroupDetail(@Valid DoctorGroupSearchDto groupSearchDto);

    /**
     * 根据猪群id查询可以操作的事件类型
     *
     * @param groupIds 猪群ids
     * @return 事件类型s
     */
    Response<List<Integer>> findEventTypesByGroupIds(@NotEmpty(message = "groupIds.not.empty") List<Long> groupIds);

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
    Response<Paging<DoctorGroupEvent>> pagingGroupEvent(@NotNull(message = "farmId.not.null") Long farmId,
                                                        Long groupId, Integer type, Integer pageNo, Integer size, String startDate, String endDate);

    /**
     * 分页查询猪群历史事件（去除断奶）
     *
     * @param farmId  猪场id
     * @param groupId 猪群id
     * @param type    事件类型
     * @param pageNo  分页大小
     * @param size    当前页码
     * @return 分页结果
     */
    Response<Paging<DoctorGroupEvent>> pagingGroupEventDelWean(@NotNull(message = "farmId.not.null") Long farmId,
                                                               Long groupId, Integer type, Integer pageNo, Integer size, String startDate, String endDate);


    /**
     * 根据事件id查询猪群事件
     *
     * @param eventId 事件id
     * @return 猪群事件
     */
    Response<DoctorGroupEvent> findGroupEventById(@NotNull(message = "evenId.not.null") Long eventId);

    /**
     * 根据猪群id查最新询猪群事件
     *
     * @param groupId 猪群id
     * @return 猪群事件
     */
    Response<DoctorGroupEvent> findLastEventByGroupId(@NotNull(message = "groupId.not.null") Long groupId);

    /**
     * 校验猪群号是否重复
     *
     * @param farmId    猪场id
     * @param groupCode 猪群号
     * @return true 重复, false 不重复
     */
    Response<Boolean> checkGroupRepeat(@NotNull(message = "farmId.not.null") Long farmId, @NotEmpty(message = "groupCode.not.empty") String groupCode);

    /**
     * 查询当前猪舍猪群(过滤掉已关闭的群)
     *
     * @param barnId 猪舍id
     * @return 猪群list
     */
    Response<List<DoctorGroup>> findGroupByCurrentBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    Response<List<DoctorGroup>> findByCurrentBarnIdAndQuantity(@NotNull(message = "barnId.not.null") Long barnId);

    Response<Long> findGroupPigQuantityByBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 根据日期区间和事件类型查询事件列表
     *
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
     *
     * @param farmId    猪场id
     * @param groupCode 猪群号
     * @return 猪群
     */
    Response<DoctorGroup> findGroupByFarmIdAndGroupCode(@NotNull(message = "farmId.not.null") Long farmId,
                                                        @NotEmpty(message = "groupCode.not.empty") String groupCode);


    /**
     * 查询一个猪舍累计有多少个事件
     *
     * @param barnId 猪舍id
     * @return
     */
    Response<Long> countByBarnId(Long barnId);

    /**
     * 根据条件查询猪群事件
     *
     * @param criteria
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<DoctorGroupEvent>> queryGroupEventsByCriteria(Map<String, Object> criteria, Integer pageNo, Integer pageSize);

    Response<List<DoctorGroupEvent>> getGroupEventsByCriteria(Map<String, Object> criteria);

    /**
     * 判断是否是最新事件
     *
     * @param groupId 猪群id
     * @param eventId 事件id
     * @return true 是最新事件, false 不是
     */
    Response<Boolean> isLastEvent(@NotNull(message = "groupId.not.null") Long groupId, @NotNull(message = "eventId.not.null") Long eventId);

    /**
     * 查询猪群回滚事件
     *
     * @param groupId
     * @return
     */
    RespWithEx<DoctorGroupEvent> canRollbackEvent(@NotNull(message = "input.groupId.empty") Long groupId);

    /**
     * 事件能否回滚
     *
     * @param eventId 事件id
     * @return 能否回滚
     */
    RespWithEx<Boolean> eventCanRollback(@NotNull(message = "input.eventId.empty") Long eventId);

    /**
     * 查询猪群的所有事件
     *
     * @param groupId
     * @return
     */
    Response<List<DoctorGroupEvent>> queryAllGroupEventByGroupId(@NotNull(message = "input.groupId.empty") Long groupId);

    /**
     * 根据查询日期统计每个猪场的待出栏猪只数
     *
     * @param sumAt 查询日期
     * @return
     */
    Response<Map<Long, Integer>> queryFattenOutBySumAt(String sumAt);

    /**
     * 获取事件操作人列表
     */
    Response<List<DoctorEventOperator>> queryOperators(Map<String, Object> criteria);

    /**
     * 查询猪群某一事件类型的最新事件
     *
     * @param groupId 猪群id
     * @param type    事件类型
     * @return 最新事件
     */
    Response<DoctorGroupEvent> findLastGroupEventByType(@NotNull(message = "groupId.not.null") Long groupId, @NotNull(message = "type.not.null") Integer type);

    /**
     * 获取初始猪群事件
     *
     * @param groupId 猪群id
     * @return 新建猪群事件
     */
    Response<DoctorGroupEvent> findInitGroupEvent(@NotNull(message = "groupId.not.null") Long groupId);

    /**
     * 获取猪群的所有事件,按发生日期排序
     *
     * @param groupId
     * @return
     */
    Response<List<DoctorGroupEvent>> findLinkedGroupEventsByGroupId(@NotNull(message = "groupId.not.null") Long groupId);

    /**
     * 获取猪场各个类型猪的存栏
     *
     * @param farmId 猪场id
     * @return
     */
    Response<DoctorGroupCountDto> findGroupCount(@NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 获取猪在关闭时间段里面的猪群ID
     *
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    Response<List<DoctorGroup>> findGroupIds(Long farmId, Date startAt, Date endAt);

    /**
     * 根据当前猪舍Id获取猪群
     *
     * @param farmId
     * @param barnId
     * @return
     */
    Response<List<DoctorGroup>> findGroupId(Long farmId, Long barnId);

    /**
     * 查询新建猪群事件
     *
     * @param groupId 猪群id
     * @return 新建猪群事件
     */
    Response<DoctorGroupEvent> findNewGroupEvent(@NotNull(message = "groupId.not.null") Long groupId);

    /**
     * 查询猪场与当前开启猪群数量映射
     *
     * @return
     */
    Response<Map<Long, Integer>> findFarmToGroupCount();

    /**
     * 统计某些猪群的仔猪数量和
     * @param groupIds
     * @return
     */
    Response<Integer> sumPigletCount(List<Long> groupIds);

    Response<DoctorGroupTrack> findTrackByGroupId(Long groupId);

    /**
     * 查询指定时间处于开启状态的猪群
     * @param date 日期 yyyy-MM-dd
     * @return 猪群列表
     */
    Response<List<DoctorGroup>> listOpenGroupsBy(String date);


    DoctorGroupEvent findLastEvent(Long groupId);

    /*物联网接口使用（孔景军）*/
    Response<List<DoctorGroup>> findGroupByCurrentBarnIdFuzzy(@NotNull(message = "barnId.not.null") Long barnId,String groupCode);
    /**
     * ysq
     */
    Long findGroupQuantityByGroupCode(String groupCode);
}
