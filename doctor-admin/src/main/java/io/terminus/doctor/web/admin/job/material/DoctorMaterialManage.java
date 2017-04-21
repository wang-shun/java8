package io.terminus.doctor.web.admin.job.material;

import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.basic.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.basic.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.basic.service.DoctorMaterialPriceInWareHouseReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorMasterialDatailsGroup;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupMaterialWriteServer;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * Created by terminus on 2017/4/20.
 */
@Component
public class DoctorMaterialManage {

    @RpcConsumer
    private DoctorMaterialPriceInWareHouseReadService materialPriceInWareHouseReadService;
    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService materialConsumeProviderReadService;
    @RpcConsumer
    private DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService;
    @RpcConsumer
    private DoctorGroupReadService doctorGroupReadService;
    @RpcConsumer
    private DoctorBarnReadService doctorBarnReadService;
    @RpcConsumer
    private DoctorGroupMaterialWriteServer doctorGroupMaterialWriteServer;


    public void runDoctorGroupMaterialWareHouse(List<Long> farmIds, Integer flag) {

        List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups;
        for (Long farmId : farmIds) {
            doctorMasterialDatailsGroups = getPagingGroupMaterialHouse(farmId, flag);
            if (doctorMasterialDatailsGroups == null || doctorMasterialDatailsGroups.isEmpty()) {
                continue;
            }
            doctorGroupMaterialWriteServer.insterDoctorGroupMaterialWareDetails(doctorMasterialDatailsGroups);
        }
    }

    public void runDoctorGroupMaterialWareDetails(List<Long> farmIds, Integer flag) {
        List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups;
        for (Long farmId : farmIds) {
            doctorMasterialDatailsGroups = getPagingGroupMaterialDetails(farmId, flag);
            if (doctorMasterialDatailsGroups == null || doctorMasterialDatailsGroups.isEmpty()) {
                continue;
            }
            doctorGroupMaterialWriteServer.insterDoctorGroupMaterialWareDetails(doctorMasterialDatailsGroups);
        }
    }

    public void runDoctorGroupMaterialWare(List<Long> farmIds, Integer flag) {
            List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups;
            for (Long farmId : farmIds) {
                doctorMasterialDatailsGroups = getPagingGroupMaterial(farmId, flag);
                if (doctorMasterialDatailsGroups == null || doctorMasterialDatailsGroups.isEmpty()) {
                    continue;
                }
                doctorGroupMaterialWriteServer.insterDoctorGroupMaterialWareDetails(doctorMasterialDatailsGroups);
            }
    }

