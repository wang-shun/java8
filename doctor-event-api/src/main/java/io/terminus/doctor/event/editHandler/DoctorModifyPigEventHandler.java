package io.terminus.doctor.event.editHandler;

import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.edit.DoctorEventChangeDto;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;

/**
 * Created by xjn on 17/4/13.
 * 猪事件编辑和回滚接口
 */
public interface DoctorModifyPigEventHandler {

    /**
     * 构建编辑的变化结果
     * @param oldPigEvent 原事件
     * @param inputDto 编辑输入
     * @return 变化结果
     */
    DoctorEventChangeDto buildEventChange(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto);

    /**
     * 构建新猪信息
     * @param oldPig 原猪信息
     * @param inputDto 编辑输入
     * @return 新猪信息
     */
    DoctorPig buildNewPig(DoctorPig oldPig, BasePigEventInputDto inputDto);

    /**
     * 根据原事件和变化构建新事件
     * @param oldPigEvent 原事件
     * @param inputDto 编辑后输入
     * @return 新事件
     */
    DoctorPigEvent buildNewEvent(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto);

    /**
     * 根据原track和变化构建新track
     * @param oldPigTrack 原track
     * @param changeDto 变化
     * @return 新track
     */
    DoctorPigTrack buildNewTrack(DoctorPigTrack oldPigTrack, DoctorEventChangeDto changeDto);


    /**
     * 能否编辑
     * @param oldPigEvent 原事件
     */
    Boolean canModify(DoctorPigEvent oldPigEvent);

    /**
     * 编辑处理
     * @param oldPigEvent 原事件
     * @param inputDto 编辑后输入
     */
    void modifyHandle(DoctorPigEvent oldPigEvent, BasePigEventInputDto inputDto);

    /**
     * 能否回滚
     * @param deletePigEvent 原事件
     */
    Boolean canRollback(DoctorPigEvent deletePigEvent);

    /**
     * 事件回滚处理
     * @param deletePigEvent 回滚事件
     * @param operatorId 操作人id
     * @param operatorName 操作人姓名
     */
    void rollbackHandle(DoctorPigEvent deletePigEvent, Long operatorId, String operatorName);
}
