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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    @Deprecated
    public DoctorWarehouseStockHandle handle(AbstractWarehouseStockDto stockDto, DoctorWareHouse wareHouse, WarehouseMaterialHandleType handleType) {

        if (stockDto.getStockHandleId() != null) {

            DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getStockHandleId());

            stockHandle.setHandleDate(stockDto.getHandleDate().getTime());

            stockHandle.setOperatorId(stockDto.getOperatorId());
            stockHandle.setOperatorName(stockDto.getOperatorName());
            doctorWarehouseStockHandleDao.update(stockHandle);

            return stockHandle;
        } else
            return create(stockDto, wareHouse, handleType, null);
    }

    public void update(AbstractWarehouseStockDto stockDto, DoctorWarehouseStockHandle stockHandle) {

        stockHandle.setHandleDate(stockDto.getHandleDate().getTime());
        stockHandle.setSettlementDate(stockDto.getSettlementDate());

        stockHandle.setOperatorId(stockDto.getOperatorId());
        stockHandle.setOperatorName(stockDto.getOperatorName());
        doctorWarehouseStockHandleDao.update(stockHandle);
    }


    public DoctorWarehouseStockHandle create(AbstractWarehouseStockDto stockDto,
                                             DoctorWareHouse wareHouse,
                                             WarehouseMaterialHandleType handleType,
                                             Long relStockHandleId) {
        String serialNo;
        if (handleType == WarehouseMaterialHandleType.IN)
            serialNo = "R" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.OUT)
            serialNo = "C" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.INVENTORY_PROFIT)
            serialNo = "PY" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.INVENTORY_DEFICIT)
            serialNo = "PK" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.TRANSFER_IN)
            serialNo = "DR" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.TRANSFER_OUT)
            serialNo = "DC" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.FORMULA_IN)
            serialNo = "PR" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else if (handleType == WarehouseMaterialHandleType.FORMULA_OUT)
            serialNo = "PC" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");
        else
            serialNo = "T" + DateFormatUtils.format(new Date(), "yyyyMMddhhmmssSSS");

        DoctorWarehouseStockHandle handle = new DoctorWarehouseStockHandle();
        handle.setFarmId(wareHouse.getFarmId());
        handle.setWarehouseId(wareHouse.getId());
        handle.setWarehouseType(wareHouse.getType());
        handle.setWarehouseName(wareHouse.getWareHouseName());

        handle.setRelStockHandleId(relStockHandleId);

        handle.setOperatorId(stockDto.getOperatorId());
        handle.setOperatorName(stockDto.getOperatorName());

        handle.setHandleDate(stockDto.getHandleDate().getTime());
        handle.setSettlementDate(stockDto.getSettlementDate());
        handle.setSerialNo(serialNo);

        if (WarehouseMaterialHandleType.isBigIn(handleType.getValue()))
            handle.setHandleType(1);//入库
        else
            handle.setHandleType(2);//出库
        handle.setHandleSubType(handleType.getValue());

        doctorWarehouseStockHandleDao.create(handle);
        return handle;
    }

    public DoctorWarehouseStockHandle create(Long operatorId,
                                             String operatorName,
                                             Date handleDate,
                                             DoctorWareHouse wareHouse,
                                             WarehouseMaterialHandleType handleType,
                                             Long relStockHandleId) {
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
        handle.setFarmId(wareHouse.getFarmId());
        handle.setWarehouseId(wareHouse.getId());
        handle.setWarehouseType(wareHouse.getType());
        handle.setWarehouseName(wareHouse.getWareHouseName());

        handle.setRelStockHandleId(relStockHandleId);

        handle.setOperatorId(operatorId);
        handle.setOperatorName(operatorName);

        handle.setHandleDate(handleDate);
        handle.setSerialNo(serialNo);

        if (WarehouseMaterialHandleType.isBigIn(handleType.getValue()))
            handle.setHandleType(1);//入库
        else
            handle.setHandleType(2);//出库
        handle.setHandleSubType(handleType.getValue());

        doctorWarehouseStockHandleDao.create(handle);
        return handle;
    }

    @Deprecated
    public <T extends AbstractWarehouseStockDetail> List<T> clean(AbstractWarehouseStockDto stockDto, List<T> stockDetails, DoctorWareHouse wareHouse) {
        doctorWarehouseMaterialHandleDao
                .findByStockHandle(stockDto.getStockHandleId())
                .forEach(mh -> {
                    doctorWarehouseMaterialHandleManager.delete(mh);
                });

        return stockDetails;
    }

    @Deprecated
    public <T extends AbstractWarehouseStockDetail> List<T> clean(AbstractWarehouseStockDto stockDto, List<T> stockDetails, DoctorWareHouse wareHouse, MaterialHandleComparator<T> keyComparator) {

        List<DoctorWarehouseMaterialHandle> oldSkuHandle = doctorWarehouseMaterialHandleDao
                .findByStockHandle(stockDto.getStockHandleId());

        Map<Long/*id*/, List<DoctorWarehouseMaterialHandle>> oldSkuHandleMap = oldSkuHandle
                .stream()
                .collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getId));

        //过滤一下，不是所有的都更改了
        List<T> needUpdateDetails = new ArrayList<>();

        stockDetails.stream().forEach(d -> {
            if (d.getMaterialHandleId() != null) { //update
                if (!oldSkuHandleMap.containsKey(d.getMaterialHandleId())) {
                    needUpdateDetails.add(d);
                } else {
                    DoctorWarehouseMaterialHandle oldHandle = oldSkuHandleMap.get(d.getMaterialHandleId()).get(0);
                    if (keyComparator.same(d, oldHandle)) {

                        if (keyComparator.notImportDifferentProcess(d, oldHandle))
                            doctorWarehouseMaterialHandleDao.update(oldHandle);
                    } else {
                        needUpdateDetails.add(d);
                        doctorWarehouseMaterialHandleManager.delete(oldHandle);
                    }
                }
            } else
                needUpdateDetails.add(d);
        });

        //删除的
        for (DoctorWarehouseMaterialHandle materialHandle : oldSkuHandle) {
            boolean include = false;
            for (T detail : stockDetails) {
                if (materialHandle.getId().equals(detail.getMaterialHandleId())) {
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


    public interface MaterialHandleComparator<T extends AbstractWarehouseStockDetail> {

        boolean same(T source, DoctorWarehouseMaterialHandle target);


        boolean notImportDifferentProcess(T source, DoctorWarehouseMaterialHandle target);

    }
}
