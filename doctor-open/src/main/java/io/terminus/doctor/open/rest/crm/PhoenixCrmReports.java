package io.terminus.doctor.open.rest.crm;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dto.report.common.DoctorGroupLiveStockDetailDto;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorMonthlyReport;
import io.terminus.doctor.event.service.DoctorCommonReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.open.dto.DoctorDailyReportOpen;
import io.terminus.doctor.open.dto.DoctorGroupLiveStockDetailOpen;
import io.terminus.doctor.open.dto.DoctorMonthlyReportOpen;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Desc: CRM相关接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/22
 */
@Slf4j
@OpenBean
public class PhoenixCrmReports {

    private final DoctorDailyReportReadService doctorDailyReportReadService;
    private final DoctorCommonReportReadService doctorCommonReportReadService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorGroupReadService doctorGroupReadService;

    private final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final JsonMapper MAPPER = JsonMapper.JSON_NON_EMPTY_MAPPER;

    @Value(value = "${open.domain}")
    private String domain;
    @Autowired
    public PhoenixCrmReports(DoctorDailyReportReadService doctorDailyReportReadService,
                             DoctorCommonReportReadService doctorCommonReportReadService,
                             DoctorFarmReadService doctorFarmReadService,
                             DoctorGroupReadService doctorGroupReadService) {
        this.doctorDailyReportReadService = doctorDailyReportReadService;
        this.doctorCommonReportReadService = doctorCommonReportReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorGroupReadService = doctorGroupReadService;
    }



    /**
     * 根据日期查询全部猪场的日报
     *
     * @param date 日期
     * @return 日报数据json
     */
    @OpenMethod(key = "get.daily.report", paramNames = "date")
    public String getDailyReport(@NotEmpty(message = "date.not.empty") String date) {
        Response<List<DoctorDailyReport>> dailyReportsResponse = doctorDailyReportReadService.findDailyReportBySumAt(DTF.parseDateTime(date).toDate());
        if (!dailyReportsResponse.isSuccess() || Arguments.isNullOrEmpty(dailyReportsResponse.getResult())) {
            return "";
        }
        Response<List<DoctorFarm>> farmsResponse = doctorFarmReadService.findAllFarms();
        if (!farmsResponse.isSuccess()) {
            return "";
        }
        Response<Map<Long, Integer>> mapResponse = doctorGroupReadService.queryFattenOutBySumAt(date);
        Map<Long, Integer> fattenOutMap = mapResponse.getResult();
        Map<Long, String> farmMap = farmsResponse.getResult().stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getName()));
        List<DoctorDailyReportOpen> doctorDailyReportDtos = dailyReportsResponse.getResult().stream().map(doctorDailyReport -> {
            DoctorDailyReportOpen doctorDailyReportOpen = new DoctorDailyReportOpen();
            BeanMapper.copy(doctorDailyReport.getReportData(), doctorDailyReportOpen);
            doctorDailyReportOpen.setFarmName(farmMap.get(doctorDailyReport.getFarmId()));
            doctorDailyReportOpen.getLiveStock().setFattenOut(fattenOutMap.get(doctorDailyReport.getFarmId()));
            return doctorDailyReportOpen;
        }).collect(Collectors.toList());
        return MAPPER.toJson(doctorDailyReportDtos);
    }

    /**
     * 根据统计日期查询月报
     * @param date 查询日期
     * @return 月报数据json
     */
    @OpenMethod(key = "get.monthly.report", paramNames = "date")
    public String getMonthlyReport(@NotEmpty(message = "date.not.empty") String date) {
        Response<List<DoctorMonthlyReport>> monthlyReportsResponse = doctorCommonReportReadService.findMonthlyReports(date);
        if (!monthlyReportsResponse.isSuccess() || Arguments.isNullOrEmpty(monthlyReportsResponse.getResult())) {
            return "";
        }
        Response<List<DoctorFarm>> farmsResponse = doctorFarmReadService.findAllFarms();
        if (!farmsResponse.isSuccess()) {
            return "";
        }
        Map<Long, String> farmMap = farmsResponse.getResult().stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getName()));

        List<DoctorMonthlyReportOpen> doctorCommonReportDtos = monthlyReportsResponse.getResult().stream().map(doctorMonthlyReport -> {
            DoctorMonthlyReportOpen doctorMonthlyReportOpen = MAPPER.fromJson(doctorMonthlyReport.getData(), DoctorMonthlyReportOpen.class);
            doctorMonthlyReportOpen.setDate(doctorMonthlyReport.getSumAt());
            doctorMonthlyReportOpen.setFarmName(farmMap.get(doctorMonthlyReport.getFarmId()));
            return doctorMonthlyReportOpen;
        }).collect(Collectors.toList());
        return MAPPER.toJson(doctorCommonReportDtos);
    }

    /**
     * 获取猪群存栏明细
     * @param date 查询日期
     * @return 猪群存栏明细json
     */
    @OpenMethod(key = "get.group.live.stock.detail", paramNames = "date")
    public String getGroupLiveStockDetail(@NotEmpty(message = "date.not.empty") String date) {
        Response<List<DoctorGroupLiveStockDetailDto>> detailDtoResponse = doctorCommonReportReadService.findEveryGroupInfo(date);
        if (!detailDtoResponse.isSuccess() || detailDtoResponse.getResult() == null) {
            return "";
        }
        List<DoctorGroupLiveStockDetailOpen> liveStockDetailOpenList = detailDtoResponse.getResult()
                .stream().map(dto -> MAPPER.getMapper().convertValue(dto, DoctorGroupLiveStockDetailOpen.class))
                .collect(Collectors.toList());
        DoctorGroupLiveStockDetailOpen doctorGroupLiveStockDetailOpen = new DoctorGroupLiveStockDetailOpen();
        BeanMapper.copy(detailDtoResponse.getResult(), doctorGroupLiveStockDetailOpen);
        return MAPPER.toJson(liveStockDetailOpenList);
    }

    @OpenMethod(key = "get.shop.item.sale")
    public String getShopItemSale(){
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        try {
            String url = "http://"+ domain + "/api/queryShopItemSaleOpen";
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.connect();
            InputStream in = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder builder = new StringBuilder();
            String line ;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return MAPPER.toJson(builder.toString());
        } catch (Exception e) {
            log.error("get.shop.item.sale.failed, cause:{}", Throwables.getStackTraceAsString(e));
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                log.error("get.shop.item.sale.failed");
            }
        }
        return "";
   }
}
