package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleSubType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.WareHouseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RpcProvider
public class SelectOptionServiceImpl implements SelectOptionService {

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Override
    public Map<Integer,String> getPigTypeOption() {
        Map<Integer, String> sels = Maps.newHashMap();
        sels.put(PigType.NURSERY_PIGLET.getValue(), PigType.NURSERY_PIGLET.getDesc());
        sels.put(PigType.FATTEN_PIG.getValue(), PigType.FATTEN_PIG.getDesc());
        sels.put(PigType.RESERVE.getValue(), PigType.RESERVE.getDesc());
        sels.put(PigType.MATE_SOW.getValue(), PigType.MATE_SOW.getDesc());
        sels.put(PigType.PREG_SOW.getValue(), PigType.PREG_SOW.getDesc());
        sels.put(PigType.DELIVER_SOW.getValue(), PigType.DELIVER_SOW.getDesc());
        sels.put(PigType.BOAR.getValue(), PigType.BOAR.getDesc());
        return sels;
    }

    @Override
    public List<Map<String, Object>> getPigBarnNameOption(Long farmId) {
        return doctorWarehouseMaterialHandleDao.getPigBarnNameOption(farmId);
    }

    @Override
    public List<Map<String, Object>> getPigGroupNameOption(Long farmId) {
        return doctorWarehouseMaterialHandleDao.getPigGroupNameOption(farmId);
    }

    @Override
    public Map<Integer, String> getHandlerTypeOption() {
        Map<Integer, String> sels = Maps.newHashMap();
        sels.put(WarehouseMaterialHandleSubType.CG_IN.getValue(), WarehouseMaterialHandleSubType.CG_IN.getDesc());
        sels.put(WarehouseMaterialHandleSubType.TL_IN.getValue(), WarehouseMaterialHandleSubType.TL_IN.getDesc());
        sels.put(WarehouseMaterialHandleSubType.PF_IN.getValue(), WarehouseMaterialHandleSubType.PF_IN.getDesc());
        sels.put(WarehouseMaterialHandleSubType.PY_IN.getValue(), WarehouseMaterialHandleSubType.PY_IN.getDesc());
        sels.put(WarehouseMaterialHandleSubType.DB_IN.getValue(), WarehouseMaterialHandleSubType.DB_IN.getDesc());
        sels.put(WarehouseMaterialHandleSubType.LL_OUT.getValue(), WarehouseMaterialHandleSubType.LL_OUT.getDesc());
        sels.put(WarehouseMaterialHandleSubType.PK_OUT.getValue(), WarehouseMaterialHandleSubType.PK_OUT.getDesc());
        sels.put(WarehouseMaterialHandleSubType.PF_OUT.getValue(), WarehouseMaterialHandleSubType.PF_OUT.getDesc());
        sels.put(WarehouseMaterialHandleSubType.DB_OUT.getValue(), WarehouseMaterialHandleSubType.DB_OUT.getDesc());
        return sels;
    }

    @Override
    public Map<Integer, String> getSkuTypeOption() {
        Map<Integer, String> sels = Maps.newHashMap();
        sels.put(WareHouseType.FEED.getKey(), WareHouseType.FEED.getDesc());
        sels.put(WareHouseType.MATERIAL.getKey(), WareHouseType.MATERIAL.getDesc());
        sels.put(WareHouseType.VACCINATION.getKey(), WareHouseType.VACCINATION.getDesc());
        sels.put(WareHouseType.MEDICINE.getKey(), WareHouseType.MEDICINE.getDesc());
        sels.put(WareHouseType.CONSUME.getKey(), WareHouseType.CONSUME.getDesc());
        return sels;
    }

    @Override
    public List<Map<String, Object>> getWareHouseDataOption(Long farmId) {
        return doctorWarehouseMaterialHandleDao.getWareHouseDataOption(farmId);
    }

}
