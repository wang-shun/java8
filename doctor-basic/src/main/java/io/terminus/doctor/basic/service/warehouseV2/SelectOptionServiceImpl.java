package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.enums.HandleType;
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
        List<Integer> values = PigType.ALL_TYPES;
        List<String> descs = PigType.ALL_TYPES_DESC;
        for(int i = 0;i < values.size();i++) {
            Map<String, String> sels = Maps.newHashMap();
            sels.put("value",values.get(i).toString());
            sels.put("name", descs.get(i));
            lists.add(sels);
        }
        return lists;
    }

    @Override
    public List<Map<String, Object>> getPigBarnNameOption(Long farmId,Integer pigType) {
        return doctorWarehouseMaterialHandleDao.getPigBarnNameOption(farmId,pigType);
    }

    @Override
    public List<Map<String, Object>> getPigGroupNameOption(Long farmId,Long barnId) {
        return doctorWarehouseMaterialHandleDao.getPigGroupNameOption(farmId,barnId);
    }

    @Override
    public List<Map<String,String>> getHandlerTypeOption() {
        List<Map<String,String>> lists = Lists.newArrayList();
        List<Integer> values = HandleType.ALL_TYPES;
        List<String> descs = HandleType.ALL_TYPES_DESC;
        for(int i = 0;i < values.size();i++) {
            Map<String, String> sels = Maps.newHashMap();
            sels.put("value",values.get(i).toString());
            sels.put("name", descs.get(i));
            lists.add(sels);
        }
        return lists;
    }

    @Override
    public List<Map<String,String>> getSkuTypeOption() {
        List<Map<String,String>> lists = Lists.newArrayList();
        List<Integer> values = WareHouseType.ALL_TYPES;
        List<String> descs = WareHouseType.ALL_TYPES_DESC;
        for(int i = 0;i < values.size();i++) {
            Map<String, String> sels = Maps.newHashMap();
            sels.put("value",values.get(i).toString());
            sels.put("name", descs.get(i));
            lists.add(sels);
        }
        return lists;
    }

    @Override
    public List<Map<String, Object>> getWareHouseDataOption(Long farmId) {
        return doctorWarehouseMaterialHandleDao.getWareHouseDataOption(farmId);
    }

}
