package io.terminus.doctor.web.admin.job;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorProfitExportDto;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorProfitMaterOrPigWriteServer;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.zookeeper.leader.HostLeader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by terminus on 2017/4/11.
 */

@RestController
@RequestMapping("/api/profit")
@Slf4j
public class DoctorProfitJobs {


    private final HostLeader hostLeader;
    @RpcConsumer
    private DoctorPigEventReadService doctorPigEventReadService;
    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;
    @RpcConsumer
    private DoctorProfitMaterOrPigWriteServer doctorProfitMaterOrPigWriteServer;

    public static final List<Long> materialType = Lists.newArrayList(1L, 2L, 3L, 4L, 5L);
    public static final List<String> pigType = Lists.newArrayList("3", "4", "5", "7_2");

    @Autowired
    public DoctorProfitJobs(HostLeader hostLeader) {
        this.hostLeader = hostLeader;
    }

    /**
     * 猪场利润的计算
     * 每天凌晨1点统计昨天的数据
     */
//    @Scheduled(cron = "0 0 1 * * ?")

    @Scheduled(cron = "0 */1 * * * ?")
    @RequestMapping(value = "/profit", method = RequestMethod.GET)
    public void profitReport() {
        try {
            if(!hostLeader.isLeader()) {
                log.info("current leader is:{}, skip", hostLeader.currentLeaderId());
                return;
            }
            log.info("daily profit job start, now is:{}", DateUtil.toDateTimeString(new Date()));

            Date startDate = DateUtil.monthStart(new Date());
            startDate = DateUtils.addMonths(startDate, -3);
            Date endDate = DateUtil.getMonthEnd(new DateTime(startDate)).toDate();

            doctorProfitMaterOrPigWriteServer.deleteDoctorProfitMaterialOrPig(startDate);

            Map<String, Object> map = Maps.newHashMap();

            List<DoctorProfitMaterialOrPig> doctorProfitMaterialOrPigList = Lists.newArrayList();

            map.put("startDate", startDate);
            map.put("endDate", endDate);

            List<Long> farmIds = getAllFarmIds();
            for (Long farmId : farmIds) {
                map.put("farmId", farmId);
                for (String pigs : pigType) {
                    map.put("pigTypeId", pigs);
                    List<DoctorProfitExportDto> profitExportDto = RespHelper.or500(doctorPigEventReadService.sumProfitAmount(map));
                    DoctorProfitMaterialOrPig doctorProfitMaterialOrPig = new DoctorProfitMaterialOrPig();
                    Double amountPig = 0.0;
                    for (DoctorProfitExportDto doctorProfitExportDto : profitExportDto) {
                        doctorProfitMaterialOrPig = sumMaterialAmount(startDate, endDate, farmId, doctorProfitExportDto.getBarnId(), doctorProfitMaterialOrPig);
                        amountPig += doctorProfitExportDto.getAmount();
                    }
                    doctorProfitMaterialOrPig.setFarmId(farmId);
                    doctorProfitMaterialOrPig.setPigTypeNameId(pigs);
                    if (!profitExportDto.isEmpty()) {
                        doctorProfitMaterialOrPig.setPigTypeName(profitExportDto.get(0).getPigTypeName());
                    }
                    doctorProfitMaterialOrPig.setAmountPig(amountPig);
                    doctorProfitMaterialOrPig.setSumTime(startDate);
                    doctorProfitMaterialOrPig.setRefreshTime(DateUtil.toDateTimeString(new Date()));
                    doctorProfitMaterialOrPigList.add(doctorProfitMaterialOrPig);
                }
            }
            doctorProfitMaterOrPigWriteServer.insterDoctorProfitMaterialOrPig(doctorProfitMaterialOrPigList);
            log.info("daily profit job end, now is:{}", DateUtil.toDateTimeString(new Date()));
        } catch (Exception e) {
            log.error("daily profit job failed, cause:{}", Throwables.getStackTraceAsString(e));
        }
    }
    public DoctorProfitMaterialOrPig sumMaterialAmount(Date startDate, Date endDate, Long farmId, Long barnId, DoctorProfitMaterialOrPig doctorProfitMaterialOrPig) {

//        DateTime date = new DateTime(startDate);
//        Date startDates = DateUtils.addDays(startDate,1-date.dayOfYear().get());
//        Date endDates = DateUtils.addSeconds(DateUtils.addYears(startDate,1),-1);
        for (Long type : materialType) {

            List<DoctorMaterialConsumeProvider> doctorMaterialConsumeProviders = RespHelper.or500(doctorMaterialConsumeProviderReadService.findMaterialProfit(farmId,
                        type,
                        barnId,
                        startDate,
                        endDate));
            if (type == 1L) {
                doctorProfitMaterialOrPig.setFeedTypeName("饲料");
                doctorProfitMaterialOrPig.setFeedTypeId(type);
                doctorProfitMaterialOrPig.setFeedAmount(builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders) + doctorProfitMaterialOrPig.getFeedAmount());

            } else if (type == 2L) {
                doctorProfitMaterialOrPig.setMaterialTypeName("原料");
                doctorProfitMaterialOrPig.setMaterialTypeId(type);
                doctorProfitMaterialOrPig.setMaterialAmount(builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders) + doctorProfitMaterialOrPig.getMaterialAmount());

            } else if (type == 3L) {
                doctorProfitMaterialOrPig.setVaccineTypeName("疫苗");
                doctorProfitMaterialOrPig.setVaccineTypeId(type);
                doctorProfitMaterialOrPig.setVaccineAmount(builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders) + doctorProfitMaterialOrPig.getVaccineAmount());

            } else if (type == 4L) {
                doctorProfitMaterialOrPig.setMedicineTypeName("药品");
                doctorProfitMaterialOrPig.setMedicineTypeId(type);
                doctorProfitMaterialOrPig.setMedicineAmount(builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders) + doctorProfitMaterialOrPig.getMedicineAmount());

            } else if (type == 5L) {
                doctorProfitMaterialOrPig.setConsumablesTypeName("消耗品");
                doctorProfitMaterialOrPig.setConsumablesTypeId(type);
                doctorProfitMaterialOrPig.setConsumablesAmount(builderDoctorMaterialConumeProvider(doctorMaterialConsumeProviders)+doctorProfitMaterialOrPig.getConsumablesAmount());

            }

        }
        return doctorProfitMaterialOrPig;
    }

    private Double builderDoctorMaterialConumeProvider(List<DoctorMaterialConsumeProvider> doctorMaterialConsumeProviders) {

        Double acmunt = 0.0;

        for (int i = 0; i < doctorMaterialConsumeProviders.size(); i++) {

            if (doctorMaterialConsumeProviders.get(i).getExtra() != null && doctorMaterialConsumeProviders.get(i).getExtraMap().containsKey("consumePrice")) {
                List<Map<String, Object>> priceCompose = (ArrayList) doctorMaterialConsumeProviders.get(i).getExtraMap().get("consumePrice");
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

    private List<Long> getAllFarmIds() {
        return RespHelper.orServEx(doctorFarmReadService.findAllFarms()).stream().map(DoctorFarm::getId).collect(Collectors.toList());
    }
}
