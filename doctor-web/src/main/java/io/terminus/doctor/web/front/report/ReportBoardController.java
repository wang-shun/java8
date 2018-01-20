package io.terminus.doctor.web.front.report;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesReadService;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.report.daily.DoctorFarmLiveStockDto;
import io.terminus.doctor.event.dto.reportBi.DoctorDimensionReport;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.DoctorReportRegion;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.model.DoctorReportBoar;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import io.terminus.doctor.event.model.DoctorReportFatten;
import io.terminus.doctor.event.model.DoctorReportMaterial;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.model.DoctorReportNursery;
import io.terminus.doctor.event.model.DoctorReportReserve;
import io.terminus.doctor.event.model.DoctorReportSow;
import io.terminus.doctor.event.service.DoctorDailyReportV2Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Created by sunbo@terminus.io on 2018/1/3.
 */
@Api("猪场看板报表")
@Slf4j
@RestController
@RequestMapping("/api/doctor/report/board/{farmId}/")
public class ReportBoardController {

    @RpcConsumer
    private DoctorReportFieldCustomizesReadService doctorReportFieldCustomizesReadService;
    @RpcConsumer
    private DoctorDailyReportV2Service doctorDailyReportV2Service;

    private Map<String, Method> methodMap;

    @PostConstruct
    public void init() {
        methodMap = Maps.newHashMap();
        map(DoctorReportReserve.class.getSimpleName(), DoctorReportReserve.class.getMethods(), methodMap);
        map(DoctorReportSow.class.getSimpleName(), DoctorReportSow.class.getMethods(), methodMap);
        map(DoctorReportMating.class.getSimpleName(),DoctorReportMating.class.getMethods(), methodMap);
        map(DoctorReportDeliver.class.getSimpleName(),DoctorReportDeliver.class.getMethods(), methodMap);
        map(DoctorReportNursery.class.getSimpleName(),DoctorReportNursery.class.getMethods(), methodMap);
        map(DoctorReportFatten.class.getSimpleName(),DoctorReportFatten.class.getMethods(), methodMap);
        map(DoctorReportBoar.class.getSimpleName(),DoctorReportBoar.class.getMethods(), methodMap);
        map(DoctorReportMaterial.class.getSimpleName(),DoctorReportMaterial.class.getMethods(), methodMap);
        map(DoctorReportEfficiency.class.getSimpleName(),DoctorReportEfficiency.class.getMethods(), methodMap);
    }

