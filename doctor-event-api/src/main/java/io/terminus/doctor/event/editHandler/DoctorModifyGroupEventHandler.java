package io.terminus.doctor.event.editHandler;

import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.dto.event.group.input.BaseGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;

/**
 * Created by xjn on 17/4/13.
 * 猪群编辑和回滚处理接口
 */
public interface DoctorModifyGroupEventHandler {

    /**
     * 构建编辑的变化结果
     * @param oldGroupEvent 原事件
     * @param input 编辑输入
     * @return 变化结果
     */
    DoctorEventChangeDto buildEventChange(DoctorGroupEvent oldGroupEvent, BaseGroupInput input);

    /**
     * 根据原事件和变化构建新事件
     * @param oldGroupEvent 原事件
     * @param input 编辑输入
     * @return 新事件
     */
    DoctorGroupEvent buildNewEvent(DoctorGroupEvent oldGroupEvent, BaseGroupInput input);

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
     * 能否编辑
     * @param oldGroupEvent 原事件
     */
    Boolean canModify(DoctorGroupEvent oldGroupEvent);

    /**
     * 编辑处理
     * @param oldGroupEvent 原事件
     * @param input 编辑输入
     */
    void modifyHandle(DoctorGroupEvent oldGroupEvent, BaseGroupInput input);

    /**
     * 能否回滚
     * @param deleteGroupEvent 删除事件
     */
    Boolean canRollback(DoctorGroupEvent deleteGroupEvent);
    /**
     * 事件回滚处理
     * @param groupEvent 回滚事件
     * @param operatorId 操作人id
     * @param operatorName 操作人姓名
     */
    void rollbackHandle(DoctorGroupEvent groupEvent, Long operatorId, String operatorName);
}
