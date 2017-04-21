package io.terminus.doctor.open.rest.crm;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dto.report.common.DoctorGroupLiveStockDetailDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorBaseReport;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorGroupChangeSum;
import io.terminus.doctor.event.service.DoctorCommonReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.doctor.open.dto.DoctorDailyReportOpen;
import io.terminus.doctor.open.dto.DoctorGroupLiveStockDetailOpen;
import io.terminus.doctor.open.dto.DoctorMonthlyReportOpen;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.pampas.openplatform.annotations.OpenBean;
import io.terminus.pampas.openplatform.annotations.OpenMethod;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotEmpty;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.net.HttpURLConnection;
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
    private final JsonMapperUtil MAPPER = JsonMapperUtil.JSON_NON_EMPTY_MAPPER;

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
        Response<List<DoctorDailyReportDto>> dailyReportsResponse = doctorDailyReportReadService.findDailyReportBySumAt(DTF.parseDateTime(date).toDate());
        if (!dailyReportsResponse.isSuccess() || Arguments.isNullOrEmpty(dailyReportsResponse.getResult())) {
            return "";
        }
        Response<List<DoctorFarm>> farmsResponse = doctorFarmReadService.findAllFarms();
        if (!farmsResponse.isSuccess()) {
            return "";
        }
        Response<Map<Long, Integer>> mapResponse = doctorGroupReadService.queryFattenOutBySumAt(date);
