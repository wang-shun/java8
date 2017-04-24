package io.terminus.doctor.web.admin.job.material;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorProfitExportDto;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorProfitMaterOrPigWriteServer;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by terminus on 2017/4/20.
 */
@Component
public class DoctorGroupProfitManage {

    @RpcConsumer
    private DoctorProfitMaterOrPigWriteServer doctorProfitMaterOrPigWriteServer;
    @RpcConsumer
    private DoctorPigEventReadService doctorPigEventReadService;
    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;


    public static final List<Long> materialType = Lists.newArrayList(1L, 2L, 3L, 4L, 5L);
    public static final List<String> pigType = Lists.newArrayList("3", "4", "5", "7_2");

    public Double feedAmount = 0.0;
    public Double materialAmount = 0.0;
    public Double vaccineAmount = 0.0;
    public Double medicineAmount = 0.0;
    public Double consumablesAmount = 0.0;
    public Double amount = 0.0;

    public void sumDoctorProfitMaterialOrPig(List<Long> farmIds, Date dates){

        Date startDate = DateUtil.monthStart(dates);
        Date endDate = DateUtil.getMonthEnd(new DateTime(startDate)).toDate();
        DateTime date;
        Date startDates;
        Date endDates;
        doctorProfitMaterOrPigWriteServer.deleteDoctorProfitMaterialOrPig(startDate);
        Map<String, Object> map = Maps.newHashMap();
        List<DoctorProfitMaterialOrPig> doctorProfitMaterialOrPigList = Lists.newArrayList();
        List<DoctorProfitExportDto> profitExportDto;
        DoctorProfitMaterialOrPig doctorProfitMaterialOrPig;
        List<DoctorProfitExportDto> profitYearExportDto;
        map.put("startDate", startDate);
        map.put("endDate", endDate);
        for (Long farmId : farmIds) {
            map.put("farmId", farmId);
            for (String pigs : pigType) {
                map.put("pigTypeId", pigs);
                profitExportDto = RespHelper.or500(doctorPigEventReadService.sumProfitAmount(map));
                doctorProfitMaterialOrPig = new DoctorProfitMaterialOrPig();
                Double amountPig = 0.0;
                feedAmount = 0.0;
                materialAmount = 0.0;
                vaccineAmount = 0.0;
                medicineAmount = 0.0;
                consumablesAmount = 0.0;
                amount = 0.0;
                for (DoctorProfitExportDto doctorProfitExportDto : profitExportDto) {
                    doctorProfitMaterialOrPig = sumMaterialAmount(startDate, endDate, farmId, doctorProfitExportDto.getBarnId(), doctorProfitMaterialOrPig, true);
                    amountPig += doctorProfitExportDto.getAmount();
                }
                date = new DateTime(startDate);
                startDates = DateUtils.addDays(startDate,1-date.dayOfYear().get());
                endDates = DateUtils.addSeconds(DateUtils.addYears(startDates,1),-1);
                map.put("startDate", startDates);
                map.put("endDate", endDates);
                profitYearExportDto = RespHelper.or500(doctorPigEventReadService.sumProfitAmount(map));
                Double amountPigYear = 0.0;
                amount = 0.0;
                for (DoctorProfitExportDto doctorProfitExportDto : profitYearExportDto) {
                    doctorProfitMaterialOrPig = sumMaterialAmount(startDates, endDates, farmId, doctorProfitExportDto.getBarnId(), doctorProfitMaterialOrPig, false);
                    amountPigYear += doctorProfitExportDto.getAmount();
                }
                if (!profitExportDto.isEmpty()) {
                    doctorProfitMaterialOrPig.setFarmId(farmId);
                    doctorProfitMaterialOrPig.setPigTypeNameId(pigs);
                    doctorProfitMaterialOrPig.setPigTypeName(profitExportDto.get(0).getPigTypeName());
                    doctorProfitMaterialOrPig.setAmountPig(amountPig);
                    doctorProfitMaterialOrPig.setSumTime(startDate);
                    doctorProfitMaterialOrPig.setRefreshTime(DateUtil.toDateTimeString(new Date()));
                    doctorProfitMaterialOrPig.setAmountYearPig(amountPigYear);
                    doctorProfitMaterialOrPig.setFeedAmount(feedAmount);
                    doctorProfitMaterialOrPig.setMaterialAmount(materialAmount);
                    doctorProfitMaterialOrPig.setMedicineAmount(medicineAmount);
                    doctorProfitMaterialOrPig.setConsumablesAmount(consumablesAmount);
                    doctorProfitMaterialOrPig.setAmountYearMaterial(amount);
                    doctorProfitMaterialOrPigList.add(doctorProfitMaterialOrPig);
                }
            }
        }
        doctorProfitMaterOrPigWriteServer.insterDoctorProfitMaterialOrPig(doctorProfitMaterialOrPigList);
    }

