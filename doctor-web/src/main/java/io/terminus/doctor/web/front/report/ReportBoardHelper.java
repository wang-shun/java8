package io.terminus.doctor.web.front.report;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesReadService;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.reportBi.DoctorDimensionReport;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.DoctorReportRegion;
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
import io.terminus.doctor.web.front.report.DataFormatter.DataFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Created by xjn on 18/1/21.
 * email:xiaojiannan@terminus.io
 */
@Slf4j
@Component
public class ReportBoardHelper {

    @RpcConsumer
    private DoctorReportFieldCustomizesReadService doctorReportFieldCustomizesReadService;
    @RpcConsumer
    private DoctorDailyReportV2Service doctorDailyReportV2Service;
    @Autowired
    private ApplicationContext applicationContext;
    private Map<String, Method> methodMap;
    private Map<String, DataFormatter> dataFormatterMap;


    @PostConstruct
    public void init() {
        dataFormatterMap = applicationContext.getBeansOfType(DataFormatter.class);
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

    List<DoctorReportFieldTypeDto> fieldWithHidden(DoctorDimensionCriteria dimensionCriteria) {
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
                    subField(doctorReportFieldTypeDto, report.getReportMaterial());
                    break;
                case EFFICIENCY:
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
                subField.setValue(getValue(value, subField.getDataFormatter()));
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

    private String getValue(Object value, String dataFormatter) {

        if (isNull(value)) {
            return "-";
        }
        String val = value.toString();
        if (!val.startsWith("{")) {
            DataFormatter formatter = dataFormatterMap.get(dataFormatter);
            if (isNull(formatter)) {
                return val;
            }
            return formatter.format(val);
        }
        Map map = JsonMapperUtil.nonEmptyMapper().fromJson(val, Map.class);
        return map.get("value").toString();
    }
}
