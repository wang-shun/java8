package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.model.DoctorPigEvent;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 猪事件信息读取操作方式
 */
public interface DoctorPigEventReadService {

    /**
     * 母猪状态信息获取母猪事件列表
     * @param statusList
     * @return
     */
    Response<Map<String, List<Integer>>> queryPigEventByPigStatus(List<Integer> statusList);

    /**
     * 统计事件范围内的猪事件信息
     * @param farmId
     * @param pigId
     * @param beginDate
     * @param endDate
     * @return
     */
    Response<Paging<DoctorPigEvent>> queryPigDoctorEvents(@NotNull(message = "input.farmId.empty") Long farmId,
                                                          @NotNull(message = "input.pigId.empty") Long pigId,
                                                          Integer pageNo, Integer pageSize,
                                                          Date beginDate, Date endDate);

    /**
     * 通过pigIds 获取操作事件内容
     * @param pigIds
     * @return
     */
    Response<List<Integer>> queryPigEvents(@NotNull(message = "input.pigIds.empty") List<Long> pigIds);
}