    private List<DoctorMasterialDatailsGroup> getPagingGroupMaterialHouse(Long farmId, Integer flag) {
        List<DoctorMaterialConsumeProvider> list = RespHelper.or500(materialConsumeProviderReadService.findMaterialByGroup(farmId, null, null, null, null, null, null, null, null));
        List<DoctorMasterialDatailsGroup> doctorMaterialDatails = Lists.newArrayList();
        List<DoctorMaterialConsumeProvider> listOverride = buildDoctorMaterialConsumeProvider(list);
        for (DoctorMaterialConsumeProvider lists : listOverride) {
            DoctorMasterialDatailsGroup doctorMaterialDatail = new DoctorMasterialDatailsGroup();
            doctorMaterialDatail.setBarnName(lists.getBarnName());
            doctorMaterialDatail.setBarnId(lists.getBarnId());
            doctorMaterialDatail.setMaterialId(lists.getMaterialId());
            doctorMaterialDatail.setMaterialName(lists.getMaterialName());
            doctorMaterialDatail.setType(lists.getType());
            doctorMaterialDatail.setTypeName(DoctorMaterialConsumeProvider.EVENT_TYPE.from(lists.getEventType()).getDesc());
            DoctorMaterialInWareHouse doctorMaterialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.findMaterialUnits(
                    farmId,
                    lists.getMaterialId(),
                    lists.getWareHouseId()));
            if (doctorMaterialInWareHouse != null) {
                doctorMaterialDatail.setUnitName(doctorMaterialInWareHouse.getUnitName());
            }
            DoctorBarn doctorBarn = null;
            if (lists.getBarnId() != null) {
                doctorBarn = RespHelper.or500(doctorBarnReadService.findBarnById(lists.getBarnId()));
            }
            if (doctorBarn != null) {
                doctorMaterialDatail.setPeople(doctorBarn.getStaffName());
            }
            DoctorGroup doctorGroups =RespHelper.or500(doctorGroupReadService.findGroupById(lists.getGroupId()));
            if (doctorGroups != null) {
                doctorMaterialDatail.setOpenAt(doctorGroups.getOpenAt());
                doctorMaterialDatail.setCloseAt(doctorGroups.getCloseAt());
            }
            doctorMaterialDatail.setFarmId(farmId);
            doctorMaterialDatail.setMaterialType(lists.getType());
            doctorMaterialDatail.setEventAt(lists.getEventTime());
            doctorMaterialDatail.setGroupId(lists.getGroupId());
            doctorMaterialDatail.setGroupName(lists.getGroupCode());
            doctorMaterialDatail.setPrice(lists.getUnitPrice());
            doctorMaterialDatail.setNumber(lists.getEventCount());
            doctorMaterialDatail.setPriceSum(lists.getUnitPrice() * lists.getEventCount());
            doctorMaterialDatail.setWareHouseId(lists.getWareHouseId());
            doctorMaterialDatail.setWareHouseName(lists.getWareHouseName());
            doctorMaterialDatail.setFlushDate(DateUtil.monthStart(new Date()));
            doctorMaterialDatail.setFlag(flag);
            doctorMaterialDatails.add(doctorMaterialDatail);
        }
        return doctorMaterialDatails;
    }


    public List<DoctorMasterialDatailsGroup> getPagingGroupMaterialDetails(Long farmId, Integer flag) {
        List<DoctorMaterialConsumeProvider> list = RespHelper.or500(materialConsumeProviderReadService.findMaterialConsume(farmId, null, null, null, null, null, null, null, null, null, null, null));
        List<DoctorMasterialDatailsGroup> doctorMaterialDatails = Lists.newArrayList();
        List<DoctorMaterialConsumeProvider> listOverride = buildDoctorMaterialConsumeProvider(list);
        for (DoctorMaterialConsumeProvider lists : listOverride) {
            DoctorMasterialDatailsGroup doctorMaterialDatail = new DoctorMasterialDatailsGroup();
            doctorMaterialDatail.setBarnName(lists.getBarnName());
            doctorMaterialDatail.setBarnId(lists.getBarnId());
            doctorMaterialDatail.setMaterialId(lists.getMaterialId());
            doctorMaterialDatail.setMaterialName(lists.getMaterialName());
            doctorMaterialDatail.setType(lists.getType());
            doctorMaterialDatail.setTypeName(DoctorMaterialConsumeProvider.EVENT_TYPE.from(lists.getEventType()).getDesc());
            DoctorMaterialInWareHouse doctorMaterialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.findMaterialUnits(
                    farmId,
                    lists.getMaterialId(),
                    lists.getWareHouseId()));
            if (doctorMaterialInWareHouse != null) {
                doctorMaterialDatail.setUnitName(doctorMaterialInWareHouse.getUnitName());
            }
            DoctorBarn doctorBarn = null;
            if (lists.getBarnId() != null) {
                doctorBarn = RespHelper.or500(doctorBarnReadService.findBarnById(lists.getBarnId()));
            }
            if (doctorBarn != null) {
                doctorMaterialDatail.setPeople(doctorBarn.getStaffName());
            }
            DoctorGroup doctorGroups =RespHelper.or500(doctorGroupReadService.findGroupById(lists.getGroupId()));
            if (doctorGroups != null) {
                doctorMaterialDatail.setOpenAt(doctorGroups.getOpenAt());
                doctorMaterialDatail.setCloseAt(doctorGroups.getCloseAt());
            }
            doctorMaterialDatail.setFarmId(farmId);
            doctorMaterialDatail.setMaterialType(lists.getType());
            doctorMaterialDatail.setEventAt(lists.getEventTime());
            doctorMaterialDatail.setGroupId(lists.getGroupId());
            doctorMaterialDatail.setGroupName(lists.getGroupCode());
            doctorMaterialDatail.setPrice(lists.getUnitPrice());
            doctorMaterialDatail.setNumber(lists.getEventCount());
            doctorMaterialDatail.setPriceSum(lists.getUnitPrice() * lists.getEventCount());
            doctorMaterialDatail.setWareHouseId(lists.getWareHouseId());
            doctorMaterialDatail.setWareHouseName(lists.getWareHouseName());
            doctorMaterialDatail.setFlushDate(DateUtil.monthStart(new Date()));
            doctorMaterialDatail.setFlag(flag);
            doctorMaterialDatails.add(doctorMaterialDatail);
        }
        return doctorMaterialDatails;
    }


    public List<DoctorMasterialDatailsGroup> getPagingGroupMaterial(Long farmId, Integer flag) {

        List<DoctorGroup> doctorGroup = RespHelper.or500(doctorGroupReadService.findGroupIds(farmId, null, null));
        List<Long> groupIds = doctorGroup.stream()
                .map(s -> s.getId()).collect(Collectors.toList());

        if (groupIds.isEmpty()){
            return null;
        }
        List<DoctorMaterialConsumeProvider> list = RespHelper.or500(materialConsumeProviderReadService.findMaterialByGroup(
                farmId,
                null,
                null,
                groupIds,
                null,
                null,
                null,
                null,
                null
        ));
        List<DoctorMasterialDatailsGroup> doctorMaterialDatails = Lists.newArrayList();
        List<DoctorMaterialConsumeProvider> listOverride = buildDoctorMaterialConsumeProvider(list);
        for (DoctorMaterialConsumeProvider lists : listOverride) {
            DoctorMasterialDatailsGroup doctorMaterialDatail = new DoctorMasterialDatailsGroup();
            doctorMaterialDatail.setBarnName(lists.getBarnName());
            doctorMaterialDatail.setBarnId(lists.getBarnId());
            doctorMaterialDatail.setMaterialId(lists.getMaterialId());
            doctorMaterialDatail.setMaterialName(lists.getMaterialName());
            doctorMaterialDatail.setType(lists.getType());
            doctorMaterialDatail.setTypeName(DoctorMaterialConsumeProvider.EVENT_TYPE.from(lists.getEventType()).getDesc());
            DoctorMaterialInWareHouse doctorMaterialInWareHouse = RespHelper.or500(doctorMaterialInWareHouseReadService.findMaterialUnits(
                    farmId,
                    lists.getMaterialId(),
                    lists.getWareHouseId()));
            if (doctorMaterialInWareHouse != null) {
                doctorMaterialDatail.setUnitName(doctorMaterialInWareHouse.getUnitName());
            }
            DoctorBarn doctorBarn = null;
            if (lists.getBarnId() != null) {
                doctorBarn = RespHelper.or500(doctorBarnReadService.findBarnById(lists.getBarnId()));
            }
            if (doctorBarn != null) {
                doctorMaterialDatail.setPeople(doctorBarn.getStaffName());
            }
            DoctorGroup doctorGroups =RespHelper.or500(doctorGroupReadService.findGroupById(lists.getGroupId()));
            if (doctorGroups != null) {
                doctorMaterialDatail.setOpenAt(doctorGroups.getOpenAt());
                doctorMaterialDatail.setCloseAt(doctorGroups.getCloseAt());
            }
            doctorMaterialDatail.setFarmId(lists.getFarmId());
            doctorMaterialDatail.setMaterialType(lists.getType());
            doctorMaterialDatail.setEventAt(lists.getEventTime());
            doctorMaterialDatail.setGroupId(lists.getGroupId());
            doctorMaterialDatail.setGroupName(lists.getGroupCode());
            doctorMaterialDatail.setPrice(lists.getUnitPrice());
            doctorMaterialDatail.setNumber(lists.getEventCount());
            doctorMaterialDatail.setPriceSum(lists.getUnitPrice() * lists.getEventCount());
            doctorMaterialDatail.setWareHouseId(lists.getWareHouseId());
            doctorMaterialDatail.setWareHouseName(lists.getWareHouseName());
            doctorMaterialDatail.setFlushDate(DateUtil.monthStart(new Date()));
            doctorMaterialDatail.setFlag(flag);
            doctorMaterialDatails.add(doctorMaterialDatail);

        }
        return doctorMaterialDatails;
    }


    private List<DoctorMaterialConsumeProvider> buildDoctorMaterialConsumeProvider(List<DoctorMaterialConsumeProvider> list) {
        List<DoctorMaterialConsumeProvider> listOverride = new ArrayList<>(500);
        DoctorMaterialConsumeProvider doctorMaterialConsumeProviderOverride;
        for (int i = 0 , length = list.size(); i < length ; i++) {
            if(list.get(i).getExtra() != null && list.get(i).getExtraMap().containsKey("consumePrice")) {
                List<Map<String, Object>> priceCompose = (ArrayList) list.get(i).getExtraMap().get("consumePrice");
                for(Map<String, Object> eachPrice : priceCompose) {
                    doctorMaterialConsumeProviderOverride = new DoctorMaterialConsumeProvider();
                    Long providerIdfd = Long.valueOf(eachPrice.get("providerId").toString());
                    if (isNull(providerIdfd)) {
                        providerIdfd = -1L;
                    }
                    Long unitPrice = Long.valueOf(eachPrice.get("unitPrice").toString());
                    Double count = Double.valueOf(eachPrice.get("count").toString());
                    doctorMaterialConsumeProviderOverride.setMaterialName(list.get(i).getMaterialName());
                    doctorMaterialConsumeProviderOverride.setUnitPrice(unitPrice);
                    doctorMaterialConsumeProviderOverride.setFarmId(list.get(i).getFarmId());
                    doctorMaterialConsumeProviderOverride.setMaterialId(list.get(i).getMaterialId());
                    doctorMaterialConsumeProviderOverride.setWareHouseId(list.get(i).getWareHouseId());
                    doctorMaterialConsumeProviderOverride.setBarnId(list.get(i).getBarnId());
                    doctorMaterialConsumeProviderOverride.setBarnName(list.get(i).getBarnName());
                    doctorMaterialConsumeProviderOverride.setEventTime(list.get(i).getEventTime());
                    doctorMaterialConsumeProviderOverride.setGroupCode(list.get(i).getGroupCode());
                    doctorMaterialConsumeProviderOverride.setGroupId(list.get(i).getGroupId());
                    doctorMaterialConsumeProviderOverride.setWareHouseName(list.get(i).getWareHouseName());
                    doctorMaterialConsumeProviderOverride.setEventCount(count);
                    doctorMaterialConsumeProviderOverride.setType(list.get(i).getType());
                    doctorMaterialConsumeProviderOverride.setProvider(providerIdfd);
                    doctorMaterialConsumeProviderOverride.setEventType(list.get(i).getEventType());
                    listOverride.add(doctorMaterialConsumeProviderOverride);
                }
            }else {
                if (isNull(list.get(i).getProviderFactoryId())) {
                    list.get(i).setProvider(-1L);
                }
                listOverride.add(list.get(i));
            }
        }
        return listOverride;
    }

}
