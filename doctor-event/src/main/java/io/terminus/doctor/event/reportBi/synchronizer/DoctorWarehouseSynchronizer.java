package io.terminus.doctor.event.reportBi.synchronizer;

import ch.qos.logback.core.joran.conditional.ElseAction;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorWarehouseReportDao;
import io.terminus.doctor.event.dao.reportBi.DoctorReportMaterialDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.enums.ReportTime;
import io.terminus.doctor.event.model.DoctorReportMaterial;
import io.terminus.doctor.event.reportBi.helper.DateHelper;
import io.terminus.doctor.event.reportBi.model.WarehouseReportTempResult;
import io.terminus.doctor.event.service.DoctorPigReportReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.parana.auth.model.App;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by sunbo@terminus.io on 2018/1/15.
 */
@Slf4j
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


    /**
     * 获取指定日期内更新的领用记录的操作日期
     *
     * @param date
     * @return
     */
    public List<Date> getChangedDate(Date date) {

        return doctorWarehouseReportDao.getChangedDate(date);
    }

    /**
     * 刷新某一日期所在的日、周、月、季、年
     * 猪场和公司10个
     *
     * @param date
     */
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

        if (dimensionCriteria.getOrzType().equals(OrzDimension.CLIQUE.getValue())) {
            log.warn("暂无集团维度");
            return;
        }

        OrzDimension orzDimension = OrzDimension.from(dimensionCriteria.getOrzType());
        DateDimension dateDimension = DateDimension.from(dimensionCriteria.getDateType());
//
//        if (dimensionCriteria.getOrzType().equals(OrzDimension.FARM.getValue())) {
//
//        } else {

//        Map<String, Date> maxAndMinDate = doctorWarehouseReportDao.getMaxAndMinDate();
//        if (null == maxAndMinDate) //自开天辟地以来，从来没有发生过领用。虽然这不可能发生
//            return;

        //从BI表中获取所有有org的，只需要保证org的时间维度不中断

        Map<Long/*公司或猪场编号*/, List<WarehouseReportTempResult>> tempResultMap = doctorWarehouseReportDao.count(dimensionCriteria.getDateType(), dimensionCriteria.getOrzType());


        Map<Long/*公司编号*/, List<DoctorOrg>> orgMap = new HashMap<>();
        Map<Long/*猪场编号*/, List<DoctorFarm>> farmMap = new HashMap<>();
        if (dimensionCriteria.getOrzType().equals(OrzDimension.ORG.getValue()))
            orgMap = RespHelper.orServEx(doctorOrgReadService.findOrgByIds(tempResultMap.keySet().stream().collect(Collectors.toList())))
                    .stream()
                    .collect(Collectors.groupingBy(DoctorOrg::getId));
        else
            farmMap = RespHelper.orServEx(doctorFarmReadService.findFarmsByIds(tempResultMap.keySet().stream().collect(Collectors.toList())))
                    .stream()
                    .collect(Collectors.groupingBy(DoctorFarm::getId));

