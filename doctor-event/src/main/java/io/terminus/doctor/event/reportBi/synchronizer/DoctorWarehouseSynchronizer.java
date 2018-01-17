package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorWarehouseReportDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportMaterialDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorReportMaterial;
import io.terminus.doctor.event.reportBi.helper.DateHelper;
import io.terminus.doctor.event.service.DoctorPigReportReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/1/15.
 */
@Component
public class DoctorWarehouseSynchronizer {

    @Autowired
    private DoctorReportMaterialDao doctorReportMaterialDao;
    @Autowired
    private DoctorWarehouseReportDao doctorWarehouseReportDao;
    @RpcConsumer
    private DoctorOrgReadService doctorOrgReadService;
    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @RpcConsumer
    private DoctorPigReportReadService doctorPigReportReadService;

    public void sync(Date date) {

        //日
        List<Long> farmIds = doctorWarehouseReportDao.findApplyFarm(date, date);
        Set<Long> orgIds = new HashSet<>();
        for (Long f : farmIds) {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));
            if (null != farm) {
                orgIds.add(farm.getOrgId());
            }

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), date, date);

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(date);
            material.setSumAtName(DateHelper.dateCN(date, DateDimension.DAY));
            material.setDateType(DateDimension.DAY.getValue());
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType(OrzDimension.FARM.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }
        orgIds.forEach(o ->

        {
            DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(o));
            List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(o));

            Map<String, Object> result = doctorWarehouseReportDao.count(farms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), date, date);

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(date);
            material.setSumAtName(DateHelper.dateCN(date, DateDimension.DAY));
            material.setDateType(DateDimension.DAY.getValue());
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType(OrzDimension.ORG.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        });
        //周
        DoctorPigReportReadService.DateDuration dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.WEEK);
        farmIds = doctorWarehouseReportDao.findApplyFarm(dateDuration.getStart(), dateDuration.getEnd());
        orgIds = new HashSet<>();
        for (Long f : farmIds) {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));
            if (null != farm) {
                orgIds.add(farm.getOrgId());
            }

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), dateDuration.getStart(), dateDuration.getEnd());

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(dateDuration.getStart());
            material.setSumAtName(DateHelper.dateCN(dateDuration.getStart(), DateDimension.WEEK));
            material.setDateType(DateDimension.WEEK.getValue());
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType(OrzDimension.FARM.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }
        for (Long o : orgIds) {
            DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(o));
            List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(o));

            Map<String, Object> result = doctorWarehouseReportDao.count(farms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), dateDuration.getStart(), dateDuration.getEnd());

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(dateDuration.getStart());
            material.setSumAtName(DateHelper.dateCN(dateDuration.getStart(), DateDimension.WEEK));
            material.setDateType(DateDimension.WEEK.getValue());
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType(OrzDimension.ORG.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }
        //月
        dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.MONTH);
        farmIds = doctorWarehouseReportDao.findApplyFarm(dateDuration.getStart(), dateDuration.getEnd());
        orgIds = new HashSet<>();
        for (Long f : farmIds) {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));
            if (null != farm) {
                orgIds.add(farm.getOrgId());
            }

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), dateDuration.getStart(), dateDuration.getEnd());

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(dateDuration.getStart());
            material.setSumAtName(DateHelper.dateCN(dateDuration.getStart(), DateDimension.MONTH));
            material.setDateType(DateDimension.MONTH.getValue());
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType(OrzDimension.FARM.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }
        for (Long o : orgIds) {
            DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(o));
            List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(o));

            Map<String, Object> result = doctorWarehouseReportDao.count(farms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), dateDuration.getStart(), dateDuration.getEnd());

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(dateDuration.getStart());
            material.setSumAtName(DateHelper.dateCN(dateDuration.getStart(), DateDimension.MONTH));
            material.setDateType(DateDimension.MONTH.getValue());
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType(OrzDimension.ORG.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }
        //季
        dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.SEASON);
        farmIds = doctorWarehouseReportDao.findApplyFarm(dateDuration.getStart(), dateDuration.getEnd());
        orgIds = new HashSet<>();
        for (Long f : farmIds) {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));
            if (null != farm) {
                orgIds.add(farm.getOrgId());
            }

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), dateDuration.getStart(), dateDuration.getEnd());

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(dateDuration.getStart());
            material.setSumAtName(DateHelper.dateCN(dateDuration.getStart(), DateDimension.QUARTER));
            material.setDateType(DateDimension.QUARTER.getValue());
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType(OrzDimension.FARM.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }
        for (Long o : orgIds) {
            DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(o));
            List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(o));

            Map<String, Object> result = doctorWarehouseReportDao.count(farms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), dateDuration.getStart(), dateDuration.getEnd());

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(dateDuration.getStart());
            material.setSumAtName(DateHelper.dateCN(dateDuration.getStart(), DateDimension.QUARTER));
            material.setDateType(DateDimension.QUARTER.getValue());
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType(OrzDimension.ORG.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }
        //年
        dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.YEAR);
        farmIds = doctorWarehouseReportDao.findApplyFarm(dateDuration.getStart(), dateDuration.getEnd());
        orgIds = new HashSet<>();
        for (Long f : farmIds) {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));
            if (null != farm) {
                orgIds.add(farm.getOrgId());
            }

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), dateDuration.getStart(), dateDuration.getEnd());

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(date);
            material.setSumAtName(DateHelper.dateCN(date, DateDimension.YEAR));
            material.setDateType(DateDimension.YEAR.getValue());
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType(OrzDimension.FARM.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }
        for (Long o : orgIds) {
            DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(o));
            List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(o));

            Map<String, Object> result = doctorWarehouseReportDao.count(farms.stream().map(DoctorFarm::getId).collect(Collectors.toList()), dateDuration.getStart(), dateDuration.getEnd());

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(dateDuration.getStart());
            material.setSumAtName(DateHelper.dateCN(dateDuration.getStart(), DateDimension.YEAR));
            material.setDateType(DateDimension.YEAR.getValue());
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType(OrzDimension.ORG.getValue());

            fill(material, result);

            doctorReportMaterialDao.create(material);
        }

    }


    public void sync(DoctorDimensionCriteria dimensionCriteria) {
        List<Long> farmIds = new ArrayList<>();
        if (dimensionCriteria.getOrzType().equals(OrzDimension.FARM.getValue())) {
            farmIds.add(dimensionCriteria.getOrzId());
        } else {
            farmIds.addAll(RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(dimensionCriteria.getOrzId()))
                    .stream().map(DoctorFarm::getId)
                    .collect(Collectors.toList()));
        }


        

    }


    private void fill(DoctorReportMaterial material, Map<String, Object> result) {
        material.setHoubeiFeedQuantity(((BigDecimal) (result.get("houbeiFeedCount"))).intValue());
        material.setHoubeiFeedAmount(((BigDecimal) result.get("houbeiFeedAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setHoubeiMaterialAmount(((BigDecimal) result.get("houbeiMaterialAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setHoubeiMaterialQuantity(((BigDecimal) result.get("houbeiMaterialCount")).intValue());
        material.setHoubeiConsumeAmount(((BigDecimal) result.get("houbeiConsumerAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setHoubeiMedicineAmount(((BigDecimal) result.get("houbeiDrugAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setHoubeiVaccinationAmount(((BigDecimal) result.get("houbeiVaccineAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));

        material.setPeihuaiFeedQuantity(((BigDecimal) result.get("peiHuaiFeedCount")).intValue());
        material.setPeihuaiFeedAmount(((BigDecimal) result.get("peiHuaiFeedAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setPeihuaiMaterialAmount(((BigDecimal) result.get("peiHuaiMaterialAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setPeihuaiMaterialQuantity(((BigDecimal) result.get("peiHuaiMaterialCount")).intValue());
        material.setPeihuaiConsumeAmount(((BigDecimal) result.get("peiHuaiConsumerAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setPeihuaiMedicineAmount(((BigDecimal) result.get("peiHuaiDrugAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setPeihuaiVaccinationAmount(((BigDecimal) result.get("peiHuaiVaccineAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));

        material.setSowFeedQuantity(((BigDecimal) result.get("farrowSowFeedCount")).intValue());
        material.setSowFeedAmount(((BigDecimal) result.get("farrowSowFeedAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setSowMaterialAmount(((BigDecimal) result.get("farrowSowMaterialAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setSowMaterialQuantity(((BigDecimal) result.get("farrowSowMaterialCount")).intValue());
        material.setSowConsumeAmount(((BigDecimal) result.get("farrowSowConsumerAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setSowMedicineAmount(((BigDecimal) result.get("farrowSowDrugAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setSowVaccinationAmount(((BigDecimal) result.get("farrowSowVaccineAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));

        material.setPigletFeedQuantity(((BigDecimal) result.get("farrowFeedCount")).intValue());
        material.setPigletFeedAmount(((BigDecimal) result.get("farrowFeedAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setPigletMaterialAmount(((BigDecimal) result.get("farrowMaterialAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setPigletMaterialQuantity(((BigDecimal) result.get("farrowMaterialCount")).intValue());
        material.setPigletConsumeAmount(((BigDecimal) result.get("farrowConsumerAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setPigletMedicineAmount(((BigDecimal) result.get("farrowDrugAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setPigletVaccinationAmount(((BigDecimal) result.get("farrowVaccineAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));

        material.setBaoyuFeedQuantity(((BigDecimal) result.get("nurseryFeedCount")).intValue());
        material.setBaoyuFeedAmount(((BigDecimal) result.get("nurseryFeedAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setBaoyuMaterialAmount(((BigDecimal) result.get("nurseryMaterialAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setBaoyuMaterialQuantity(((BigDecimal) result.get("nurseryMaterialCount")).intValue());
        material.setBaoyuConsumeAmount(((BigDecimal) result.get("nurseryConsumerAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setBaoyuMedicineAmount(((BigDecimal) result.get("nurseryDrugAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setBaoyuVaccinationAmount(((BigDecimal) result.get("nurseryVaccineAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));

        material.setYufeiFeedQuantity(((BigDecimal) result.get("fattenFeedCount")).intValue());
        material.setYufeiFeedAmount(((BigDecimal) result.get("fattenFeedAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setYufeiMaterialAmount(((BigDecimal) result.get("fattenMaterialAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setYufeiMaterialQuantity(((BigDecimal) result.get("fattenMaterialCount")).intValue());
        material.setYufeiConsumeAmount(((BigDecimal) result.get("fattenConsumerAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setYufeiMedicineAmount(((BigDecimal) result.get("fattenDrugAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setYufeiVaccinationAmount(((BigDecimal) result.get("fattenVaccineAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));

        material.setBoarFeedQuantity(((BigDecimal) result.get("boarFeedCount")).intValue());
        material.setBoarFeedAmount(((BigDecimal) result.get("boarFeedAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setBoarMaterialAmount(((BigDecimal) result.get("boarMaterialAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setBoarMaterialQuantity(((BigDecimal) result.get("boarMaterialCount")).intValue());
        material.setBoarConsumeAmount(((BigDecimal) result.get("boarConsumerAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setBoarMedicineAmount(((BigDecimal) result.get("boarDrugAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
        material.setBoarVaccinationAmount(((BigDecimal) result.get("boarVaccineAmount")).divide(new BigDecimal(100), 4, BigDecimal.ROUND_HALF_DOWN));
    }

}
