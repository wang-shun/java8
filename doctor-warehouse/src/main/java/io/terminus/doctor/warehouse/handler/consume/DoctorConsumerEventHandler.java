package io.terminus.doctor.warehouse.handler.consume;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialPriceInWareHouseDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.warehouse.handler.IHandler;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialPriceInWareHouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: 创建消耗事件信息内容
 */
@Component
public class DoctorConsumerEventHandler implements IHandler{

    private final DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao;
    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public DoctorConsumerEventHandler(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao,
                                      DoctorMaterialPriceInWareHouseDao doctorMaterialPriceInWareHouseDao){
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
        this.doctorMaterialPriceInWareHouseDao = doctorMaterialPriceInWareHouseDao;
    }

    @Override
    public Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) {
        return dto.getActionType().equals(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue());
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        Long consumeCount = dto.getCount(); // 本次领用总数量
        // 1. 计算本次领用的组成(单价\数量\入库时间)
        long plus = 0L;
        Map<String, Object> extraMap = new HashMap<>();
        extraMap.put("consumePrice", new ArrayList<>());
        List<DoctorMaterialPriceInWareHouse> list = doctorMaterialPriceInWareHouseDao.findByWareHouseAndMaterialId(dto.getWareHouseId(), dto.getMaterialTypeId());
        for (DoctorMaterialPriceInWareHouse item : list) {
            Long remainder = item.getRemainder();
            if(plus + remainder <= consumeCount){
                plus += remainder;
                doctorMaterialPriceInWareHouseDao.delete(item.getId());
                ((ArrayList) extraMap.get("consumePrice")).add(ImmutableMap.of(
                        "providerId", item.getProviderId(),
                        "providerTime", item.getProviderTime(),
                        "unitPrice", item.getUnitPrice(), // 这次入库的物料的单价
                        "count", remainder // 从这次入库的物料当中领了多少
                ));
                if(plus + remainder == consumeCount){
                    break;
                }
            }else{
                item.setRemainder(remainder - (consumeCount - plus));
                doctorMaterialPriceInWareHouseDao.update(item);
                ((ArrayList) extraMap.get("consumePrice")).add(ImmutableMap.of(
                        "providerId", item.getProviderId(),
                        "providerTime", item.getProviderTime(),
                        "unitPrice", item.getUnitPrice(),
                        "count", consumeCount - plus
                ));
                break;
            }
        }

        // 2. 保存数据
        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider = DoctorMaterialConsumeProvider.buildFromDto(dto);
        if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
            extraMap.putAll(ImmutableMap.of(
                    "consumeDays", dto.getConsumeDays(),
                    "barnId", dto.getBarnId(),
                    "barnName", dto.getBarnName()));
        }
        doctorMaterialConsumeProvider.setExtraMap(extraMap);
        doctorMaterialConsumeProviderDao.create(doctorMaterialConsumeProvider);
        context.put("eventId",doctorMaterialConsumeProvider.getId());
    }
}
