package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorGroupDetailDto;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import io.terminus.doctor.event.model.DoctorGroupTrack;

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
    Response<DoctorGroupDetailDto> findGroupDetailByGroupId(@NotNull(message = "groupId.not.null") Long groupId);

    /**
     * 根据查询条件分页查询猪群
     * @param groupSearchDto 查询条件dto
     * @param pageNo 当前页码
     * @param size   分页大小
     * @return 分页后的猪群列表
     */
    Response<Paging<DoctorGroup>> pagingGroup(DoctorGroupSearchDto groupSearchDto, Integer pageNo, Integer size);

    /**
     * 根据id查询猪群事件表
     * @param groupEventId 主键id
     * @return 猪群事件表
     */
    Response<DoctorGroupEvent> findGroupEventById(Long groupEventId);

    /**
     * 根据farmId查询猪群事件表
     * @param farmId 猪场id
     * @return 猪群事件表
     */
    Response<List<DoctorGroupEvent>> findGroupEventsByFarmId(Long farmId);

    /**
     * 根据id查询猪群快照表
     * @param groupSnapshotId 主键id
     * @return 猪群快照表
     */
    Response<DoctorGroupSnapshot> findGroupSnapshotById(Long groupSnapshotId);

    /**
     * 根据id查询猪群卡片明细表
     * @param groupTrackId 主键id
     * @return 猪群卡片明细表
     */
    Response<DoctorGroupTrack> findGroupTrackById(Long groupTrackId);
}
