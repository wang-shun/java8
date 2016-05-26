package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;

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
    Response<Long> createNewGroup(DoctorGroup group, DoctorGroupEvent groupEvent, DoctorGroupTrack groupTrack);


    /**
     * 录入防疫事件
     * @param group  操作猪群
     * @param antiepidemic 防疫信息
     * @return 是否成功
     */
    Response<Boolean> groupEventAntiepidemic(DoctorGroup group, @Valid DoctorAntiepidemicGroupInput antiepidemic);

    /**
     * 录入猪群变动事件
     * @param group  操作猪群
     * @param change 猪群变动信息
     * @return 是否成功
     */
    Response<Boolean> groupEventChange(DoctorGroup group, @Valid DoctorChangeGroupInput change);

    /**
     * 录入猪群关闭事件
     * @param group  操作猪群
     * @param close  猪群关闭信息
     * @return 是否成功
     */
    Response<Boolean> groupEventClose(DoctorGroup group, @Valid DoctorCloseGroupInput close);

    /**
     * 录入疾病事件
     * @param group  操作猪群
     * @param disease 疾病信息
     * @return 是否成功
     */
    Response<Boolean> groupEventDisease(DoctorGroup group, @Valid DoctorDiseaseGroupInput disease);

    /**
     * 录入猪只存栏事件
     * @param group  操作猪群
     * @param liveStock 猪只存栏信息
     * @return 是否成功
     */
    Response<Boolean> groupEventLiveStock(DoctorGroup group, @Valid DoctorLiveStockGroupInput liveStock);

    /**
     * 录入转入猪群事件
     * @param group  操作猪群
     * @param moveIn 转入猪群信息
     * @return 是否成功
     */
    Response<Boolean> groupEventMoveIn(DoctorGroup group, @Valid DoctorMoveInGroupInput moveIn);

    /**
     * 录入转场事件
     * @param group  操作猪群
     * @param transFarm 转场信息
     * @return 是否成功
     */
    Response<Boolean> groupEventTransFarm(DoctorGroup group, @Valid DoctorTransFarmGroupInput transFarm);

    /**
     * 录入猪群转群事件
     * @param group  操作猪群
     * @param transGroup 猪群转群信息
     * @return 是否成功
     */
    Response<Boolean> groupEventTransGroup(DoctorGroup group, @Valid DoctorTransGroupInput transGroup);

    /**
     * 录入商品猪转为种猪事件
     * @param group  操作猪群
     * @param turnSeed 商品猪转为种猪信息
     * @return 是否成功
     */
    Response<Boolean> groupEventTurnSeed(DoctorGroup group, @Valid DoctorTurnSeedGroupInput turnSeed);

}