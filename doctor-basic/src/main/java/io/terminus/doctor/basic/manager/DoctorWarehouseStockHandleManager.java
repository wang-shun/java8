package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.exception.InvalidException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2017/9/12.
 */
@Slf4j
@Component
public class DoctorWarehouseStockHandleManager {
    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;
    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;

    //    @Transactional(propagation = Propagation.NESTED)
    public void handle(AbstractWarehouseStockDto stockDto, DoctorWareHouse wareHouse, String serialNo, WarehouseMaterialHandleType handleType) {

        DoctorWarehouseStockHandle handle = new DoctorWarehouseStockHandle();
        handle.setFarmId(stockDto.getFarmId());
        handle.setWarehouseId(stockDto.getWarehouseId());
        handle.setWarehouseName(wareHouse.getWareHouseName());

        handle.setOperatorId(stockDto.getOperatorId());
        handle.setOperatorName(stockDto.getOperatorName());

        handle.setHandleDate(stockDto.getHandleDate().getTime());
        handle.setSerialNo(serialNo);

        handle.setHandleType(handleType.getValue());
        handle.setHandleSubType(handleType.getValue());

        doctorWarehouseStockHandleDao.create(handle);
    }


    public void update(AbstractWarehouseStockDto stockDto, List<? extends AbstractWarehouseStockDetail> details, WarehouseMaterialHandleType handleType) {
        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getStockHandleId());
        if (null == stockHandle)
            throw new InvalidException("warehouse.stock.handle.not.found", stockDto.getStockHandleId());

        List<DoctorWarehouseMaterialHandle> materialHandles = doctorWarehouseMaterialHandleDao.list(DoctorWarehouseMaterialHandle.builder()
                .stockHandleId(stockDto.getStockHandleId())
                .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
                .build());

        //先check一遍
        if (handleType.getValue() == WarehouseMaterialHandleType.IN.getValue()
                || handleType.getValue() == WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue()) {

            Map<Long, List<DoctorWarehouseMaterialHandle>> handleMap = materialHandles.stream().collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));

            for (Long skuId : handleMap.keySet()) {
                BigDecimal needOutOfStockQuantity = new BigDecimal(0);
                for (DoctorWarehouseMaterialHandle handle : handleMap.get(skuId)) {  //同一笔单据中有可能存在同一个sku的操作
                    needOutOfStockQuantity.add(handle.getQuantity());
                }

                log.debug("reverse {},need out of stock:{}", skuId, needOutOfStockQuantity);
                for (AbstractWarehouseStockDetail detail : details) {
                    if (detail.getMaterialId().equals(skuId)) {
                        needOutOfStockQuantity = needOutOfStockQuantity.subtract(detail.getQuantity());
                    }
                }
                Optional<DoctorWarehouseStock> stock = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(skuId, stockDto.getWarehouseId());
                if (stock.isPresent()) {
                    needOutOfStockQuantity.subtract(stock.get().getQuantity());
                }
                if (needOutOfStockQuantity.compareTo(new BigDecimal(0)) > 0)
                    throw new ServiceException("stock.not.enough");
            }
        } else if (handleType.getValue() == WarehouseMaterialHandleType.OUT.getValue()
                || handleType.getValue() == WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue()) {

        }


    }
}
