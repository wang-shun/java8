package io.terminus.doctor.warehouse.handler.out;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.Params;
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
 * Descirbe: 创建出库事件信息内容
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
        DoctorMaterialConsumeProvider.EVENT_TYPE eventType = DoctorMaterialConsumeProvider.EVENT_TYPE.from(dto.getActionType());
        return eventType != null && eventType.isOut();
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, Map<String, Object> context) throws RuntimeException {
        Double consumeCount = dto.getCount(); // 本次领用总数量
        long totalPrice = 0L; // 本次领用总价格
        // 1. 计算本次领用的组成(单价\数量\入库时间)
        long plus = 0L;
        Map<String, Object> extraMap = new HashMap<>();
        extraMap.put("consumePrice", new ArrayList<>());
        List<DoctorMaterialPriceInWareHouse> list = doctorMaterialPriceInWareHouseDao.findByWareHouseAndMaterialId(dto.getWareHouseId(), dto.getMaterialTypeId());
        for (DoctorMaterialPriceInWareHouse item : list) {
            Double remainder = item.getRemainder();
            if(plus + remainder <= consumeCount){
                doctorMaterialPriceInWareHouseDao.delete(item.getId());
                ((ArrayList) extraMap.get("consumePrice")).add(ImmutableMap.of(
                        "providerId", item.getProviderId(),
                        "providerTime", item.getProviderTime(),
                        "unitPrice", item.getUnitPrice(), // 这次入库的物料的单价
                        "count", remainder // 从这次入库的物料当中领了多少
                ));
                totalPrice += item.getUnitPrice() * remainder;
                if(plus + remainder == consumeCount){
                    break;
                }
                plus += remainder;
            }else{
                item.setRemainder(remainder - (consumeCount - plus));
                doctorMaterialPriceInWareHouseDao.update(item);
                ((ArrayList) extraMap.get("consumePrice")).add(ImmutableMap.of(
                        "providerId", item.getProviderId(),
                        "providerTime", item.getProviderTime(),
                        "unitPrice", item.getUnitPrice(),
                        "count", consumeCount - plus
                ));
                totalPrice += (consumeCount - plus) * item.getUnitPrice();
                break;
            }
        }

        // 2. 保存数据
        DoctorMaterialConsumeProvider doctorMaterialConsumeProvider = DoctorMaterialConsumeProvider.buildFromDto(dto);
        if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey())){
            extraMap.putAll(Params.filterNullOrEmpty(MapBuilder.<String, Object>of().put(
                    "consumeDays", dto.getConsumeDays(),
                    "barnId", dto.getBarnId(),
                    "barnName", dto.getBarnName()).map()));
            doctorMaterialConsumeProvider.setBarnId(dto.getBarnId());
            doctorMaterialConsumeProvider.setBarnName(dto.getBarnName());
            doctorMaterialConsumeProvider.setGroupId(dto.getGroupId());
            doctorMaterialConsumeProvider.setGroupCode(dto.getGroupCode());
        }
        doctorMaterialConsumeProvider.setExtraMap(extraMap);
        doctorMaterialConsumeProvider.setUnitPrice(Double.valueOf(totalPrice / consumeCount).longValue());
        doctorMaterialConsumeProviderDao.create(doctorMaterialConsumeProvider);
        context.put("eventId",doctorMaterialConsumeProvider.getId());
    }
}
