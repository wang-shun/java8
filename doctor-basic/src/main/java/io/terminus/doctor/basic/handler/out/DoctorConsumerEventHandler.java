package io.terminus.doctor.basic.handler.out;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.basic.dao.DoctorMaterialPriceInWareHouseDao;
import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.basic.dto.EventHandlerContext;
import io.terminus.doctor.basic.handler.IHandler;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.basic.model.DoctorMaterialPriceInWareHouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
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
    public boolean ifHandle(DoctorMaterialConsumeProvider.EVENT_TYPE eventType) {
        return eventType != null && eventType.isOut();
    }

    @Override
    public void handle(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context) throws RuntimeException {
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
        if(Objects.equals(dto.getType(), WareHouseType.FEED.getKey()) && dto.getConsumeDays() != null){
            extraMap.put("consumeDays", dto.getConsumeDays());
        }
        doctorMaterialConsumeProvider.setBarnId(dto.getBarnId());
        doctorMaterialConsumeProvider.setBarnName(dto.getBarnName());
        doctorMaterialConsumeProvider.setGroupId(dto.getGroupId());
        doctorMaterialConsumeProvider.setGroupCode(dto.getGroupCode());
        doctorMaterialConsumeProvider.setExtraMap(extraMap);
        doctorMaterialConsumeProvider.setUnitPrice(Double.valueOf(totalPrice / consumeCount).longValue());
        doctorMaterialConsumeProviderDao.create(doctorMaterialConsumeProvider);
        context.setEventId(doctorMaterialConsumeProvider.getId());
    }

    @Override
    public void rollback(DoctorMaterialConsumeProvider cp) {
        // 本次出库事件的价格组成
        List<Map<String, Object>> priceCompose = (ArrayList) cp.getExtraMap().get("consumePrice");
        if(priceCompose == null || priceCompose.isEmpty()){
            throw new ServiceException("price.compose.not.found"); // 没有找到本次出库的价格组成
        }
        for(Map<String, Object> eachPrice : priceCompose){
            Long providerId = Long.valueOf(eachPrice.get("providerId").toString());
            Date providerTime = new Date(Long.valueOf(eachPrice.get("providerTime").toString()));
            Long unitPrice = Long.valueOf(eachPrice.get("unitPrice").toString());
            Double count = Double.valueOf(eachPrice.get("count").toString());
            DoctorMaterialPriceInWareHouse priceInWareHouse = doctorMaterialPriceInWareHouseDao.findByProviderId(providerId);
            if(priceInWareHouse == null){
                doctorMaterialPriceInWareHouseDao.create(
                        DoctorMaterialPriceInWareHouse.builder()
                                .farmId(cp.getFarmId()).farmName(cp.getFarmName())
                                .wareHouseId(cp.getWareHouseId()).wareHouseName(cp.getWareHouseName())
                                .materialId(cp.getMaterialId()).materialName(cp.getMaterialName())
                                .type(cp.getType()).providerId(providerId).providerTime(providerTime)
                                .unitPrice(unitPrice).remainder(count)
                                .build()
                );
            }else{
                priceInWareHouse.setRemainder(priceInWareHouse.getRemainder() + count);
                doctorMaterialPriceInWareHouseDao.update(priceInWareHouse);
            }
        }
        doctorMaterialConsumeProviderDao.delete(cp.getId());
    }
}