//        tempResultMap.forEach((k, v) -> {
        for (Long k : tempResultMap.keySet()) {

            List<WarehouseReportTempResult> v = tempResultMap.get(k);

            //需要按照该公司最早和最晚的领用没个期间生成一笔，防止某一个日期没有发生领用导致日期中断
            //按照时间从早到晚排序
            List<WarehouseReportTempResult> sortedOrgResult = v.stream().sorted((t1, t2) -> t1.getDate().compareTo(t2.getDate())).collect(Collectors.toList());

            Date minDate = sortedOrgResult.get(0).getDate();
            Date maxDate = sortedOrgResult.get(sortedOrgResult.size() - 1).getDate();
            Date current = DateHelper.withDateStartDay(minDate, dateDimension);//当前期限的第一天

            sortedOrgResult.stream().forEach(s -> s.setDate(DateHelper.withDateStartDay(s.getDate(), dateDimension)));
            Map<Date, List<WarehouseReportTempResult>> eachPeriod = sortedOrgResult.stream().collect(Collectors.groupingBy(WarehouseReportTempResult::getDate));

            Date last;
            if (dateDimension == DateDimension.DAY)
                last = DateUtils.addDays(maxDate, 1);
            else if (dateDimension == DateDimension.WEEK) {
                last = DateUtils.addWeeks(DateHelper.withDateStartDay(maxDate, dateDimension), 1);
            } else if (dateDimension == DateDimension.MONTH) {
                last = DateUtils.addMonths(DateHelper.withDateStartDay(maxDate, dateDimension), 1);
            } else if (dateDimension == DateDimension.QUARTER)
                last = DateUtils.addMonths(DateHelper.withDateStartDay(maxDate, dateDimension), 3);
            else
                last = DateUtils.addYears(DateHelper.withDateStartDay(maxDate, dateDimension), 1);

            while (current.before(last)) {


                DoctorDimensionCriteria criteria = new DoctorDimensionCriteria();
                criteria.setOrzId(k);
                criteria.setDateType(dateDimension.getValue());
                criteria.setOrzType(OrzDimension.ORG.getValue());
                criteria.setSumAt(current);

                DoctorReportMaterial material;
                material = doctorReportMaterialDao.findByDimension(criteria);
                if (null == material)
                    material = new DoctorReportMaterial();

                material.setOrzId(k);
                material.setOrzType(orzDimension.getValue());
                if (orzDimension.getValue().equals(OrzDimension.ORG.getValue()))
                    material.setOrzName(orgMap.containsKey(k) ? orgMap.get(k).get(0).getName() : "");
                else
                    material.setOrzName(farmMap.containsKey(k) ? farmMap.get(k).get(0).getName() : "");

                material.setDateType(dateDimension.getValue());
                material.setSumAt(current);
                material.setSumAtName(DateHelper.dateCN(current, dateDimension));


                Map<WareHouseType, DoctorWarehouseReportDao.AmountAndQuantityDto> materialUsage = getMaterialUsageUnderPigType(getPigType(eachPeriod.get(current), PigType.RESERVE.getValue()));
                material.setHoubeiFeedQuantity(materialUsage.get(WareHouseType.FEED).getQuantity().intValue());
                material.setHoubeiFeedAmount(materialUsage.get(WareHouseType.FEED).getAmount());
                material.setHoubeiMaterialAmount(materialUsage.get(WareHouseType.MATERIAL).getAmount());
                material.setHoubeiMaterialQuantity(materialUsage.get(WareHouseType.MATERIAL).getQuantity().intValue());
                material.setHoubeiVaccinationAmount(materialUsage.get(WareHouseType.VACCINATION).getAmount());
                material.setHoubeiMedicineAmount(materialUsage.get(WareHouseType.MEDICINE).getAmount());
                material.setHoubeiConsumeAmount(materialUsage.get(WareHouseType.CONSUME).getAmount());

                materialUsage = getMaterialUsageUnderPigType(getPigType(eachPeriod.get(current), PigType.MATE_SOW.getValue(), PigType.PREG_SOW.getValue()));
                material.setPeihuaiFeedQuantity(materialUsage.get(WareHouseType.FEED).getQuantity().intValue());
                material.setPeihuaiFeedAmount(materialUsage.get(WareHouseType.FEED).getAmount());
                material.setPeihuaiMaterialAmount(materialUsage.get(WareHouseType.MATERIAL).getAmount());
                material.setPeihuaiMaterialQuantity(materialUsage.get(WareHouseType.MATERIAL).getQuantity().intValue());
                material.setPeihuaiVaccinationAmount(materialUsage.get(WareHouseType.VACCINATION).getAmount());
                material.setPeihuaiMedicineAmount(materialUsage.get(WareHouseType.MEDICINE).getAmount());
                material.setPeihuaiConsumeAmount(materialUsage.get(WareHouseType.CONSUME).getAmount());

                materialUsage = getMaterialUsageUnderPigType(getPigType(eachPeriod.get(current), PigType.DELIVER_SOW.getValue()));
                material.setSowFeedQuantity(materialUsage.get(WareHouseType.FEED).getQuantity().intValue());
                material.setSowFeedAmount(materialUsage.get(WareHouseType.FEED).getAmount());
                material.setSowMaterialAmount(materialUsage.get(WareHouseType.MATERIAL).getAmount());
                material.setSowMaterialQuantity(materialUsage.get(WareHouseType.MATERIAL).getQuantity().intValue());
                material.setSowVaccinationAmount(materialUsage.get(WareHouseType.VACCINATION).getAmount());
                material.setSowMedicineAmount(materialUsage.get(WareHouseType.MEDICINE).getAmount());
                material.setSowConsumeAmount(materialUsage.get(WareHouseType.CONSUME).getAmount());

                materialUsage = getMaterialUsageUnderPigType(getPigType(eachPeriod.get(current), 22));
                material.setPigletFeedQuantity(materialUsage.get(WareHouseType.FEED).getQuantity().intValue());
                material.setPigletFeedAmount(materialUsage.get(WareHouseType.FEED).getAmount());
                material.setPigletMaterialAmount(materialUsage.get(WareHouseType.MATERIAL).getAmount());
                material.setPigletMaterialQuantity(materialUsage.get(WareHouseType.MATERIAL).getQuantity().intValue());
                material.setPigletVaccinationAmount(materialUsage.get(WareHouseType.VACCINATION).getAmount());
                material.setPigletMedicineAmount(materialUsage.get(WareHouseType.MEDICINE).getAmount());
                material.setPigletConsumeAmount(materialUsage.get(WareHouseType.CONSUME).getAmount());

                materialUsage = getMaterialUsageUnderPigType(getPigType(eachPeriod.get(current), PigType.NURSERY_PIGLET.getValue()));
                material.setBaoyuFeedQuantity(materialUsage.get(WareHouseType.FEED).getQuantity().intValue());
                material.setBaoyuFeedAmount(materialUsage.get(WareHouseType.FEED).getAmount());
                material.setBaoyuMaterialAmount(materialUsage.get(WareHouseType.MATERIAL).getAmount());
                material.setBaoyuMaterialQuantity(materialUsage.get(WareHouseType.MATERIAL).getQuantity().intValue());
                material.setBaoyuVaccinationAmount(materialUsage.get(WareHouseType.VACCINATION).getAmount());
                material.setBaoyuMedicineAmount(materialUsage.get(WareHouseType.MEDICINE).getAmount());
                material.setBaoyuConsumeAmount(materialUsage.get(WareHouseType.CONSUME).getAmount());

                materialUsage = getMaterialUsageUnderPigType(getPigType(eachPeriod.get(current), PigType.FATTEN_PIG.getValue()));
                material.setYufeiFeedQuantity(materialUsage.get(WareHouseType.FEED).getQuantity().intValue());
                material.setYufeiFeedAmount(materialUsage.get(WareHouseType.FEED).getAmount());
                material.setYufeiMaterialAmount(materialUsage.get(WareHouseType.MATERIAL).getAmount());
                material.setYufeiMaterialQuantity(materialUsage.get(WareHouseType.MATERIAL).getQuantity().intValue());
                material.setYufeiVaccinationAmount(materialUsage.get(WareHouseType.VACCINATION).getAmount());
                material.setYufeiMedicineAmount(materialUsage.get(WareHouseType.MEDICINE).getAmount());
                material.setYufeiConsumeAmount(materialUsage.get(WareHouseType.CONSUME).getAmount());

                materialUsage = getMaterialUsageUnderPigType(getPigType(eachPeriod.get(current), PigType.BOAR.getValue()));
                material.setBoarFeedQuantity(materialUsage.get(WareHouseType.FEED).getQuantity().intValue());
                material.setBoarFeedAmount(materialUsage.get(WareHouseType.FEED).getAmount());
                material.setBoarMaterialAmount(materialUsage.get(WareHouseType.MATERIAL).getAmount());
                material.setBoarMaterialQuantity(materialUsage.get(WareHouseType.MATERIAL).getQuantity().intValue());
                material.setBoarVaccinationAmount(materialUsage.get(WareHouseType.VACCINATION).getAmount());
                material.setBoarMedicineAmount(materialUsage.get(WareHouseType.MEDICINE).getAmount());
                material.setBoarConsumeAmount(materialUsage.get(WareHouseType.CONSUME).getAmount());


                if (null == material.getId())
                    doctorReportMaterialDao.create(material);
                else
                    doctorReportMaterialDao.update(material);

                if (dateDimension == DateDimension.DAY)
                    current = DateUtils.addDays(current, 1);
                else if (dateDimension == DateDimension.WEEK) {
                    current = DateUtils.addWeeks(DateHelper.withDateStartDay(current, dateDimension), 1);
                } else if (dateDimension == DateDimension.MONTH) {
                    current = DateUtils.addMonths(DateHelper.withDateStartDay(current, dateDimension), 1);
                } else if (dateDimension == DateDimension.QUARTER)
                    current = DateUtils.addMonths(DateHelper.withDateStartDay(current, dateDimension), 3);
                else
                    current = DateUtils.addYears(DateHelper.withDateStartDay(current, dateDimension), 1);
            }
        }
