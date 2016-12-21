package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorSowParityAvgDto;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
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
     * 查找一只猪(在指定时间之后)的第一个事件
     * @param pigId 猪id, 不可为空
     * @param fromDate 可为空
     * @return
     */
    Response<DoctorPigEvent> findFirstPigEvent(Long pigId, Date fromDate);

    /**
     * 查询猪舍的事件数量
     * @param barnId 猪舍id
     * @return
     */
    Response<Long> countByBarnId(Long barnId);

    /**
     * 通过pigIds 获取操作事件内容
     * @param pigIds
     * @return
     */
    Response<List<Integer>> queryPigEvents(@NotNull(message = "input.pigIds.empty") List<Long> pigIds);

    /**
     * 通过 id 获取 PigEvent
     * @param id
     * @return
     */
    Response<DoctorPigEvent> queryPigEventById(Long id);

    /**
     * 母猪胎次信息总结
     * @param pigId 母猪Id
     * @return
     */
    Response<List<DoctorSowParityCount>> querySowParityCount(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 校验母猪不处于分娩状态
     * @param pigIds
     * @return
     */
    Response<Boolean> validatePigNotInFeed(@NotNull(message = "input.pigIds.empty") String pigIds);

    /**
     * 根据条件查询事件
     * @param criteria
     * @return
     */
    Response<Paging<DoctorPigEvent>> queryPigEventsByCriteria(Map<String, Object> criteria, Integer pageNo, Integer pageSize);

    /**
     * 获取事件操作人列表
     */
    Response<List<DoctorPigEvent>> queryOperators(Map<String, Object> criteria);

    /**
     * 判断是否是最新事件
     * @param pigId 猪id
     * @return true 是最新事件, false 不是
     */
    Response<Boolean> isLastEvent(@NotNull(message = "input.pigId.empty") Long pigId, @NotNull(message = "eventId.not.null") Long eventId);

    /**
     * 查询猪回滚事件
     * @param pigId
     * @return
     */
    Response<DoctorPigEvent> canRollbackEvent(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 获取猪的最新事件
     * @param pigId
     * @return
     */
    Response<DoctorPigEvent> lastEvent(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 获取多个猪时的最新事件
     * @param pigIds
     * @return
     */
    Response<DoctorPigEvent> lastEvent(@NotNull(message = "input.pigIds.empty") List<Long> pigIds);


    /**
     * 事件列表(修复断奶事件暂时)
     * @return
     */
    Response<List<DoctorPigEvent>> addWeanEventAfterFosAndPigLets();

    /**
     * 查询母猪胎次中数据平均值
     * @param pigId
     * @return
     */
    Response<DoctorSowParityAvgDto> querySowParityAvg(@NotNull(message = "input.pigId.empty") Long pigId);


}
