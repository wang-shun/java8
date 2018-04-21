package io.terminus.doctor.basic.service.warehouseV2;

import java.util.List;
import java.util.Map;

public interface SelectOptionService {

    Map<Integer,String> getPigTypeOption();

    List<Map<String,Object>> getPigBarnNameOption(Long farmId);

    List<Map<String,Object>> getPigGroupNameOption(Long farmId);

    Map<Integer,String> getHandlerTypeOption();

    Map<Integer,String> getSkuTypeOption();

    List<Map<String,Object>> getWareHouseDataOption(Long farmId);

}
