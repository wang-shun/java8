package io.terminus.doctor.basic.service.warehouseV2;

import java.util.List;
import java.util.Map;

public interface SelectOptionService {

    List<Map<String,String>> getPigTypeOption();

    List<Map<String,Object>> getPigBarnNameOption(Long farmId,Integer pigType);

    List<Map<String,Object>> getPigGroupNameOption(Long farmId,Long barnId);

    List<Map<String,String>> getHandlerTypeOption();

    List<Map<String,String>> getSkuTypeOption();

    List<Map<String,Object>> getWareHouseDataOption(Long farmId);

}
