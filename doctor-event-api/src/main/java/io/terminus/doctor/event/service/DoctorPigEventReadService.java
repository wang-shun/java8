package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorNpdExportDto;
import io.terminus.doctor.event.dto.DoctorSowParityAvgDto;
import io.terminus.doctor.event.dto.DoctorSowParityCount;
import io.terminus.doctor.event.dto.DoctorSuggestPig;
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
     * 查询某一猪场可执行此事件的猪
     * @param eventType 事件类型
     * @param farmId 猪场id
     * @return
     */
    Response<List<DoctorSuggestPig>> suggestPigsByEvent(Integer eventType, Long farmId, String pigCode, Integer sex);

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
    Response<List<DoctorEventOperator>> queryOperators(Map<String, Object> criteria);

    /**
     * 判断是否是最新事件
     * @param pigId 猪id
     * @return true 是最新事件, false 不是
     */
    Response<Boolean> isLastEvent(@NotNull(message = "input.pigId.empty") Long pigId, @NotNull(message = "eventId.not.null") Long eventId);

    /**
     * 判断是否是最新手动事件
     * @param pigId 猪id
     * @return true 是最新事件, false 不是
     */
    Response<Boolean> isLastManualEvent(@NotNull(message = "input.pigId.empty") Long pigId, @NotNull(message = "eventId.not.null") Long eventId);

    /**
     * 查询猪回滚事件
     * @param pigId
     * @return
     */
    RespWithEx<DoctorPigEvent> canRollbackEvent(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 事件能否回滚
     * @param eventId 事件id
     * @return 能否回滚
     */
    RespWithEx<Boolean> eventCanRollback(@NotNull(message = "input.eventId.empty") Long eventId);

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

    /**
     *  获取猪某一类型的最新事件
     * @param pigId
     * @param type
     * @return
     */
    Response<DoctorPigEvent> findLastEventByType(@NotNull(message = "input.pigId.empty") Long pigId, Integer type);


    /**
     * 根据事件来获取猪id
     * @param criteria
     *  type      事件类型
     *  beginAt   事件日期开始
     *  endAt     事件日期结束
     * @return
     */
    Response<List<Long>> findPigIdsBy(Map<String, Object> criteria);

    /**
     * 获取母猪事件的npd
     * @param map
     * @param offset
     * @param limit
     * @return
     */
    Response<Paging<DoctorNpdExportDto>> pagingFindNpd(Map<String, Object> map, Integer offset, Integer limit);
}