    @ApiOperation("看板日报表")
    @RequestMapping(method = RequestMethod.GET, value = "daily")
    public List<DoctorReportFieldTypeDto> dailyBoard(@ApiParam("猪场名称")
                                                     @PathVariable Long farmId,
                                                     @ApiParam("查询日期")
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, OrzDimension.FARM.getValue(),
                date, DateDimension.DAY.getValue());
        return fieldWithHidden(dimensionCriteria);
    }


    @ApiOperation("看板周报表")
    @RequestMapping(method = RequestMethod.GET, value = "weekly")
    public List<DoctorReportFieldTypeDto> weeklyBoard(@ApiParam("猪场名称")
                                                      @PathVariable Long farmId,
                                                      @ApiParam("查询日期， 本周第一天")
                                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, OrzDimension.FARM.getValue(),
                date, DateDimension.WEEK.getValue());
        return fieldWithHidden(dimensionCriteria);
    }

    @ApiOperation("看板月报表")
    @RequestMapping(method = RequestMethod.GET, value = "monthly")
    public List<DoctorReportFieldTypeDto> monthlyBoard(@ApiParam("猪场名称")
                                                       @PathVariable Long farmId,
                                                       @ApiParam("查询日期， 本月第一天")
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, OrzDimension.FARM.getValue(),
                date, DateDimension.MONTH.getValue());
        return fieldWithHidden(dimensionCriteria);
    }

    @ApiOperation("看板季报表")
    @RequestMapping(method = RequestMethod.GET, value = "quarterly")
    public List<DoctorReportFieldTypeDto> quarterlyBoard(@ApiParam("猪场名称")
                                                         @PathVariable Long farmId,
                                                         @ApiParam("查询日期， 本季第一天")
                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, OrzDimension.FARM.getValue(),
                date, DateDimension.QUARTER.getValue());
        return fieldWithHidden(dimensionCriteria);
    }

    @ApiOperation("看板年报表")
    @RequestMapping(method = RequestMethod.GET, value = "yearly")
    public List<DoctorReportFieldTypeDto> yearlyBoard(@ApiParam("猪场名称")
                                                      @PathVariable Long farmId,
                                                      @ApiParam("查询日期， 本年第一天")
                                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, OrzDimension.FARM.getValue(),
                date, DateDimension.YEAR.getValue());
        return fieldWithHidden(dimensionCriteria);
    }

    @RequestMapping(value = "/live/stock")
    public DoctorFarmLiveStockDto realTimeLiveStock(@PathVariable Long farmId){
        return RespHelper.or500(doctorDailyReportV2Service.findFarmsLiveStock(Lists.newArrayList(farmId))).get(0);
    }

    private List<DoctorReportFieldTypeDto> fieldWithHidden(DoctorDimensionCriteria dimensionCriteria) {
        List<DoctorReportFieldTypeDto> reportFieldTypeDtoList = RespHelper.or500(doctorReportFieldCustomizesReadService.getAllWithSelected(dimensionCriteria.getOrzId()));
        DoctorDimensionReport report = RespHelper.or500(doctorDailyReportV2Service.dimensionReport(dimensionCriteria));
        reportFieldTypeDtoList.forEach(doctorReportFieldTypeDto -> {
            DoctorReportRegion region = DoctorReportRegion.from(doctorReportFieldTypeDto.getName());
            switch (region) {
                case RESERVE: subField(doctorReportFieldTypeDto, report.getReportReserve()); break;
                case SOW: subField(doctorReportFieldTypeDto, report.getReportSow()); break;
                case MATING: subField(doctorReportFieldTypeDto, report.getReportMating()); break;
                case DELIVER: subField(doctorReportFieldTypeDto, report.getReportDeliver()); break;
                case NURSERY: subField(doctorReportFieldTypeDto, report.getReportNursery()); break;
                case FATTEN: subField(doctorReportFieldTypeDto, report.getReportFatten()); break;
                case BOAR: subField(doctorReportFieldTypeDto, report.getReportBoar()); break;
                case MATERIAL:
                    // TODO: 18/1/18 物料数据错误太多
                    subField(doctorReportFieldTypeDto, report.getReportMaterial());
                    break;
                case EFFICIENCY:
                    // TODO: 18/1/18 效率指标数据错误太多
                    if (DateDimension.YEARLY.contains(dimensionCriteria.getDateType())) {
                        subField(doctorReportFieldTypeDto, report.getReportEfficiency());
                    } else {
                        subFieldDefault(doctorReportFieldTypeDto);
                    }
                    break;
                default:
                    throw new JsonResponseException("region.is.illegal");
            }
            });
        return reportFieldTypeDtoList;
    }

    private void subFieldDefault(DoctorReportFieldTypeDto doctorReportFieldTypeDto) {
        doctorReportFieldTypeDto.getFields().forEach(subField -> subField.setValue("-"));
    }

    private void subField(DoctorReportFieldTypeDto doctorReportFieldTypeDto, Object obj) {

        doctorReportFieldTypeDto.getFields().forEach(subField -> {
            String methodName = getMethodName(doctorReportFieldTypeDto.getReportField(), subField.getReportField());
            Method method = methodMap.get(methodName);
            try {
                Object value = method.invoke(obj);
                subField.setValue(getValue(value));
            } catch (Exception e) {
                log.error("method invoke error, methodName:{}, cause:{}", methodName, Throwables.getStackTraceAsString(e));
                throw new JsonResponseException("method.invoke.error");
            }
        });
    }

    private static String getMethodName(String regionName, String reportFiled) {
        char[] chars = reportFiled.toCharArray();
        chars[0] -= 32;
        return regionName + ".get" + new String(chars);
    }

    private void map(String prefix, Method[] methods, Map<String, Method> methodMap) {
        for (Method method: methods) {
            methodMap.put(prefix + "." + method.getName(), method);
        }
    }

    private String getValue(Object value) {

        if (isNull(value)) {
            return "-";
        }
        String val = value.toString();
        if (!val.startsWith("{")) {
            return val;
        }
        Map map = JsonMapperUtil.nonEmptyMapper().fromJson(val, Map.class);
        return map.get("value").toString();
    }
}
