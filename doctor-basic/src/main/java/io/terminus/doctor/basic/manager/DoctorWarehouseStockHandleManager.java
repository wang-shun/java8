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


    public <T extends AbstractWarehouseStockDetail> List<T> clean(AbstractWarehouseStockDto stockDto, List<T> stockDetails, DoctorWareHouse wareHouse) {
        doctorWarehouseMaterialHandleDao
                .findByStockHandle(stockDto.getStockHandleId())
                .forEach(mh -> {
                    doctorWarehouseMaterialHandleManager.delete(mh);
                });

        return stockDetails;
    }

    @Deprecated//太复杂了算了
    public <T extends AbstractWarehouseStockDetail> List<T> clean(AbstractWarehouseStockDto stockDto, List<T> stockDetails, DoctorWareHouse wareHouse, MaterialHandleComparator<T> keyComparator) {

        List<DoctorWarehouseMaterialHandle> oldSkuHandle = doctorWarehouseMaterialHandleDao
                .findByStockHandle(stockDto.getStockHandleId());

        Map<Long/*skuID*/, List<DoctorWarehouseMaterialHandle>> oldSkuHandleMap = oldSkuHandle
                .stream()
                .collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));

        //过滤一下，不是所有的都更改了
        List<T> needUpdateDetails = new ArrayList<>();
        Map<Long/*skuID*/, List<T>> detailMaps = stockDetails.stream().collect(Collectors.groupingBy(AbstractWarehouseStockDetail::getMaterialId));
        Map<Long/**skuID*/, List<T>> coldBench = new HashMap<>();

        for (Long skuId : detailMaps.keySet()) {
            if (!oldSkuHandleMap.containsKey(skuId))
                needUpdateDetails.addAll(detailMaps.get(skuId));
            else {
                List<DoctorWarehouseMaterialHandle> oldMaterialHandles = oldSkuHandleMap.get(skuId);
                if (!oldMaterialHandles.isEmpty()) {
                    log.debug("find same sku handle record:{},start compare", skuId);
                    //有可能有多条
                    for (T detail : detailMaps.get(skuId)) {
                        for (DoctorWarehouseMaterialHandle oldHandle : oldMaterialHandles) {//id=1,3,3
                            if (keyComparator.same(detail, oldHandle)) {

                                if (!coldBench.containsKey(skuId)) {
                                    //TODO 如果remark相同就有问题

                                    List<T> details = new ArrayList<>();
                                    details.add(detail);
                                    coldBench.put(skuId, details);
                                } else
                                    coldBench.get(skuId).add(detail);
                            } else {
                                needUpdateDetails.add(detail);

                                doctorWarehouseMaterialHandleManager.delete(oldHandle);
                                needUpdateDetails.addAll(coldBench.get(skuId));//冷板凳上的需要重新上场
                                coldBench.remove(skuId);
                            }
                        }
                    }

                    coldBench.forEach((id, d) -> {
                        needUpdateDetails.add(d.get(0));
                    });
                }
            }
        }
        //id=1,3,4  o  id=1,3,4    o    id=1,3,4    o   id=1,3,4    o
        //id=2,3,4  o  id=2,3,4    o    id=2,3,4    o   id=1,2,5    o
        //                                              id=2,3,5    o

        //id=1,3,4  x  id=3,3,4    o    id=1,3,5    o   id=2,3,5    x
        //id=2,3,4  x  id=3,3,5    o    id=2,3,4    x   id=1,4,4    o
        //id=1,2,4  o  id=2,3,4    x    id=1,4,4    o

//        stockDetails.stream().forEach(d -> {
//            if (oldSkuHandleMap.containsKey(d.getMaterialId())) {
//                List<DoctorWarehouseMaterialHandle> oldMaterialHandles = oldSkuHandleMap.get(d.getMaterialId());
//                if (!oldMaterialHandles.isEmpty()) {
//                    log.debug("find same sku handle record:{},start compare", d.getMaterialId());
//                    if (keyComparator.same(d, oldMaterialHandles.get(0))) {
//                        if (!Objects.equals(oldMaterialHandles.get(0).getRemark(), d.getRemark())) {
//                            oldMaterialHandles.get(0).setRemark(d.getRemark());
//                            doctorWarehouseMaterialHandleDao.update(oldMaterialHandles.get(0));
//                        } else
//                            log.debug("new sku handle match old same quantity[{}] and remark,do not need update", d.getQuantity());
//                    } else {
//                        doctorWarehouseMaterialHandleManager.delete(oldMaterialHandles.get(0));
//                        needUpdateDetails.add(d);
//                    }
//                }
//            } else
//                //新增的
//                needUpdateDetails.add(d);
//        });

        //删除的
        for (DoctorWarehouseMaterialHandle materialHandle : oldSkuHandle) {
            boolean include = false;
            for (T detail : stockDetails) {
                if (detail.getMaterialId().equals(materialHandle.getMaterialId())) {
                    include = true;
                    break;
                }
            }
            if (!include) { //如果单据中原有的明细未包含在请求参数中，表明这条明细已被删除
                doctorWarehouseMaterialHandleManager.delete(materialHandle);
            }
        }

        return needUpdateDetails;
    }


    @Deprecated
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


    @Deprecated
    public interface MaterialHandleComparator<T> {

        boolean same(T source, DoctorWarehouseMaterialHandle target);
    }
}
