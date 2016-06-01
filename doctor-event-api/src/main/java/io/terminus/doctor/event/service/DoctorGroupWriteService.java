package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;

import javax.validation.Valid;

/**
 * Desc: 猪群卡片表写服务
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */

public interface DoctorGroupWriteService {

    /**
     * 新建猪群
     * @return 主键id
     */
    Response<Long> createNewGroup(DoctorGroup group, @Valid DoctorNewGroupInput newGroupInput);

    /**
     * 录入防疫事件
     * @param groupDetail 操作猪群信息
     * @param antiepidemic 防疫信息
     * @return 是否成功
     */
    Response<Boolean> groupEventAntiepidemic(DoctorGroupDetail groupDetail, @Valid DoctorAntiepidemicGroupInput antiepidemic);

    /**
     * 录入猪群变动事件
     * @param groupDetail 操作猪群信息
     * @param change 猪群变动信息
     * @return 是否成功
     */
    Response<Boolean> groupEventChange(DoctorGroupDetail groupDetail, @Valid DoctorChangeGroupInput change);

    /**
     * 录入猪群关闭事件
     * @param groupDetail 操作猪群信息
     * @param close  猪群关闭信息
     * @return 是否成功
     */
    Response<Boolean> groupEventClose(DoctorGroupDetail groupDetail, @Valid DoctorCloseGroupInput close);

    /**
     * 录入疾病事件
     * @param groupDetail 操作猪群信息
     * @param disease 疾病信息
     * @return 是否成功
     */
    Response<Boolean> groupEventDisease(DoctorGroupDetail groupDetail, @Valid DoctorDiseaseGroupInput disease);

    /**
     * 录入猪只存栏事件
     * @param groupDetail 操作猪群信息
     * @param liveStock 猪只存栏信息
     * @return 是否成功
     */
    Response<Boolean> groupEventLiveStock(DoctorGroupDetail groupDetail, @Valid DoctorLiveStockGroupInput liveStock);

    /**
     * 录入转入猪群事件
     * @param groupDetail 操作猪群信息
     * @param moveIn 转入猪群信息
     * @return 是否成功
     */
    Response<Boolean> groupEventMoveIn(DoctorGroupDetail groupDetail, @Valid DoctorMoveInGroupInput moveIn);

    /**
     * 录入转场事件
     * @param groupDetail 操作猪群信息
     * @param transFarm 转场信息
     * @return 是否成功
     */
    Response<Boolean> groupEventTransFarm(DoctorGroupDetail groupDetail, @Valid DoctorTransFarmGroupInput transFarm);

    /**
     * 录入猪群转群事件
     * @param groupDetail 操作猪群信息
     * @param transGroup 猪群转群信息
     * @return 是否成功
     */
    Response<Boolean> groupEventTransGroup(DoctorGroupDetail groupDetail, @Valid DoctorTransGroupInput transGroup);

    /**
     * 录入商品猪转为种猪事件
     * @param groupDetail 操作猪群信息
     * @param turnSeed 商品猪转为种猪信息
     * @return 是否成功
     */
    Response<Boolean> groupEventTurnSeed(DoctorGroupDetail groupDetail, @Valid DoctorTurnSeedGroupInput turnSeed);

    /**
     * job调用, 用于每日更新日龄
     * @return 是否成功
     */
    Response<Boolean> incrDayAge();
}