//        Map<Long, Integer> fattenOutMap = mapResponse.getResult();
        Map<Long, String> farmMap = farmsResponse.getResult().stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getName()));
        List<DoctorDailyReportOpen> doctorDailyReportDtos = dailyReportsResponse.getResult().stream().map(doctorDailyReportDto -> {
            DoctorDailyReportOpen doctorDailyReportOpen = new DoctorDailyReportOpen();
//            BeanMapper.copy(doctorDailyReport.getReportData(), doctorDailyReportOpen);
            DoctorDailyReport pigDailyReport = doctorDailyReportDto.getDailyReport();
            DoctorGroupChangeSum groupDailyReport = doctorDailyReportDto.getGroupChangeSum();

            doctorDailyReportOpen.setFarmName(farmMap.get(pigDailyReport.getFarmId()));
            doctorDailyReportOpen.setSumAt(DateTime.parse(pigDailyReport.getSumAt()).toDate());

            doctorDailyReportOpen.getMating().setHoubei(pigDailyReport.getMateHb());
            doctorDailyReportOpen.getMating().setDuannai(pigDailyReport.getMateDn());
            doctorDailyReportOpen.getMating().setFanqing(pigDailyReport.getMateFq());
            doctorDailyReportOpen.getMating().setLiuchan(pigDailyReport.getMateLc());

            doctorDailyReportOpen.getCheckPreg().setPositive(pigDailyReport.getPregPositive());
            doctorDailyReportOpen.getCheckPreg().setNegative(pigDailyReport.getPregNegative());
            doctorDailyReportOpen.getCheckPreg().setFanqing(pigDailyReport.getPregFanqing());
            doctorDailyReportOpen.getCheckPreg().setLiuchan(pigDailyReport.getPregLiuchan());

            doctorDailyReportOpen.getDeliver().setNest(pigDailyReport.getFarrowNest());
            doctorDailyReportOpen.getDeliver().setLive(pigDailyReport.getFarrowLive());
            doctorDailyReportOpen.getDeliver().setHealth(pigDailyReport.getFarrowHealth());
            doctorDailyReportOpen.getDeliver().setWeak(pigDailyReport.getFarrowWeak());
            doctorDailyReportOpen.getDeliver().setBlack(pigDailyReport.getFarrowSjmh());

            doctorDailyReportOpen.getWean().setCount(pigDailyReport.getWeanCount());
            doctorDailyReportOpen.getWean().setWeight(pigDailyReport.getWeanAvgWeight());

            doctorDailyReportOpen.getLiveStock().setPeihuaiSow(pigDailyReport.getSowPh());
            doctorDailyReportOpen.getLiveStock().setBuruSow(pigDailyReport.getSowCf());
            doctorDailyReportOpen.getLiveStock().setHoubeiSow(groupDailyReport.getHoubeiEnd());
            doctorDailyReportOpen.getLiveStock().setBoar(pigDailyReport.getBoarEnd());
            doctorDailyReportOpen.getLiveStock().setFarrow(groupDailyReport.getFarrowEnd());
            doctorDailyReportOpen.getLiveStock().setNursery(groupDailyReport.getNurseryEnd());
            doctorDailyReportOpen.getLiveStock().setFatten(groupDailyReport.getFattenEnd());
            doctorDailyReportOpen.getLiveStock().setFattenOut(doctorDailyReportDto.getFattenWillOut());

            doctorDailyReportOpen.getDead().setSow(pigDailyReport.getSowDead());
            doctorDailyReportOpen.getDead().setBoar(pigDailyReport.getBoarDead());
            doctorDailyReportOpen.getDead().setFarrow(groupDailyReport.getFarrowDead());
            doctorDailyReportOpen.getDead().setNursery(groupDailyReport.getNurseryDead());
            doctorDailyReportOpen.getDead().setFatten(groupDailyReport.getFattenDead());

            doctorDailyReportOpen.getSale().setSow(pigDailyReport.getSowSale());
            doctorDailyReportOpen.getSale().setBoar(pigDailyReport.getBoarSale());
            doctorDailyReportOpen.getSale().setNursery(EventUtil.plusInt(groupDailyReport.getFarrowSale(), groupDailyReport.getNurserySale()));
            doctorDailyReportOpen.getSale().setFatten(groupDailyReport.getFattenSale());

            return doctorDailyReportOpen;
        }).collect(Collectors.toList());
        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(doctorDailyReportDtos);
    }

    /**
     * 根据统计日期查询月报
     * @param date 查询日期
     * @return 月报数据json
     */
    @OpenMethod(key = "get.monthly.report", paramNames = "date")
    public String getMonthlyReport(@NotEmpty(message = "date.not.empty") String date) {
//        Response<List<DoctorMonthlyReport>> monthlyReportsResponse = doctorCommonReportReadService.findMonthlyReports(date);
//        if (!monthlyReportsResponse.isSuccess() || Arguments.isNullOrEmpty(monthlyReportsResponse.getResult())) {
//            return "";
//        }
//        Response<List<DoctorFarm>> farmsResponse = doctorFarmReadService.findAllFarms();
//        if (!farmsResponse.isSuccess()) {
//            return "";
//        }
//        Map<Long, String> farmMap = farmsResponse.getResult().stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getName()));
//
//        List<DoctorMonthlyReportOpen> doctorCommonReportDtos = monthlyReportsResponse.getResult().stream().map(doctorMonthlyReport -> {
//            DoctorMonthlyReportOpen doctorMonthlyReportOpen = MAPPER.fromJson(doctorMonthlyReport.getData(), DoctorMonthlyReportOpen.class);
//            doctorMonthlyReportOpen.setDate(doctorMonthlyReport.getSumAt());
//            doctorMonthlyReportOpen.setFarmName(farmMap.get(doctorMonthlyReport.getFarmId()));
//            return doctorMonthlyReportOpen;
//        }).collect(Collectors.toList());
//        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(doctorCommonReportDtos);
        return null;
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
        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(liveStockDetailOpenList);
    }

    /**
     * 电商商品销售
     * @return
     */
    @OpenMethod(key = "get.shop.item.sale")
    public String getShopItemSale(){
        BufferedReader reader = null;
        HttpURLConnection urlConnection = null;
        try {
            String url = "http://"+ domain + "/api/queryShopItemSaleOpen";
            return HttpRequest.get(url).body();
        } catch (Exception e) {
            log.error("get.shop.item.sale.failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
        return "";
    }
}
