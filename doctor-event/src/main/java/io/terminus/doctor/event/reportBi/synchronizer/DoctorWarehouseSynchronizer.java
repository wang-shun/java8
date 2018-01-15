package io.terminus.doctor.event.reportBi.synchronizer;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorWarehouseReportDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportMaterialDao;
import io.terminus.doctor.event.enums.DateDimension;
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
            material.setDateType("日");
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType("猪场");

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
            material.setDateType("日");
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType("公司");

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

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), date, date);

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(date);
            material.setSumAtName(DateHelper.dateCN(date, DateDimension.DAY));
            material.setDateType("周");
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType("猪场");

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
            material.setDateType("周");
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType("公司");

            fill(material, result);

            doctorReportMaterialDao.create(material);
        });
        //月
        dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.MONTH);
        farmIds = doctorWarehouseReportDao.findApplyFarm(dateDuration.getStart(), dateDuration.getEnd());
        orgIds = new HashSet<>();
        for (Long f : farmIds) {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));
            if (null != farm) {
                orgIds.add(farm.getOrgId());
            }

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), date, date);

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(date);
            material.setSumAtName(DateHelper.dateCN(date, DateDimension.DAY));
            material.setDateType("月");
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType("猪场");

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
            material.setDateType("月");
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType("公司");

            fill(material, result);

            doctorReportMaterialDao.create(material);
        });
        //季
        dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.SEASON);
        farmIds = doctorWarehouseReportDao.findApplyFarm(dateDuration.getStart(), dateDuration.getEnd());
        orgIds = new HashSet<>();
        for (Long f : farmIds) {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));
            if (null != farm) {
                orgIds.add(farm.getOrgId());
            }

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), date, date);

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(date);
            material.setSumAtName(DateHelper.dateCN(date, DateDimension.DAY));
            material.setDateType("季");
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType("猪场");

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
            material.setDateType("季");
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType("公司");

            fill(material, result);

            doctorReportMaterialDao.create(material);
        });
        //年
        dateDuration = doctorPigReportReadService.getDuration(date, ReportTime.YEAR);
        farmIds = doctorWarehouseReportDao.findApplyFarm(dateDuration.getStart(), dateDuration.getEnd());
        orgIds = new HashSet<>();
        for (Long f : farmIds) {

            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(f));
            if (null != farm) {
                orgIds.add(farm.getOrgId());
            }

            Map<String, Object> result = doctorWarehouseReportDao.count(Collections.singletonList(f), date, date);

            DoctorReportMaterial material = new DoctorReportMaterial();
            material.setSumAt(date);
            material.setSumAtName(DateHelper.dateCN(date, DateDimension.DAY));
            material.setDateType("年");
            material.setOrzId(f);
            material.setOrzName(farm == null ? "" : farm.getName());
            material.setOrzType("猪场");

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
            material.setDateType("年");
            material.setOrzId(o);
            material.setOrzName(org == null ? "" : org.getName());
            material.setOrzType("公司");

            fill(material, result);

            doctorReportMaterialDao.create(material);
        });

    }


    private void fill(DoctorReportMaterial material, Map<String, Object> result) {
        material.setHoubeiFeedQuantity(Integer.parseInt(result.get("houbeiFeedCount".toUpperCase()).toString()));
        material.setHoubeiFeedAmount(Double.parseDouble(result.get("houbeiFeedAmount".toUpperCase()).toString()));
        material.setHoubeiMaterialAmount(Double.parseDouble(result.get("houbeiMaterialAmount".toUpperCase()).toString()));
        material.setHoubeiMaterialQuantity(Integer.parseInt(result.get("houbeiMaterialCount".toUpperCase()).toString()));
        material.setHoubeiConsumeAmount(Double.parseDouble(result.get("houbeiConsumerAmount".toUpperCase()).toString()));
        material.setHoubeiMedicineAmount(Double.parseDouble(result.get("houbeiDrugAmount".toUpperCase()).toString()));
        material.setHoubeiVaccinationAmount(Double.parseDouble(result.get("houbeiVaccineAmount".toUpperCase()).toString()));

        material.setPeihuaiFeedQuantity(Integer.parseInt(result.get("peiHuaiFeedCount".toUpperCase()).toString()));
        material.setPeihuaiFeedAmount(Double.parseDouble(result.get("peiHuaiFeedAmount".toUpperCase()).toString()));
        material.setPeihuaiMaterialAmount(Double.parseDouble(result.get("peiHuaiMaterialAmount".toUpperCase()).toString()));
        material.setPeihuaiMaterialQuantity(Integer.parseInt(result.get("peiHuaiMaterialCount".toUpperCase()).toString()));
        material.setPeihuaiConsumeAmount(Double.parseDouble(result.get("peiHuaiConsumerAmount".toUpperCase()).toString()));
        material.setPeihuaiMedicineAmount(Double.parseDouble(result.get("peiHuaiDrugAmount".toUpperCase()).toString()));
        material.setPeihuaiVaccinationAmount(Double.parseDouble(result.get("peiHuaiVaccineAmount".toUpperCase()).toString()));

        material.setSowFeedQuantity(Integer.parseInt(result.get("farrowSowFeedCount".toUpperCase()).toString()));
        material.setSowFeedAmount(Double.parseDouble(result.get("farrowSowFeedAmount".toUpperCase()).toString()));
        material.setSowMaterialAmount(Double.parseDouble(result.get("farrowSowMaterialAmount".toUpperCase()).toString()));
        material.setSowMaterialQuantity(Integer.parseInt(result.get("farrowSowMaterialCount".toUpperCase()).toString()));
        material.setSowConsumeAmount(Double.parseDouble(result.get("farrowSowConsumerAmount".toUpperCase()).toString()));
        material.setSowMedicineAmount(Double.parseDouble(result.get("farrowSowDrugAmount".toUpperCase()).toString()));
        material.setSowVaccinationAmount(Double.parseDouble(result.get("farrowSowVaccineAmount".toUpperCase()).toString()));

        material.setPigletFeedQuantity(Integer.parseInt(result.get("farrowFeedCount".toUpperCase()).toString()));
        material.setPigletFeedAmount(Double.parseDouble(result.get("farrowFeedAmount".toUpperCase()).toString()));
        material.setPigletMaterialAmount(Double.parseDouble(result.get("farrowMaterialAmount".toUpperCase()).toString()));
        material.setPigletMaterialQuantity(Integer.parseInt(result.get("farrowMaterialCount".toUpperCase()).toString()));
        material.setPigletConsumeAmount(Double.parseDouble(result.get("farrowConsumerAmount".toUpperCase()).toString()));
        material.setPigletMedicineAmount(Double.parseDouble(result.get("farrowDrugAmount".toUpperCase()).toString()));
        material.setPigletVaccinationAmount(Double.parseDouble(result.get("farrowVaccineAmount".toUpperCase()).toString()));

        material.setBaoyuFeedQuantity(Integer.parseInt(result.get("nurseryFeedCount".toUpperCase()).toString()));
        material.setBaoyuFeedAmount(Double.parseDouble(result.get("nurseryFeedAmount".toUpperCase()).toString()));
        material.setBaoyuMaterialAmount(Double.parseDouble(result.get("nurseryMaterialAmount".toUpperCase()).toString()));
        material.setBaoyuMaterialQuantity(Integer.parseInt(result.get("nurseryMaterialCount".toUpperCase()).toString()));
        material.setBaoyuConsumeAmount(Double.parseDouble(result.get("nurseryConsumerAmount".toUpperCase()).toString()));
        material.setBaoyuMedicineAmount(Double.parseDouble(result.get("nurseryDrugAmount".toUpperCase()).toString()));
        material.setBaoyuVaccinationAmount(Double.parseDouble(result.get("nurseryVaccineAmount".toUpperCase()).toString()));

        material.setYufeiFeedQuantity(Integer.parseInt(result.get("fattenFeedCount".toUpperCase()).toString()));
        material.setYufeiFeedAmount(Double.parseDouble(result.get("fattenFeedAmount".toUpperCase()).toString()));
        material.setYufeiMaterialAmount(Double.parseDouble(result.get("fattenMaterialAmount".toUpperCase()).toString()));
        material.setYufeiMaterialQuantity(Integer.parseInt(result.get("fattenMaterialCount".toUpperCase()).toString()));
        material.setYufeiConsumeAmount(Double.parseDouble(result.get("fattenConsumerAmount".toUpperCase()).toString()));
        material.setYufeiMedicineAmount(Double.parseDouble(result.get("fattenDrugAmount".toUpperCase()).toString()));
        material.setYufeiVaccinationAmount(Double.parseDouble(result.get("fattenVaccineAmount".toUpperCase()).toString()));

        material.setBoarFeedQuantity(Integer.parseInt(result.get("boarFeedCount".toUpperCase()).toString()));
        material.setBoarFeedAmount(Double.parseDouble(result.get("boarFeedAmount".toUpperCase()).toString()));
        material.setBoarMaterialAmount(Double.parseDouble(result.get("boarMaterialAmount".toUpperCase()).toString()));
        material.setBoarMaterialQuantity(Integer.parseInt(result.get("boarMaterialCount".toUpperCase()).toString()));
        material.setBoarConsumeAmount(Double.parseDouble(result.get("boarConsumerAmount".toUpperCase()).toString()));
        material.setBoarMedicineAmount(Double.parseDouble(result.get("boarDrugAmount".toUpperCase()).toString()));
        material.setBoarVaccinationAmount(Double.parseDouble(result.get("boarVaccineAmount".toUpperCase()).toString()));
    }

}
