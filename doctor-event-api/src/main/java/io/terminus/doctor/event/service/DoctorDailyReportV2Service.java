package io.terminus.doctor.event.service;


import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.DoctorFarmEarlyEventAtDto;
import io.terminus.doctor.event.dto.report.daily.DoctorFarmLiveStockDto;
import io.terminus.doctor.event.dto.reportBi.DoctorDimensionReport;

import java.util.Date;
import java.util.List;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
public interface DoctorDailyReportV2Service {

    /**
     * 刷新猪场指定时间段内每一天的指标
     *
     * @param farmId  猪场id
     * @param startAt 开始时间 yyyy-MM-dd
     * @param endAt   结束时间 yyyy-MM-dd
     */
    Response<Boolean> flushFarmDaily(Long farmId, String startAt, String endAt);

    /**
     * 刷新猪群指定时间段内每一天相关指标
     *
     * @param farmId  猪场id
     * @param startAt 开始时间 yyyy-MM-dd
     * @param endAt   结束时间 yyyy-MM-dd
     */
    Response<Boolean> flushGroupDaily(Long farmId, String startAt, String endAt);

    /**
     * 刷新猪群指定时间段内每一天相关指标
     *
     * @param farmId  猪场id
     * @param pigType 猪群类型
     * @param startAt 开始时间 yyyy-MM-dd
     * @param endAt   结束时间 yyyy-MM-dd
     */
    Response<Boolean> flushGroupDaily(Long farmId, Integer pigType, String startAt, String endAt);

    /**
     * 刷新猪指定时间段内每一天相关指标
     *
     * @param farmId  猪场id
     * @param startAt 开始时间 yyyy-MM-dd
     * @param endAt   结束时间 yyyy-MM-dd
     */
    Response<Boolean> flushPigDaily(Long farmId, String startAt, String endAt);

    /**
     * 生成昨天和今天的报表包含同步数据
     *
     * @param farmIds 猪场ids
     */
    Response<Boolean> generateYesterdayAndToday(List<Long> farmIds, Date date);

    /**
     * 同步数据
     * @param farmIds 猪场ids
     */
    Response<Boolean> synchronize(List<Long> farmIds, Date date);

    /**
     * 全量同步报表数据
     *
     * @return
     */
    Response<Boolean> synchronizeFullBiData();

    /**
     * 增量同步报表数据
     *
     * @return
     */
    Response<Boolean> synchronizeDeltaDayBiData(Long farmId, Date start, Integer orzType);

    /**
     * 增量同步报表数据
     *
     * @return
     */
    Response<Boolean> synchronizeDelta(Long farmId, Date start, Integer orzType);

    /**
     * 查询某一时间维度的猪场报表数据
     *
     * @param dimensionCriteria 查询维度
     * @return 报表数据
     */
    Response<DoctorDimensionReport> dimensionReport(DoctorDimensionCriteria dimensionCriteria);


    /**
     * 查询过个猪场的存栏
     *
     * @param farmIdList 猪场id列表
     * @return
     */
    Response<List<DoctorFarmLiveStockDto>> findFarmsLiveStock(List<Long> farmIdList,Integer type);

    Response<Boolean> syncWarehouse(Date date);

    Response<Boolean> syncWarehouse(Integer dateType, Integer orgType);

    Response<Boolean> syncEfficiency(Date date);

    Response<Boolean> syncEfficiency(Integer dateType, Integer orgType);

    /**
     * 刷新单个猪场
     * 月、季、年
     * 猪场、公司
     *
     * @param farmId
     * @return
     */
    Response<Boolean> syncEfficiency(Long farmId);

    Response<List<DoctorFarmEarlyEventAtDto>> findEarLyAt();

    /**
     * 刷新分娩率
     * @param farmIds 猪场ids
     * @param date 刷新日期之后数据
     * @return
     */
    Response<Boolean> generateDeliverRate(List<Long> farmIds, Date date);

    /**
     * 刷新某一组织维度，指定日期后的各个时间维度的分娩率相关指标
     * @param orzId 组织id
     * @param orzType 组织类型
     * @param start 开始日期
     * @return
     */
    Response<Boolean> flushDeliverRate(Long orzId, Integer orzType, Date start);
}
