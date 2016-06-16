package io.terminus.doctor.schedule.msg.producer.factory;

import com.google.api.client.util.Maps;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeAvgDto;
import io.terminus.doctor.warehouse.enums.WareHouseType;

import java.util.Map;

/**
 * Desc: 物料消耗信息统计方式Dto消息创建
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/12
 */
public class MaterialDtoFactory {

    private static MaterialDtoFactory materialDtoFactory = new MaterialDtoFactory();

    private MaterialDtoFactory() {}

    public static MaterialDtoFactory getInstance() {
        return materialDtoFactory;
    }

    public Map<String, Object> createMaterialMessage(DoctorMaterialConsumeAvgDto dto, String url) {
        // 创建消息
        Map<String, Object> jsonDate = Maps.newHashMap();
        jsonDate.put("farmId", dto.getFarmId());
        jsonDate.put("farmName", dto.getFarmName());
        jsonDate.put("wareHouseId", dto.getWareHouseId());
        jsonDate.put("wareHouseName", dto.getWareHouseName());
        jsonDate.put("managerId", dto.getManagerId());
        jsonDate.put("managerName", dto.getManagerName());
        jsonDate.put("materialId", dto.getMaterialId());
        jsonDate.put("materialName", dto.getMaterialName());
        jsonDate.put("type", dto.getType());
        WareHouseType type = WareHouseType.from(dto.getType());
        jsonDate.put("typeName", type == null ? null : type.getDesc());
        jsonDate.put("lotConsumeDay", dto.getLotConsumeDay());
        jsonDate.put("lotNumber", dto.getLotNumber());
        jsonDate.put("url", url);
        return jsonDate;
    }
}