//        });
//}
    }

    /**
     * 获取某一个猪舍类型下的各种物料类型的金额和数量
     * 猪舍类型下有可能什么都没有
     * 不是每个物料类型都会有
     *
     * @param reportTempResults 某一个公司下所有猪舍类型下所有物料类型的消耗金额和数量
     * @param pigTypes          需要取的猪舍类型，多个pigType可能只表示一个猪舍类型
     * @return
     */
    private List<WarehouseReportTempResult> getPigType(List<WarehouseReportTempResult> reportTempResults, Integer... pigTypes) {
        List<WarehouseReportTempResult> materialUsedUnderPigType = new ArrayList<>();

        if (reportTempResults != null) {
            reportTempResults.parallelStream().forEach(r -> {
                if (Stream.of(pigTypes).anyMatch(p -> r.getPigType().equals(p))) {
                    materialUsedUnderPigType.add(r);
                }
            });
        }


        return Collections.unmodifiableList(materialUsedUnderPigType);
    }

    /**
     * 获取各个类型的物料消耗的数量和金额
     *
     * @param reportTempResults 某一个猪舍类型下的物料消耗
     * @return
     */
    private Map<WareHouseType, DoctorWarehouseReportDao.AmountAndQuantityDto> getMaterialUsageUnderPigType(List<WarehouseReportTempResult> reportTempResults) {
//        if (reportTempResults.isEmpty())
//            return Collections.emptyMap();
//
        Map<WareHouseType, DoctorWarehouseReportDao.AmountAndQuantityDto> result = new HashMap<>();
        Map<Integer, List<WarehouseReportTempResult>> eachMaterialType = reportTempResults.stream().collect(Collectors.groupingBy(WarehouseReportTempResult::getType));


        Stream.of(WareHouseType.values()).forEach(w -> {
            if (!eachMaterialType.containsKey(w.getKey()))
                result.put(w, new DoctorWarehouseReportDao.AmountAndQuantityDto());
            else {
                BigDecimal totalQuantity = new BigDecimal(0);
                BigDecimal totalAmount = new BigDecimal(0);
                for (WarehouseReportTempResult r : eachMaterialType.get(w.getKey())) {
                    totalQuantity = totalQuantity.add(r.getQuantity());
                    totalAmount = totalAmount.add(r.getAmount());
                }
                result.put(w, new DoctorWarehouseReportDao.AmountAndQuantityDto(totalAmount, totalQuantity.longValue()));
            }
        });

        return Collections.unmodifiableMap(result);
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
