package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.*;
import io.terminus.doctor.event.dto.event.DoctorEventOperator;
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
     *
     * @param statusList
     * @return
     */
    Response<Map<String, List<Integer>>> queryPigEventByPigStatus(List<Integer> statusList);

    /**
     * 统计事件范围内的猪事件信息
     *
     * @param farmId
     * @param pigId
     * @param beginDate
     * @param endDate
     * @return
     */
    Response<Paging<DoctorPigEvent>> queryPigDoctorEvents(Long farmId,
                                                          @NotNull(message = "input.pigId.empty") Long pigId,
                                                          Integer pageNo, Integer pageSize,
                                                          Date beginDate, Date endDate);

    /**
     * 查找一只猪(在指定时间之后)的第一个事件
     *
     * @param pigId    猪id, 不可为空
     * @param fromDate 可为空
     * @return
     */
    Response<DoctorPigEvent> findFirstPigEvent(Long pigId, Date fromDate);

    /**
     * 查询猪舍的事件数量
     *
     * @param barnId 猪舍id
     * @return
     */
    Response<Long> countByBarnId(Long barnId);

    /**
     * 通过pigIds 获取操作事件内容
     *
     * @param pigIds
     * @return
     */
    Response<List<Integer>> queryPigEvents(@NotNull(message = "input.pigIds.empty") List<Long> pigIds);

    /**
     * 查询某一猪场可执行此事件的猪
     *
     * @param eventType 事件类型
     * @param farmId    猪场id
     * @return
     */
    Response<List<DoctorSuggestPig>> suggestPigsByEvent(Integer eventType, Long farmId, String pigCode, Integer sex);

    /**
     * 通过 id 获取 PigEvent
     *
     * @param id
     * @return
     */
    Response<DoctorPigEvent> queryPigEventById(Long id);

    /**
     * 母猪胎次信息总结
     *
     * @param pigId 母猪Id
     * @return
     */
    Response<List<DoctorSowParityCount>> querySowParityCount(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 校验母猪不处于分娩状态
     *
     * @param pigIds
     * @return
     */
    Response<Boolean> validatePigNotInFeed(@NotNull(message = "input.pigIds.empty") String pigIds);

    /**
     * 根据条件查询事件
     *
     * @param criteria
     * @return
     */
    Response<Paging<DoctorPigEvent>> queryPigEventsByCriteria(Map<String, Object> criteria, Integer pageNo, Integer pageSize);

    Response<DoctorPigEvent> getabosum(Map<String, Object> criteria, Integer pageNo, Integer pageSize);

    Response<DoctorPigEvent> getweansum(Map<String, Object> criteria, Integer pageNo, Integer pageSize);
    Response<DoctorPigEvent> getfosterssum(Map<String, Object> criteria, Integer pageNo, Integer pageSize);
    Response<DoctorPigEvent> getpigletssum(Map<String, Object> criteria, Integer pageNo, Integer pageSize);


    /**
     * 获取事件操作人列表
     */
    Response<List<DoctorEventOperator>> queryOperators(Map<String, Object> criteria);

    /**
     * 判断是否是最新事件
     *
     * @param pigId 猪id
     * @return true 是最新事件, false 不是
     */
    Response<Boolean> isLastEvent(@NotNull(message = "input.pigId.empty") Long pigId, @NotNull(message = "eventId.not.null") Long eventId);

    /**
     * 判断是否是最新手动事件
     *
     * @param pigId 猪id
     * @return true 是最新事件, false 不是
     */
    Response<Boolean> isLastManualEvent(@NotNull(message = "input.pigId.empty") Long pigId, @NotNull(message = "eventId.not.null") Long eventId);

    /**
     * 查询猪回滚事件
     *
     * @param pigId
     * @return
     */
    RespWithEx<DoctorPigEvent> canRollbackEvent(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 事件能否回滚
     *
     * @param eventId 事件id
     * @return 能否回滚
     */
    RespWithEx<Boolean> eventCanRollback(@NotNull(message = "input.eventId.empty") Long eventId);

    /**
     * 获取猪的最新事件
     *
     * @param pigId
     * @return
     */
    Response<DoctorPigEvent> lastEvent(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 获取多个猪时的最新事件
     *
     * @param pigIds
     * @return
     */
    Response<DoctorPigEvent> lastEvent(@NotNull(message = "input.pigIds.empty") List<Long> pigIds);


    /**
     * 事件列表(修复断奶事件暂时)
     *
     * @return
     */
    Response<List<DoctorPigEvent>> addWeanEventAfterFosAndPigLets();

    /**
     * 查询母猪胎次中数据平均值
     *
     * @param pigId
     * @return
     */
    Response<DoctorSowParityAvgDto> querySowParityAvg(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 获取猪某一类型的最新事件
     *
     * @param pigId
     * @param type
     * @return
     */
    Response<DoctorPigEvent> findLastEventByType(@NotNull(message = "input.pigId.empty") Long pigId, Integer type);


    /**
     * 根据事件来获取猪id
     *
     * @param criteria type      事件类型
     *                 beginAt   事件日期开始
     *                 endAt     事件日期结束
     * @return
     */
    Response<List<Long>> findPigIdsBy(Map<String, Object> criteria);

    /**
     * 获取母猪事件的npd
     *
     * @param map
     * @param offset
     * @param limit
     * @return
     */
    Response<Paging<DoctorNpdExportDto>> pagingFindNpd(Map<String, Object> map, Integer offset, Integer limit);

    /**
     * 查询猪的销售情况
     *
     * @param map
     * @param offset
     * @param limit
     * @return
     */
    Response<Paging<DoctorPigSalesExportDto>> pagingFindSales(Map<String, Object> map, Integer offset, Integer limit);

    /**
     * 查询猪的销售情况
     *
     * @param map
     * @return
     */
    Response<List<DoctorPigSalesExportDto>> listFindSales(Map<String, Object> map);

    /**
     * 查询猪的利润情况
     *
     * @param map
     * @return
     */
    Response<List<DoctorProfitExportDto>> sumProfitAmount(Map<String, Object> map);

    /**
     * 获取最新的胎次
     *
     * @param pigId 猪id
     * @return 最新胎次
     */
    Response<Integer> findLastParity(Long pigId);

    /**
     * 获取某一头某一胎次下未断奶数量
     *
     * @param pigId  猪id
     * @param parity 胎次
     * @return 未断奶数
     */
    Response<Integer> findUnWeanCountByParity(Long pigId, Integer parity);

    /**
     * 获取最近一次的初配事件
     *
     * @param pigId 猪id
     * @return 初配事件
     */
    Response<DoctorPigEvent> findLastFirstMateEvent(Long pigId);

    /**
     * 获取指定胎次下最近一次的初配事件
     *
     * @param pigId  猪编号
     * @param parity 胎次
     * @return
     */
    Response<DoctorPigEvent> findLastFirstMateEvent(Long pigId, Integer parity);

    /**
     * 获取某猪某胎次下,妊娠检查时间前最近的初配事件
     *
     * @param pigId  猪id
     * @param parity 胎次
     * @param id     妊娠检查事件id
     * @return 初配事件
     */
    Response<DoctorPigEvent> findFirstMatingBeforePregCheck(Long pigId, Integer parity, Long id);


    Response<DoctorPigEvent> findById(Long eventId);


    Response<DoctorPigEvent> getFarrowEventByParity(Long pigId, Integer parity);

    /**
     * 查询导致猪到达当前的状态事件的日期
     * @param pigId 猪id
     * @param status 猪状态
     * @return 事件日期
     */
    Response<Date> findEventAtLeadToStatus(Long pigId, Integer status);



    /**
     *                              新增代码
     * 根据事件筛选出母猪ID
     * @param criteria
     * @return
     */
    Response<List<Long>> findPigIdsByEvent(Map<String, Object> criteria);


}
