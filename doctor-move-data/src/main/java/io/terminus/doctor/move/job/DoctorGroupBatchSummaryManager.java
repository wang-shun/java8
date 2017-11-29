package io.terminus.doctor.move.job;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseMaterialApplyReadService;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryReadService;
import io.terminus.doctor.event.service.DoctorGroupBatchSummaryWriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xjn on 17/6/5.
 */
@Slf4j
@Component
public class DoctorGroupBatchSummaryManager {

    @Autowired
    private DoctorGroupDao doctorGroupDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;
    @Autowired
    private DoctorGroupBatchSummaryWriteService doctorGroupBatchSummaryWriteService;
    @Autowired
    private DoctorGroupBatchSummaryReadService doctorGroupBatchSummaryReadService;

    @Autowired
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    public void createAllGroupSummary() {
        log.info("create all group summary starting");
        List<DoctorGroup> groups = doctorGroupDao.listAll();
        groups.forEach(this::createGroupSummary);
        log.info("create all group summary end");
    }

    public void createGroupSummary(DoctorGroup group) {
        try {
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
            Double frcFeed = RespHelper.or(doctorMaterialConsumeProviderReadService.sumConsumeFeed(null, null, null, null, null, group.getId(), null, null), 0D);
            Response<DoctorGroupBatchSummary> result = doctorGroupBatchSummaryReadService.getGroupBatchSummary(group, groupTrack, frcFeed);
            if (result.isSuccess() && result.getResult() != null) {
                DoctorGroupBatchSummary s = result.getResult();
//                List<DoctorMaterialConsumeProvider> consumeProviders = Lists.newArrayList();
//                consumeProviders = RespHelper.or500(doctorMaterialConsumeProviderReadService.findMaterialByGroupId(s.getFarmId(),s.getGroupId(),null,null,null,null,1L,null,null));
//                s.setFeedAmount(getMaterialAmount(consumeProviders));
//                s.setFendNumber(getMaterialNumber(consumeProviders));
//                consumeProviders = RespHelper.or500(doctorMaterialConsumeProviderReadService.findMaterialByGroupId(s.getFarmId(),s.getGroupId(),null,null,null,null,2L,null,null));
//                s.setMedicineAmount(getMaterialAmount(consumeProviders));
//                consumeProviders = RespHelper.or500(doctorMaterialConsumeProviderReadService.findMaterialByGroupId(s.getFarmId(),s.getGroupId(),null,null,null,null,3L,null,null));
//                s.setVaccineAmount(getMaterialAmount(consumeProviders));
//                consumeProviders = RespHelper.or500(doctorMaterialConsumeProviderReadService.findMaterialByGroupId(s.getFarmId(),s.getGroupId(),null,null,null,null,4L,null,null));
//                s.setMedicineAmount(getMaterialAmount(consumeProviders));
//                consumeProviders = RespHelper.or500(doctorMaterialConsumeProviderReadService.findMaterialByGroupId(s.getFarmId(),s.getGroupId(),null,null,null,null,5L,null,null));
//                s.setConsumablesAmount(getMaterialAmount(consumeProviders));

                List<DoctorWarehouseMaterialApply> applies = RespHelper.or500(doctorWarehouseMaterialApplyReadService.findByFarmAndPigGroup(s.getFarmId(), s.getGroupId()));
                double feedAmount = 0, medicineAmount = 0, vaccineAmount = 0, consumeAmount = 0;
                BigDecimal feedQuantity = new BigDecimal(0);
                for (DoctorWarehouseMaterialApply apply : applies) {
                    if (WareHouseType.FEED.getKey() == apply.getType()) {
                        feedAmount += apply.getQuantity().multiply(new BigDecimal(apply.getUnitPrice())).doubleValue();
                        feedQuantity = feedQuantity.add(apply.getQuantity());
                    } else if (WareHouseType.VACCINATION.getKey() == apply.getType())
                        vaccineAmount += apply.getQuantity().multiply(new BigDecimal(apply.getUnitPrice())).doubleValue();
                    else if (WareHouseType.MEDICINE.getKey() == apply.getType())
                        medicineAmount += apply.getQuantity().multiply(new BigDecimal(apply.getUnitPrice())).doubleValue();
                    else if (WareHouseType.CONSUME.getKey() == apply.getType())
                        consumeAmount += apply.getQuantity().multiply(new BigDecimal(apply.getUnitPrice())).doubleValue();
                }
                s.setFeedAmount(feedAmount);
                s.setFendNumber(feedQuantity.doubleValue());
                s.setMedicineAmount(medicineAmount);
                s.setVaccineAmount(vaccineAmount);
                s.setConsumablesAmount(consumeAmount);

                doctorGroupBatchSummaryWriteService.createGroupBatchSummary(s);
            }
        } catch (Exception e) {
            log.error("create.group.summary.failed, groupId:{}, cause:{}", group.getId(), Throwables.getStackTraceAsString(e));
        }
    }

    private static Double getMaterialNumber(List<DoctorMaterialConsumeProvider> doctorMaterialConsumeProviders) {
        Double number = 0.0;
        for (int i = 0; i < doctorMaterialConsumeProviders.size(); i++) {
            if (doctorMaterialConsumeProviders.get(i).getExtra() != null && doctorMaterialConsumeProviders.get(i).getExtraMap().containsKey("consumePrice")) {
                List<Map<String, Object>> priceCompose = (ArrayList) doctorMaterialConsumeProviders.get(i).getExtraMap().get("consumePrice");
                for (Map<String, Object> eachPrice : priceCompose) {
                    Double count = Double.valueOf(eachPrice.get("count").toString());
                    number += count;
                }
            } else {
                Double count = doctorMaterialConsumeProviders.get(i).getEventCount();
                number += count;
            }
        }
        return number;
    }

    private static Double getMaterialAmount(List<DoctorMaterialConsumeProvider> doctorMaterialConsumeProviders) {
        Double amount = 0.0;
        for (int i = 0; i < doctorMaterialConsumeProviders.size(); i++) {
            if (doctorMaterialConsumeProviders.get(i).getExtra() != null && doctorMaterialConsumeProviders.get(i).getExtraMap().containsKey("consumePrice")) {
                List<Map<String, Object>> priceCompose = (ArrayList) doctorMaterialConsumeProviders.get(i).getExtraMap().get("consumePrice");
                for (Map<String, Object> eachPrice : priceCompose) {
                    Long unitPrice = Long.valueOf(eachPrice.get("unitPrice").toString());
                    Double count = Double.valueOf(eachPrice.get("count").toString());
                    amount += unitPrice * count;
                }
            } else {
                Long unitPrice = doctorMaterialConsumeProviders.get(i).getUnitPrice();
                Double count = doctorMaterialConsumeProviders.get(i).getEventCount();
                amount += unitPrice * count;
            }
        }
        return amount;
    }

}
