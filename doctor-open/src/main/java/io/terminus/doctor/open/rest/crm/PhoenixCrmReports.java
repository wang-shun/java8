package io.terminus.doctor.open.rest.crm;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.api.client.util.Lists;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.*;
import io.terminus.doctor.event.service.*;
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
    private final DoctorDailyGroupReadService doctorDailyGroupReadService;
    private final DoctorGroupReadService doctorGroupReadService;

    private final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final JsonMapperUtil MAPPER = JsonMapperUtil.JSON_NON_EMPTY_MAPPER;

    @Value(value = "${open.domain}")
    private String domain;
    @Autowired
    public PhoenixCrmReports(DoctorDailyReportReadService doctorDailyReportReadService,
                             DoctorCommonReportReadService doctorCommonReportReadService,
                             DoctorFarmReadService doctorFarmReadService,
                             DoctorDailyGroupReadService doctorDailyGroupReadService,
                             DoctorGroupReadService doctorGroupReadService) {
        this.doctorDailyReportReadService = doctorDailyReportReadService;
        this.doctorCommonReportReadService = doctorCommonReportReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorDailyGroupReadService = doctorDailyGroupReadService;
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
        Map<Long, String> farmMap = farmsResponse.getResult().stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getName()));
        List<DoctorDailyReportOpen> doctorDailyReportDtos = dailyReportsResponse.getResult().stream().map(doctorDailyReportDto -> {
            DoctorDailyReportOpen doctorDailyReportOpen = new DoctorDailyReportOpen();
            DoctorDailyReport pigDailyReport = MoreObjects.firstNonNull(doctorDailyReportDto.getDailyReport(), new DoctorDailyReport());
            DoctorGroupChangeSum groupDailyReport = MoreObjects.firstNonNull(doctorDailyReportDto.getGroupChangeSum(), new DoctorGroupChangeSum());

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
        Response<List<DoctorCommonReportDto>> monthlyReportsResponse = doctorCommonReportReadService.findMonthlyReports(date);
        if (!monthlyReportsResponse.isSuccess() || Arguments.isNullOrEmpty(monthlyReportsResponse.getResult())) {
            return "";
        }
        Response<List<DoctorFarm>> farmsResponse = doctorFarmReadService.findAllFarms();
        if (!farmsResponse.isSuccess()) {
            return "";
        }
        Map<Long, String> farmMap = farmsResponse.getResult().stream().collect(Collectors.toMap(k -> k.getId(), v -> v.getName()));

        List<DoctorMonthlyReportOpen> doctorCommonReportDtos = monthlyReportsResponse.getResult().stream().map(doctorCommonReportDto -> {
            DoctorMonthlyReportOpen doctorMonthlyReportOpen = new DoctorMonthlyReportOpen();
            DoctorDailyReportSum dailyReportSum = MoreObjects.firstNonNull(doctorCommonReportDto.getChangeReport(), new DoctorDailyReportSum());
            DoctorGroupChangeSum groupChangeSum = MoreObjects.firstNonNull(doctorCommonReportDto.getGroupChangeReport(), new DoctorGroupChangeSum());
            DoctorRangeReport indicatorReport = MoreObjects.firstNonNull(doctorCommonReportDto.getIndicatorReport(), new DoctorRangeReport());
            doctorMonthlyReportOpen.setMateHoubei(dailyReportSum.getMateHb());
            doctorMonthlyReportOpen.setMateFanqing(dailyReportSum.getMateFq());
            doctorMonthlyReportOpen.setMateAbort(dailyReportSum.getMateLc());
            doctorMonthlyReportOpen.setMateWean(dailyReportSum.getMateDn());
            doctorMonthlyReportOpen.setMateNegtive(dailyReportSum.getMateYx());
            doctorMonthlyReportOpen.setMateEstimatePregRate(indicatorReport.getMateEstimatePregRate());
            doctorMonthlyReportOpen.setMateRealPregRate(indicatorReport.getMateRealPregRate());
            doctorMonthlyReportOpen.setMateEstimateFarrowingRate(indicatorReport.getMateEstimateFarrowingRate());
            doctorMonthlyReportOpen.setMateRealFarrowingRate(indicatorReport.getMateRealFarrowingRate());

            doctorMonthlyReportOpen.setFarrowNest(dailyReportSum.getFarrowNest());
            doctorMonthlyReportOpen.setFarrowEstimateParity(dailyReportSum.getPreFarrowCount());
            doctorMonthlyReportOpen.setFarrowAll(dailyReportSum.getFarrowAll());
            doctorMonthlyReportOpen.setFarrowAlive(dailyReportSum.getFarrowLive());
            doctorMonthlyReportOpen.setFarrowHealth(dailyReportSum.getFarrowHealth());
            doctorMonthlyReportOpen.setFarrowWeak(dailyReportSum.getFarrowWeak());
            doctorMonthlyReportOpen.setFarrowDead(dailyReportSum.getFarrowDead());
            doctorMonthlyReportOpen.setFarrowMny(dailyReportSum.getFarrowMny());
            doctorMonthlyReportOpen.setFarrowAvgAll((Arguments.isNull(dailyReportSum.getFarrowNest()) || dailyReportSum.getFarrowNest() == 0) ? 0 : dailyReportSum.getFarrowAll()/dailyReportSum.getFarrowNest());
            doctorMonthlyReportOpen.setFarrowAvgAlive(dailyReportSum.getFarrowAvgLive());
            doctorMonthlyReportOpen.setFarrowAvgHealth(dailyReportSum.getFarrowAvgHealth());

            doctorMonthlyReportOpen.setNpd(indicatorReport.getNpd());
            doctorMonthlyReportOpen.setPsy(indicatorReport.getPsy());
            doctorMonthlyReportOpen.setMateInSeven(indicatorReport.getMateInSeven());

            doctorMonthlyReportOpen.setSaleSow(dailyReportSum.getSowSale());
            doctorMonthlyReportOpen.setSaleBoar(dailyReportSum.getBoarSale());
            doctorMonthlyReportOpen.setSaleFarrow(groupChangeSum.getFarrowSale());
            doctorMonthlyReportOpen.setSaleNursery(groupChangeSum.getNurserySale());
            doctorMonthlyReportOpen.setSaleFatten(groupChangeSum.getFattenSale());

            doctorMonthlyReportOpen.setDate(DateUtil.getMonthEndOrToday(DateTime.parse(doctorCommonReportDto.getDate(), DateUtil.YYYYMM)));
            doctorMonthlyReportOpen.setFarmName(farmMap.get(doctorCommonReportDto.getFarmId()));
            return doctorMonthlyReportOpen;
        }).collect(Collectors.toList());
        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(doctorCommonReportDtos);
    }

    /**
     * 获取猪群存栏明细
     * @param date 查询日期
     * @return 猪群存栏明细json
     */
    @OpenMethod(key = "get.group.live.stock.detail", paramNames = "date")
    public String getGroupLiveStockDetail(@NotEmpty(message = "date.not.empty") String date) {
        Response<List<DoctorDailyGroup>> dailyGroups = doctorDailyGroupReadService.findGroupInfoBySumAt(date);
        if (!dailyGroups.isSuccess() || dailyGroups.getResult() == null) {
            return "";
        }
        List<DoctorGroupLiveStockDetailOpen> liveStockDetailOpenList = Lists.newArrayList();
        dailyGroups.getResult().forEach(doctorDailyGroup -> {
            DoctorGroupDetail groupDetail = RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(doctorDailyGroup.getGroupId()));
            DoctorGroupLiveStockDetailOpen doctorGroupLiveStockDetailOpen = new DoctorGroupLiveStockDetailOpen();
            doctorGroupLiveStockDetailOpen.setFarmName(groupDetail.getGroup().getFarmName());
            doctorGroupLiveStockDetailOpen.setGroupCode(groupDetail.getGroup().getGroupCode());
            doctorGroupLiveStockDetailOpen.setLiveStocks(doctorDailyGroup.getEnd());
            doctorGroupLiveStockDetailOpen.setType(PigType.from(doctorDailyGroup.getType()).getName());
            doctorGroupLiveStockDetailOpen.setSumAt(doctorDailyGroup.getSumAt());
            doctorGroupLiveStockDetailOpen.setDayAge(DateUtil.getDeltaDaysAbs(groupDetail.getGroupTrack().getBirthDate(), doctorDailyGroup.getSumAt()));
            liveStockDetailOpenList.add(doctorGroupLiveStockDetailOpen);
        });
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
