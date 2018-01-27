package io.terminus.doctor.event.service;


import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.report.daily.DoctorFarmLiveStockDto;
import io.terminus.doctor.event.dto.reportBi.DoctorDimensionReport;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * 生成昨天和今天的报表
     *
     * @param farmIds 猪场ids
     */
    Response<Boolean> generateYesterdayAndToday(List<Long> farmIds);

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
    Response<Boolean> synchronizeDeltaDayBiData(Long farmId, Date start);

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
    Response<List<DoctorFarmLiveStockDto>> findFarmsLiveStock(List<Long> farmIdList);

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

    Response<Map<Long, Date>> findEarLyAt();
}
