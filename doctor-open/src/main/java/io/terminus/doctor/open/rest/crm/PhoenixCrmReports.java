package io.terminus.doctor.open.rest.crm;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.api.client.util.Lists;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.report.common.DoctorCommonReportDto;
import io.terminus.doctor.event.dto.report.daily.DoctorCheckPregDailyReport;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import io.terminus.doctor.event.model.DoctorDailyGroup;
import io.terminus.doctor.event.model.DoctorDailyReport;
import io.terminus.doctor.event.model.DoctorDailyReportSum;
import io.terminus.doctor.event.model.DoctorGroupChangeSum;
import io.terminus.doctor.event.model.DoctorRangeReport;
import io.terminus.doctor.event.service.DoctorCommonReportReadService;
import io.terminus.doctor.event.service.DoctorDailyGroupReadService;
import io.terminus.doctor.event.service.DoctorDailyReportReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.util.EventUtil;
import io.terminus.doctor.open.dto.DoctorDailyReportOpen;
import io.terminus.doctor.open.dto.DoctorDeadDailyReportOpen;
import io.terminus.doctor.open.dto.DoctorGroupLiveStockDetailOpen;
import io.terminus.doctor.open.dto.DoctorLiveStockDailyReportOpen;
import io.terminus.doctor.open.dto.DoctorMatingDailyReportOpen;
import io.terminus.doctor.open.dto.DoctorMonthlyReportOpen;
import io.terminus.doctor.open.dto.DoctorSaleDailyReportOpen;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
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
    private final PrimaryUserReadService primaryUserReadService;

    private final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");
    private final JsonMapperUtil MAPPER = JsonMapperUtil.JSON_NON_EMPTY_MAPPER;

    @Value(value = "${open.domain}")
    private String domain;
    @Autowired
    public PhoenixCrmReports(DoctorDailyReportReadService doctorDailyReportReadService,
                             DoctorCommonReportReadService doctorCommonReportReadService,
                             DoctorFarmReadService doctorFarmReadService,
                             DoctorDailyGroupReadService doctorDailyGroupReadService,
                             DoctorGroupReadService doctorGroupReadService,
                             PrimaryUserReadService primaryUserReadService) {
        this.doctorDailyReportReadService = doctorDailyReportReadService;
        this.doctorCommonReportReadService = doctorCommonReportReadService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorDailyGroupReadService = doctorDailyGroupReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.primaryUserReadService = primaryUserReadService;
    }



    /**
     * 根据日期查询全部猪场的日报
     *
     * @param date 日期 2017-01-01
     * @return 日报数据json
     */
    @OpenMethod(key = "get.daily.report", paramNames = "date")
    public String getDailyReport(@NotEmpty(message = "date.not.empty") String date) {
        Response<List<DoctorDailyReportDto>> dailyReportsResponse = doctorDailyReportReadService.findDailyReportBySumAt(DTF.parseDateTime(date).toDate());
        if (!dailyReportsResponse.isSuccess() || Arguments.isNullOrEmpty(dailyReportsResponse.getResult())) {
            return "";
        }
        Response<Map<Long, String>> mapResponse = primaryUserReadService.findFarmIdToUserName();
        if (!mapResponse.isSuccess()) {
            return "";
        }
        Response<List<DoctorFarm>> farmsResponse = doctorFarmReadService.findAllFarms();
        if (!farmsResponse.isSuccess()) {
            return "";
        }
        Response<Map<Long, Integer>> mapResponse1 = doctorGroupReadService.findFarmToGroupCount();
        if (!mapResponse1.isSuccess()) {
            return "";
        }
        Map<Long, Integer> farmToGroupCountMap = mapResponse1.getResult();
        Map<Long, String> farmIdToFarmNameMap = farmsResponse.getResult().stream().collect(Collectors.toMap(DoctorFarm::getId, DoctorFarm::getName));
        Map<Long, String> farmToUserNameMap = mapResponse.getResult();

        List<DoctorDailyReportOpen> doctorDailyReportDtos = dailyReportsResponse.getResult().stream().map(doctorDailyReportDto -> {
            DoctorDailyReportOpen doctorDailyReportOpen = new DoctorDailyReportOpen();
            DoctorDailyReport pigDailyReport = MoreObjects.firstNonNull(doctorDailyReportDto.getDailyReport(), new DoctorDailyReport());
            DoctorGroupChangeSum groupDailyReport = MoreObjects.firstNonNull(doctorDailyReportDto.getGroupChangeSum(), new DoctorGroupChangeSum());

            doctorDailyReportOpen.setFarmName(farmIdToFarmNameMap.get(pigDailyReport.getFarmId()));
            doctorDailyReportOpen.setUserName(farmToUserNameMap.get(pigDailyReport.getFarmId()));
            doctorDailyReportOpen.setSumAt(DateTime.parse(pigDailyReport.getSumAt()).toDate());

            doctorDailyReportOpen.getWean().setCount(MoreObjects.firstNonNull(pigDailyReport.getWeanCount(), 0));
            doctorDailyReportOpen.getWean().setWeight(MoreObjects.firstNonNull(pigDailyReport.getWeanAvgWeight(), 0.0));

            doctorDailyReportOpen.getDeliver().setNest(MoreObjects.firstNonNull(pigDailyReport.getFarrowNest(), 0));
            doctorDailyReportOpen.getDeliver().setLive(MoreObjects.firstNonNull(pigDailyReport.getFarrowLive(), 0));
            doctorDailyReportOpen.getDeliver().setHealth(MoreObjects.firstNonNull(pigDailyReport.getFarrowHealth(), 0));
            doctorDailyReportOpen.getDeliver().setWeak(MoreObjects.firstNonNull(pigDailyReport.getFarrowWeak(), 0));
            doctorDailyReportOpen.getDeliver().setBlack(MoreObjects.firstNonNull(pigDailyReport.getFarrowSjmh(), 0));

            doctorDailyReportOpen.getCheckPreg().setPositive(MoreObjects.firstNonNull(pigDailyReport.getPregPositive(), 0));
            doctorDailyReportOpen.getCheckPreg().setNegative(MoreObjects.firstNonNull(pigDailyReport.getPregNegative(), 0));
            doctorDailyReportOpen.getCheckPreg().setFanqing(MoreObjects.firstNonNull(pigDailyReport.getPregFanqing(), 0));
            doctorDailyReportOpen.getCheckPreg().setLiuchan(MoreObjects.firstNonNull(pigDailyReport.getPregLiuchan(), 0));
            DoctorCheckPregDailyReport checkPreg = doctorDailyReportOpen.getCheckPreg();
            checkPreg.setPregTotal(checkPreg.getPositive() + checkPreg.getNegative()
                    + checkPreg.getFanqing() + checkPreg.getLiuchan());

            doctorDailyReportOpen.getMating().setHoubei(MoreObjects.firstNonNull(pigDailyReport.getMateHb(), 0));
            doctorDailyReportOpen.getMating().setDuannai(MoreObjects.firstNonNull(pigDailyReport.getMateDn(), 0));
            doctorDailyReportOpen.getMating().setFanqing(MoreObjects.firstNonNull(pigDailyReport.getMateFq(), 0));
            doctorDailyReportOpen.getMating().setLiuchan(MoreObjects.firstNonNull(pigDailyReport.getMateLc(), 0));
            DoctorMatingDailyReportOpen mating = doctorDailyReportOpen.getMating();
            mating.setMatingTotal(mating.getHoubei() + mating.getDuannai()
                    + mating.getFanqing() + mating.getLiuchan());

            doctorDailyReportOpen.getLiveStock().setPeihuaiSow(MoreObjects.firstNonNull(pigDailyReport.getSowPh(), 0));
            doctorDailyReportOpen.getLiveStock().setBuruSow(MoreObjects.firstNonNull(pigDailyReport.getSowCf(), 0));
            doctorDailyReportOpen.getLiveStock().setSowTotal(MoreObjects.firstNonNull(pigDailyReport.getSowPh() + pigDailyReport.getSowCf(), 0));
            doctorDailyReportOpen.getLiveStock().setGroup(MoreObjects.firstNonNull(farmToGroupCountMap.get(pigDailyReport.getFarmId()), 0));

//            doctorDailyReportOpen.getLiveStock().setHoubeiSow(groupDailyReport.getHoubeiEnd());
            doctorDailyReportOpen.getLiveStock().setBoar(MoreObjects.firstNonNull(pigDailyReport.getBoarEnd(), 0));
            doctorDailyReportOpen.getLiveStock().setFarrow(MoreObjects.firstNonNull(groupDailyReport.getFarrowEnd(), 0));
            doctorDailyReportOpen.getLiveStock().setNursery(MoreObjects.firstNonNull(groupDailyReport.getNurseryEnd(), 0));
            doctorDailyReportOpen.getLiveStock().setFatten(MoreObjects.firstNonNull(groupDailyReport.getFattenEnd(), 0));
            doctorDailyReportOpen.getLiveStock().setFattenOut(MoreObjects.firstNonNull(doctorDailyReportDto.getFattenWillOut(), 0));
            DoctorLiveStockDailyReportOpen liveStock = doctorDailyReportOpen.getLiveStock();
            liveStock.setLiveStockTotal(liveStock.getSowTotal() + liveStock.getBoar() + liveStock.getFarrow()
                    + liveStock.getNursery() + liveStock.getFatten() + liveStock.getFattenOut());

            doctorDailyReportOpen.getDead().setSow(MoreObjects.firstNonNull(pigDailyReport.getSowDead(), 0));
            doctorDailyReportOpen.getDead().setBoar(MoreObjects.firstNonNull(pigDailyReport.getBoarDead(), 0));
            doctorDailyReportOpen.getDead().setFarrow(MoreObjects.firstNonNull(groupDailyReport.getFarrowDead(), 0));
            doctorDailyReportOpen.getDead().setNursery(MoreObjects.firstNonNull(groupDailyReport.getNurseryDead(), 0));
            doctorDailyReportOpen.getDead().setFatten(MoreObjects.firstNonNull(groupDailyReport.getFattenDead(), 0));
            DoctorDeadDailyReportOpen dead = doctorDailyReportOpen.getDead();
            dead.setDeadTotal(dead.getSow() + dead.getBoar() + dead.getFarrow() + dead.getNursery() + dead.getFatten());

            doctorDailyReportOpen.getSale().setSow(MoreObjects.firstNonNull(pigDailyReport.getSowSale(), 0));
            doctorDailyReportOpen.getSale().setBoar(MoreObjects.firstNonNull(pigDailyReport.getBoarSale(), 0));
            doctorDailyReportOpen.getSale().setNursery(EventUtil.plusInt(groupDailyReport.getFarrowSale(), groupDailyReport.getNurserySale()));
            doctorDailyReportOpen.getSale().setFatten(MoreObjects.firstNonNull(groupDailyReport.getFattenSale(), 0));
            DoctorSaleDailyReportOpen sale = doctorDailyReportOpen.getSale();
            sale.setSaleTotal(sale.getSow() + sale.getBoar() + sale.getNursery() + sale.getFatten());

            return doctorDailyReportOpen;
        }).collect(Collectors.toList());
        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(doctorDailyReportDtos);
    }

    /**
     * 根据统计日期查询月报
     * @param date 查询日期 2017-01
     * @return 月报数据json
     */
    @OpenMethod(key = "get.monthly.report", paramNames = "date")
    public String getMonthlyReport(@NotEmpty(message = "date.not.empty") String date) {
        Response<List<DoctorCommonReportDto>> monthlyReportsResponse = doctorCommonReportReadService.findMonthlyReports(date);
        if (!monthlyReportsResponse.isSuccess() || Arguments.isNullOrEmpty(monthlyReportsResponse.getResult())) {
            return "";
        }
        Response<Map<Long, String>> mapResponse = primaryUserReadService.findFarmIdToUserName();
        if (!mapResponse.isSuccess()) {
            return "";
        }

        Map<Long, String> farmToUserNameMap = mapResponse.getResult();
        List<DoctorMonthlyReportOpen> doctorCommonReportDtos = monthlyReportsResponse.getResult().stream().map(doctorCommonReportDto -> {
            DoctorMonthlyReportOpen doctorMonthlyReportOpen = new DoctorMonthlyReportOpen();

            doctorMonthlyReportOpen.setUserName(farmToUserNameMap.get(doctorCommonReportDto.getFarmId()));
            DoctorDailyReportSum dailyReportSum = MoreObjects.firstNonNull(doctorCommonReportDto.getChangeReport(), new DoctorDailyReportSum());
            DoctorGroupChangeSum groupChangeSum = MoreObjects.firstNonNull(doctorCommonReportDto.getGroupChangeReport(), new DoctorGroupChangeSum());
            DoctorRangeReport indicatorReport = MoreObjects.firstNonNull(doctorCommonReportDto.getIndicatorReport(), new DoctorRangeReport());

            doctorMonthlyReportOpen.setMateHoubei(MoreObjects.firstNonNull(dailyReportSum.getMateHb(), 0));
            doctorMonthlyReportOpen.setMateFanqing(MoreObjects.firstNonNull(dailyReportSum.getMateFq(), 0));
            doctorMonthlyReportOpen.setMateAbort(MoreObjects.firstNonNull(dailyReportSum.getMateLc(), 0));
            doctorMonthlyReportOpen.setMateWean(MoreObjects.firstNonNull(dailyReportSum.getMateDn(), 0));
            doctorMonthlyReportOpen.setMateNegtive(MoreObjects.firstNonNull(dailyReportSum.getMateYx(), 0));
            doctorMonthlyReportOpen.setMateEstimatePregRate(MoreObjects.firstNonNull(indicatorReport.getMateEstimatePregRate(), 0.0));
            doctorMonthlyReportOpen.setMateRealPregRate(MoreObjects.firstNonNull(indicatorReport.getMateRealPregRate(), 0.0));
            doctorMonthlyReportOpen.setMateEstimateFarrowingRate(MoreObjects.firstNonNull(indicatorReport.getMateEstimateFarrowingRate(), 0.0));
            doctorMonthlyReportOpen.setMateRealFarrowingRate(MoreObjects.firstNonNull(indicatorReport.getMateRealFarrowingRate(), 0.0));

            doctorMonthlyReportOpen.setFarrowNest(MoreObjects.firstNonNull(dailyReportSum.getFarrowNest(), 0));
            doctorMonthlyReportOpen.setFarrowEstimateParity(MoreObjects.firstNonNull(dailyReportSum.getPreFarrowCount(), 0));
            doctorMonthlyReportOpen.setFarrowAll(MoreObjects.firstNonNull(dailyReportSum.getFarrowAll(), 0));
            doctorMonthlyReportOpen.setFarrowAlive(MoreObjects.firstNonNull(dailyReportSum.getFarrowLive(), 0));
            doctorMonthlyReportOpen.setFarrowHealth(MoreObjects.firstNonNull(dailyReportSum.getFarrowHealth(), 0));
            doctorMonthlyReportOpen.setFarrowWeak(MoreObjects.firstNonNull(dailyReportSum.getFarrowWeak(), 0));
            doctorMonthlyReportOpen.setFarrowDead(MoreObjects.firstNonNull(dailyReportSum.getFarrowDead(), 0));
            doctorMonthlyReportOpen.setFarrowMny(MoreObjects.firstNonNull(dailyReportSum.getFarrowMny(), 0));
            doctorMonthlyReportOpen.setFarrowAvgAll((Arguments.isNull(dailyReportSum.getFarrowNest()) || dailyReportSum.getFarrowNest() == 0) ? 0 : dailyReportSum.getFarrowAll()/dailyReportSum.getFarrowNest());
            doctorMonthlyReportOpen.setFarrowAvgAlive(dailyReportSum.getFarrowAvgLive());
            doctorMonthlyReportOpen.setFarrowAvgHealth(dailyReportSum.getFarrowAvgHealth());

            doctorMonthlyReportOpen.setCheckPositive(MoreObjects.firstNonNull(dailyReportSum.getPregPositive(), 0));
            doctorMonthlyReportOpen.setCheckFanqing(MoreObjects.firstNonNull(dailyReportSum.getPregFanqing(), 0));
            doctorMonthlyReportOpen.setCheckAbort(MoreObjects.firstNonNull(dailyReportSum.getPregLiuchan(), 0));
            doctorMonthlyReportOpen.setCheckNegtive(MoreObjects.firstNonNull(dailyReportSum.getPregNegative(), 0));

            doctorMonthlyReportOpen.setNpd(MoreObjects.firstNonNull(indicatorReport.getNpd(), 0.0));
            doctorMonthlyReportOpen.setPsy(MoreObjects.firstNonNull(indicatorReport.getPsy(), 0.0));
            doctorMonthlyReportOpen.setMateInSeven(MoreObjects.firstNonNull(indicatorReport.getMateInSeven(), 0.0));

            doctorMonthlyReportOpen.setSaleSow(MoreObjects.firstNonNull(dailyReportSum.getSowSale(), 0));
            doctorMonthlyReportOpen.setSaleBoar(MoreObjects.firstNonNull(dailyReportSum.getBoarSale(), 0));
            doctorMonthlyReportOpen.setSaleNursery(MoreObjects.firstNonNull(groupChangeSum.getNurserySale(), 0));
            doctorMonthlyReportOpen.setSaleFatten(MoreObjects.firstNonNull(groupChangeSum.getFattenSale(), 0));
            doctorMonthlyReportOpen.setSaleTotal(doctorMonthlyReportOpen.getSaleSow()
                    + doctorMonthlyReportOpen.getSaleBoar()
                    + doctorMonthlyReportOpen.getSaleNursery()
                    + doctorMonthlyReportOpen.getSaleFatten());
            doctorMonthlyReportOpen.setSaleFarrow(MoreObjects.firstNonNull(groupChangeSum.getFarrowSale(), 0));

            doctorMonthlyReportOpen.setDate(DateUtil.getMonthEndOrToday(DateTime.parse(doctorCommonReportDto.getDate(), DateUtil.YYYYMM)));
            return doctorMonthlyReportOpen;
        }).collect(Collectors.toList());
        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(doctorCommonReportDtos);
    }

    /**
     * 获取猪群存栏明细
     * @param date 查询日期 2017-01-01
     * @return 猪群存栏明细json
     */
    @OpenMethod(key = "get.group.live.stock.detail", paramNames = "date")
    public String getGroupLiveStockDetail(@NotEmpty(message = "date.not.empty") String date) {
        Response<List<DoctorDailyGroup>> dailyGroups = doctorDailyGroupReadService.findGroupInfoBySumAt(date);
        if (!dailyGroups.isSuccess() || dailyGroups.getResult() == null) {
            return "";
        }
        Response<Map<Long, String>> mapResponse = primaryUserReadService.findFarmIdToUserName();
        if (!mapResponse.isSuccess()) {
            return "";
        }
        Map<Long, String> farmToUserNameMap = mapResponse.getResult();
        List<DoctorGroupLiveStockDetailOpen> liveStockDetailOpenList = Lists.newArrayList();
        dailyGroups.getResult().forEach(doctorDailyGroup -> {
            DoctorGroupDetail groupDetail = RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(doctorDailyGroup.getGroupId()));
            DoctorGroupLiveStockDetailOpen doctorGroupLiveStockDetailOpen = new DoctorGroupLiveStockDetailOpen();
            doctorGroupLiveStockDetailOpen.setUserName(farmToUserNameMap.get(doctorDailyGroup.getFarmId()));
            doctorGroupLiveStockDetailOpen.setGroupCode(groupDetail.getGroup().getGroupCode());
            doctorGroupLiveStockDetailOpen.setLiveStocks(MoreObjects.firstNonNull(doctorDailyGroup.getEnd(), 0));
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
