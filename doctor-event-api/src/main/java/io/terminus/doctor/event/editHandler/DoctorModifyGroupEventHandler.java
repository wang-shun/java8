package io.terminus.doctor.event.editHandler;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;

/**
 * Created by xjn on 17/4/13.
 * 猪群编辑处理接口
 */
public interface DoctorModifyGroupEventHandler {

    /**
     * 构建编辑的变化结果
     * @param oldGroupEvent 原事件
     * @param inputDto 编辑输入
     * @return 变化结果
     */
    DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BasePigEventInputDto inputDto);

    /**
     * 根据原事件和变化构建新事件
     * @param oldGroupEvent 原事件
     * @param changeDto 变化
     * @return 新事件
     */
    DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, DoctorEventChangeDto changeDto);

    /**
     * 构建新猪群信息
     * @param oldGroup 原猪群信息
     * @param changeDto 变化
     * @return 新猪群信息
     */
    DoctorGroup buildNewGroup(DoctorGroup oldGroup, DoctorEventChangeDto changeDto);

    /**
     * 根据原track和变化构建新track
     * @param oldGroupTrack 原track
     * @param changeDto 变化
     * @return 新track
     */
    DoctorGroupTrack buildNewTrack(DoctorGroupTrack oldGroupTrack, DoctorEventChangeDto changeDto);

    //// TODO: 17/4/13 构建记录表

    /**
     * 编辑校验
     * @param oldGroupEvent 原事件
     * @param changeDto 变化
     */
    void handleCheck(DoctorGroupEvent oldGroupEvent, DoctorEventChangeDto changeDto);

    /**
     * 编辑处理
     * @param oldGroupEvent 原事件
     * @param changeDto 变化
     */
    void handle(DoctorGroupEvent oldGroupEvent, DoctorEventChangeDto changeDto);
}
