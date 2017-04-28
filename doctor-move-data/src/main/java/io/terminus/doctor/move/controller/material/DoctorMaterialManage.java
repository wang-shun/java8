package io.terminus.doctor.move.controller.material;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import io.terminus.doctor.move.dto.DoctorGroupEventTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

/**
 * 物料信息处理
 * Created by terminus on 2017/4/20.
 */
@Component
@Slf4j
public class DoctorMaterialManage {

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
            try {

                doctorMasterialDatailsGroups = getPagingGroupMaterialHouse(farmId, flag);
                if (doctorMasterialDatailsGroups == null || doctorMasterialDatailsGroups.isEmpty()) {
                    continue;
                }
                doctorGroupMaterialWriteServer.insterDoctorGroupMaterialWareDetails(doctorMasterialDatailsGroups);
            }catch (Exception e) {
                log.error("material ware house fail, farmId:{}", farmId);
            }
        }
    }

    public void runDoctorGroupMaterialWareDetails(List<Long> farmIds, Integer flag) {
        List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups;
        for (Long farmId : farmIds) {
            try {
                doctorMasterialDatailsGroups = getPagingGroupMaterialDetails(farmId, flag);
                if (doctorMasterialDatailsGroups == null || doctorMasterialDatailsGroups.isEmpty()) {
                    continue;
                }
                doctorGroupMaterialWriteServer.insterDoctorGroupMaterialWareDetails(doctorMasterialDatailsGroups);
            }catch (Exception e) {
                log.error("material group ware details fail, farmId:{}", farmId);
            }
        }
    }

    public void runDoctorGroupMaterialWare(List<Long> farmIds, Integer flag) {
            List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups;
            for (Long farmId : farmIds) {
                try {
                    doctorMasterialDatailsGroups = getPagingGroupMaterial(farmId, flag);
                    if (doctorMasterialDatailsGroups == null || doctorMasterialDatailsGroups.isEmpty()) {
                        continue;
                    }
                    doctorGroupMaterialWriteServer.insterDoctorGroupMaterialWareDetails(doctorMasterialDatailsGroups);
                }catch (Exception e) {
                    log.error("doctor group material ware fail, farmId:{}", farmId);
                }
            }
    }
    //物料仓库
    private List<DoctorMasterialDatailsGroup> getPagingGroupMaterialHouse(Long farmId, Integer flag) {
        List<DoctorMaterialConsumeProvider> list = RespHelper.or500(materialConsumeProviderReadService.findMaterialByGroup(farmId, null, null, null, null, null, null, null, null));
        return doctorMasterialDatailsGroups(farmId, flag, list);
    }

    //物料明细
    public List<DoctorMasterialDatailsGroup> getPagingGroupMaterialDetails(Long farmId, Integer flag) {
        List<DoctorMaterialConsumeProvider> list = RespHelper.or500(materialConsumeProviderReadService.findMaterialConsume(farmId, null, null, null, null, null, null, null, null, null, null, null));
        return doctorMasterialDatailsGroups(farmId, flag, list);
    }

    //猪群物料
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
        return doctorMasterialDatailsGroups(farmId, flag, list);
    }

    public List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups(Long farmId, Integer flag, List<DoctorMaterialConsumeProvider> list) {

        Map<Long, String> mapBarn = barnMap(farmId);
        Map<Long, DoctorGroupEventTime> mapGroup = groupEventTimeMap(farmId);
        List<DoctorMaterialInWareHouse> doctorMaterialInWareHouseList = RespHelper.or500(doctorMaterialInWareHouseReadService.allDoctorMaterialInWareHouse(farmId));
        List<DoctorMasterialDatailsGroup> doctorMaterialDatails = Lists.newArrayList();
        List<DoctorMaterialConsumeProvider> listOverride = buildDoctorMaterialConsumeProvider(list);
        DoctorMasterialDatailsGroup doctorMaterialDatail;
        for (DoctorMaterialConsumeProvider lists : listOverride) {
            doctorMaterialDatail = new DoctorMasterialDatailsGroup();
            doctorMaterialDatail.setBarnName(lists.getBarnName());
            doctorMaterialDatail.setBarnId(lists.getBarnId());
            doctorMaterialDatail.setMaterialId(lists.getMaterialId());
            doctorMaterialDatail.setMaterialName(lists.getMaterialName());
            doctorMaterialDatail.setType(lists.getType());
            doctorMaterialDatail.setTypeName(DoctorMaterialConsumeProvider.EVENT_TYPE.from(lists.getEventType()).getDesc());
            doctorMaterialDatail.setUnitName(materialInWareHousesList(doctorMaterialInWareHouseList, lists.getMaterialId(), lists.getWareHouseId()));

            if (lists.getBarnId() != null) {
                doctorMaterialDatail.setPeople(mapBarn.get(lists.getBarnId()));
            }
            if (lists.getGroupId() != null) {
                doctorMaterialDatail.setOpenAt(mapGroup.get(lists.getGroupId()).getOpenAt());
                doctorMaterialDatail.setCloseAt(mapGroup.get(lists.getGroupId()).getCloseAt());
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

    private Map<Long, String> barnMap(Long farmId){
        List<DoctorBarn> doctorBarnList = RespHelper.or500(doctorBarnReadService.findBarnsByFarmId(farmId));
        Map<Long, String> map = Maps.newHashMap();
        for (DoctorBarn doctorBarn : doctorBarnList) {
            map.put(doctorBarn.getId(), doctorBarn.getStaffName());
        }
        return map;
    }

    private Map<Long, DoctorGroupEventTime> groupEventTimeMap(Long farmId) {

        List<DoctorGroup> doctorGroupList = RespHelper.or500(doctorGroupReadService.findGroupsByFarmId(farmId));
        Map<Long, DoctorGroupEventTime> map = Maps.newHashMap();
        DoctorGroupEventTime doctorGroupEventTime;
        for (DoctorGroup doctorGroup : doctorGroupList){
            doctorGroupEventTime = new DoctorGroupEventTime();
            doctorGroupEventTime.setCloseAt(doctorGroup.getCloseAt());
            doctorGroupEventTime.setOpenAt(doctorGroup.getOpenAt());
            map.put(doctorGroup.getId(), doctorGroupEventTime);
        }
        return map;
    }

    private String materialInWareHousesList(List<DoctorMaterialInWareHouse> doctorMaterialInWareHouseList,Long materialId, Long wareHouseId) {
        for (DoctorMaterialInWareHouse doctorMaterialInWareHouse : doctorMaterialInWareHouseList) {
            if (doctorMaterialInWareHouse.getMaterialId().equals(materialId) && doctorMaterialInWareHouse.getWareHouseId().equals(wareHouseId)){
                return doctorMaterialInWareHouse.getUnitName();
            }
        }
        return null;
    }

    private List<DoctorMaterialConsumeProvider> buildDoctorMaterialConsumeProvider(List<DoctorMaterialConsumeProvider> list) {
        List<DoctorMaterialConsumeProvider> listOverride = new ArrayList<>(500);
        DoctorMaterialConsumeProvider doctorMaterialConsumeProviderOverride;
        for (DoctorMaterialConsumeProvider aList : list) {
            if (aList.getExtra() != null && aList.getExtraMap().containsKey("consumePrice")) {
                List<Map<String, Object>> priceCompose = (ArrayList) aList.getExtraMap().get("consumePrice");
                for (Map<String, Object> eachPrice : priceCompose) {
                    doctorMaterialConsumeProviderOverride = new DoctorMaterialConsumeProvider();
                    Long providerIdfd = Long.valueOf(eachPrice.get("providerId").toString());
                    if (isNull(providerIdfd)) {
                        providerIdfd = -1L;
                    }
                    Long unitPrice = Long.valueOf(eachPrice.get("unitPrice").toString());
                    Double count = Double.valueOf(eachPrice.get("count").toString());
                    doctorMaterialConsumeProviderOverride.setMaterialName(aList.getMaterialName());
                    doctorMaterialConsumeProviderOverride.setUnitPrice(unitPrice);
                    doctorMaterialConsumeProviderOverride.setFarmId(aList.getFarmId());
                    doctorMaterialConsumeProviderOverride.setMaterialId(aList.getMaterialId());
                    doctorMaterialConsumeProviderOverride.setWareHouseId(aList.getWareHouseId());
                    doctorMaterialConsumeProviderOverride.setBarnId(aList.getBarnId());
                    doctorMaterialConsumeProviderOverride.setBarnName(aList.getBarnName());
                    doctorMaterialConsumeProviderOverride.setEventTime(aList.getEventTime());
                    doctorMaterialConsumeProviderOverride.setGroupCode(aList.getGroupCode());
                    doctorMaterialConsumeProviderOverride.setGroupId(aList.getGroupId());
                    doctorMaterialConsumeProviderOverride.setWareHouseName(aList.getWareHouseName());
                    doctorMaterialConsumeProviderOverride.setEventCount(count);
                    doctorMaterialConsumeProviderOverride.setType(aList.getType());
                    doctorMaterialConsumeProviderOverride.setProvider(providerIdfd);
                    doctorMaterialConsumeProviderOverride.setEventType(aList.getEventType());
                    listOverride.add(doctorMaterialConsumeProviderOverride);
                }
            } else {
                if (isNull(aList.getProviderFactoryId())) {
                    aList.setProvider(-1L);
                }
                listOverride.add(aList);
            }
        }
        return listOverride;
    }

}
