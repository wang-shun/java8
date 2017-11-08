package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockHandleDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
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
    @Autowired
    private DoctorWarehouseMaterialHandleManager doctorWarehouseMaterialHandleManager;

    //    @Transactional(propagation = Propagation.NESTED)

    /**
     * 处理
     *
     * @param stockDto
     * @param wareHouse
     * @param handleType
     * @return
     */
    public DoctorWarehouseStockHandle handle(AbstractWarehouseStockDto stockDto, DoctorWareHouse wareHouse, WarehouseMaterialHandleType handleType) {

        if (stockDto.getStockHandleId() != null) {

            DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getStockHandleId());

            stockHandle.setHandleDate(stockDto.getHandleDate().getTime());

            stockHandle.setOperatorId(stockDto.getOperatorId());
            stockHandle.setOperatorName(stockDto.getOperatorName());
            doctorWarehouseStockHandleDao.update(stockHandle);

            return stockHandle;
        }

        String serialNo;
        if (handleType == WarehouseMaterialHandleType.IN)
            serialNo = "R" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.OUT)
            serialNo = "C" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.INVENTORY)
            serialNo = "P" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else
            serialNo = "D" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");

        DoctorWarehouseStockHandle handle = new DoctorWarehouseStockHandle();
        handle.setFarmId(stockDto.getFarmId());
        handle.setWarehouseId(stockDto.getWarehouseId());
        handle.setWarehouseType(wareHouse.getType());
        handle.setWarehouseName(wareHouse.getWareHouseName());

        handle.setOperatorId(stockDto.getOperatorId());
        handle.setOperatorName(stockDto.getOperatorName());

        handle.setHandleDate(stockDto.getHandleDate().getTime());
        handle.setSerialNo(serialNo);

        handle.setHandleType(handleType.getValue());
        handle.setHandleSubType(handleType.getValue());

        doctorWarehouseStockHandleDao.create(handle);
        return handle;
    }

    public <T extends AbstractWarehouseStockDetail> List<T> clean(AbstractWarehouseStockDto stockDto, List<T> stockDetails, DoctorWareHouse wareHouse, MaterialHandleComparator<T> keyComparator) {

        List<DoctorWarehouseMaterialHandle> oldSkuHandle = doctorWarehouseMaterialHandleDao
                .findByStockHandle(stockDto.getStockHandleId());

        Map<Long/*skuID*/, List<DoctorWarehouseMaterialHandle>> oldSkuHandleMap = oldSkuHandle
                .stream()
                .collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));

        //过滤一下，不是所有的都更改了
        List<T> needUpdateDetails = new ArrayList<>();
        stockDetails.stream().forEach(d -> {
            if (oldSkuHandleMap.containsKey(d.getMaterialId())) {
                List<DoctorWarehouseMaterialHandle> oldMaterialHandles = oldSkuHandleMap.get(d.getMaterialId());
                if (!oldMaterialHandles.isEmpty()) {
                    log.debug("find same sku handle record:{},start compare", d.getMaterialId());
//                    BigDecimal oldQuantity;
//                    if (oldMaterialHandles.get(0).getType().equals(WarehouseMaterialHandleType.INVENTORY_PROFIT.getValue()))
//                        oldQuantity = oldMaterialHandles.get(0).getBeforeInventoryQuantity().add(oldMaterialHandles.get(0).getQuantity());
//                    else if (oldMaterialHandles.get(0).getType().equals(WarehouseMaterialHandleType.INVENTORY_DEFICIT.getValue()))
//                        oldQuantity = oldMaterialHandles.get(0).getBeforeInventoryQuantity().subtract(oldMaterialHandles.get(0).getQuantity());
//                    else oldQuantity = oldMaterialHandles.get(0).getQuantity();

                    if (keyComparator.same(d, oldMaterialHandles.get(0))) {
                        if (!Objects.equals(oldMaterialHandles.get(0).getRemark(), d.getRemark())) {
                            oldMaterialHandles.get(0).setRemark(d.getRemark());
                            doctorWarehouseMaterialHandleDao.update(oldMaterialHandles.get(0));
                        } else
                            log.debug("new sku handle match old same quantity[{}] and remark,do not need update", d.getQuantity());
                    }

//                    if (oldMaterialHandles.get(0).getType().equals(WarehouseMaterialHandleType.IN.getValue())) {
//                        if (oldQuantity.compareTo(d.getQuantity()) == 0 && oldMaterialHandles.get(0).getUnitPrice().equals())
//                    }

//                    if (oldQuantity.compareTo(d.getQuantity()) == 0) {
//                        if (!Objects.equals(oldMaterialHandles.get(0).getRemark(), d.getRemark())) {
//                            oldMaterialHandles.get(0).setRemark(d.getRemark());
//                            doctorWarehouseMaterialHandleDao.update(oldMaterialHandles.get(0));
//                        } else
//                            log.debug("new sku handle match old same quantity[{}] and remark,do not need update", oldQuantity);
//                    }
                    else {
                        doctorWarehouseMaterialHandleManager.delete(oldMaterialHandles.get(0));
                        needUpdateDetails.add(d);
                    }
                }
            } else
                //新增的
                needUpdateDetails.add(d);
        });

        //删除的
        for (DoctorWarehouseMaterialHandle materialHandle : oldSkuHandle) {
            boolean include = false;
            for (T detail : stockDetails) {
                if (detail.getMaterialId().equals(materialHandle.getMaterialId())) {
                    include = true;
                    break;
                }
            }
            if (!include) {
                doctorWarehouseMaterialHandleManager.delete(materialHandle);
            }
        }

        //盘点不需要检查库存
//        needUpdateDetails.stream().forEach(d -> {
//            DoctorWarehouseStock stock = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(d.getMaterialId(), wareHouse.getId())
//                    .orElseThrow(() -> new InvalidException(""));
//
//        });

        return needUpdateDetails;
    }


    public void delete(DoctorWarehouseStockHandle stockHandle, AbstractWarehouseStockDto stockDto) {

        if (null != stockDto.getStockHandleId()) {
//            doctorWarehouseStockHandleDao.delete(oldStockHandleId);

            stockHandle.setOperatorId(stockDto.getOperatorId());
            stockHandle.setOperatorName(stockDto.getOperatorName());

            stockHandle.setHandleDate(stockDto.getHandleDate().getTime());

            doctorWarehouseStockHandleDao.update(stockHandle);

            doctorWarehouseMaterialHandleDao.findByStockHandle(stockDto.getStockHandleId()).stream().forEach(m -> {
                doctorWarehouseMaterialHandleManager.delete(m);
            });
        }
    }


    public interface MaterialHandleComparator<T> {

        boolean same(T source, DoctorWarehouseMaterialHandle target);
    }
}
