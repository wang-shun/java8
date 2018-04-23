package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.collect.Lists;
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
    public List<Map<String,String>> getPigTypeOption() {
        List<Map<String,String>> lists = Lists.newArrayList();

        Map<String, String> sels = Maps.newHashMap();
        sels.put("value",PigType.NURSERY_PIGLET.getValue() + "");
        sels.put("name", PigType.NURSERY_PIGLET.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",PigType.FATTEN_PIG.getValue() + "");
        sels.put("name", PigType.FATTEN_PIG.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",PigType.RESERVE.getValue() + "");
        sels.put("name", PigType.RESERVE.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",PigType.MATE_SOW.getValue() + "");
        sels.put("name", PigType.MATE_SOW.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",PigType.PREG_SOW.getValue() + "");
        sels.put("name", PigType.PREG_SOW.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",PigType.DELIVER_SOW.getValue() + "");
        sels.put("name", PigType.DELIVER_SOW.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",PigType.BOAR.getValue() + "");
        sels.put("name", PigType.BOAR.getDesc());
        lists.add(sels);

        return lists;
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
    public List<Map<String,String>> getHandlerTypeOption() {
        List<Map<String,String>> lists = Lists.newArrayList();

        Map<String, String> sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.CG_IN.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.CG_IN.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.TL_IN.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.TL_IN.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.PF_IN.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.PF_IN.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.PY_IN.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.PY_IN.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.DB_IN.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.DB_IN.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.LL_OUT.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.LL_OUT.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.PK_OUT.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.PK_OUT.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.PF_OUT.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.PF_OUT.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WarehouseMaterialHandleSubType.DB_OUT.getValue() + "");
        sels.put("name", WarehouseMaterialHandleSubType.DB_OUT.getDesc());
        lists.add(sels);

        return lists;
    }

    @Override
    public List<Map<String,String>> getSkuTypeOption() {
        List<Map<String,String>> lists = Lists.newArrayList();

        Map<String, String> sels = Maps.newHashMap();
        sels.put("value",WareHouseType.FEED.getKey().toString());
        sels.put("name", WareHouseType.FEED.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WareHouseType.MATERIAL.getKey().toString());
        sels.put("name", WareHouseType.MATERIAL.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WareHouseType.VACCINATION.getKey().toString());
        sels.put("name", WareHouseType.VACCINATION.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WareHouseType.MEDICINE.getKey().toString());
        sels.put("name", WareHouseType.MEDICINE.getDesc());
        lists.add(sels);

        sels = Maps.newHashMap();
        sels.put("value",WareHouseType.CONSUME.getKey().toString());
        sels.put("name", WareHouseType.CONSUME.getDesc());
        lists.add(sels);

        return lists;
    }

    @Override
    public List<Map<String, Object>> getWareHouseDataOption(Long farmId) {
        return doctorWarehouseMaterialHandleDao.getWareHouseDataOption(farmId);
    }

}
