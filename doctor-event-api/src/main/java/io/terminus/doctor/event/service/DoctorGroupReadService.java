package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorGroupCount;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
                                              @NotNull(message = "farmId.not.nulll") Long farmId);

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
}