    private final DoctorProfitMaterialOrPig sumMaterialAmount(Date startDate, Date endDate, Long farmId, Long barnId, DoctorProfitMaterialOrPig doctorProfitMaterialOrPig, Boolean tag) {

        List<DoctorMaterialConsumeProvider> doctorMaterialConsumeProviders = Lists.newArrayList();
        if (tag) {
            for (Long type : materialType) {

                doctorMaterialConsumeProviders = RespHelper.or500(doctorMaterialConsumeProviderReadService.findMaterialProfit(farmId,
                        type,
                        barnId,
                        startDate,
                        endDate));
                if (type == 1L) {
                    doctorProfitMaterialOrPig.setFeedTypeName("饲料");
                    doctorProfitMaterialOrPig.setFeedTypeId(type);
                    feedAmount += builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders);
                    doctorProfitMaterialOrPig.setFeedAmount(feedAmount);

                } else if (type == 2L) {
                    doctorProfitMaterialOrPig.setMaterialTypeName("原料");
                    doctorProfitMaterialOrPig.setMaterialTypeId(type);
                    materialAmount += builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders);

                } else if (type == 3L) {
                    doctorProfitMaterialOrPig.setVaccineTypeName("疫苗");
                    doctorProfitMaterialOrPig.setVaccineTypeId(type);
                    vaccineAmount += builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders);

                } else if (type == 4L) {
                    doctorProfitMaterialOrPig.setMedicineTypeName("药品");
                    doctorProfitMaterialOrPig.setMedicineTypeId(type);
                    medicineAmount += builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders);

                } else if (type == 5L) {
                    doctorProfitMaterialOrPig.setConsumablesTypeName("消耗品");
                    doctorProfitMaterialOrPig.setConsumablesTypeId(type);
                    consumablesAmount += builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders);

                }
            }
        } else {
            doctorMaterialConsumeProviders = RespHelper.or500(doctorMaterialConsumeProviderReadService.findMaterialProfit(farmId,
                    null,
                    barnId,
                    startDate,
                    endDate));
            amount += builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders);
        }
        return doctorProfitMaterialOrPig;
    }

    private final Double builderDoctorMaterialConumeProvider(List<DoctorMaterialConsumeProvider> doctorMaterialConsumeProviders) {

        Double acmunt = 0.0;
        List<Map<String, Object>> priceCompose;
        for (int i = 0, length = doctorMaterialConsumeProviders.size(); i < length; i++) {

            if (doctorMaterialConsumeProviders.get(i).getExtra() != null && doctorMaterialConsumeProviders.get(i).getExtraMap().containsKey("consumePrice")) {
                priceCompose = (ArrayList) doctorMaterialConsumeProviders.get(i).getExtraMap().get("consumePrice");
                for (Map<String, Object> eachPrice : priceCompose) {

                    Long unitPrice = Long.valueOf(eachPrice.get("unitPrice").toString());
                    Double count = Double.valueOf(eachPrice.get("count").toString());
                    acmunt += unitPrice * count;
                }
            } else {
                Long unitPrice = doctorMaterialConsumeProviders.get(i).getUnitPrice();
                Double count = doctorMaterialConsumeProviders.get(i).getEventCount();
                acmunt += unitPrice * count;
            }

        }
        return acmunt;
    }
}
