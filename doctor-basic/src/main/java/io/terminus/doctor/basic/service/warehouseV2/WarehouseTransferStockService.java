package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockTransferDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.manager.WarehouseTransferManager;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2018/4/21.
 */
@Component
public class WarehouseTransferStockService
        extends AbstractWarehouseStockService<WarehouseStockTransferDto, WarehouseStockTransferDto.WarehouseStockTransferDetail> {

    @Autowired
    private WarehouseTransferManager warehouseTransferManager;

    @Override
    protected WarehouseMaterialHandleType getMaterialHandleType() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<WarehouseStockTransferDto.WarehouseStockTransferDetail> getDetails(WarehouseStockTransferDto stockDto) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void create(WarehouseStockTransferDto stockDto, WarehouseStockTransferDto.WarehouseStockTransferDetail detail, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void beforeUpdate(WarehouseStockTransferDto stockDto, DoctorWarehouseStockHandle stockHandle) {
        if (stockHandle.getHandleSubType().equals(WarehouseMaterialHandleType.TRANSFER_IN.getValue()))
            throw new ServiceException("transfer.in.not.allow.edit");
    }

    @Override
    protected DoctorWarehouseStockHandle create(WarehouseStockTransferDto stockDto, DoctorWareHouse wareHouse) {

        //调出单据
        DoctorWarehouseStockHandle transferOutStockHandle = doctorWarehouseStockHandleManager.create(stockDto, wareHouse, WarehouseMaterialHandleType.TRANSFER_OUT);

        //按照调入仓库分组，生成多个调入单据
        Map<Long/*warehouseId*/, List<WarehouseStockTransferDto.WarehouseStockTransferDetail>> eachWarehouseTransferIn = stockDto.getDetails()
                .stream()
                .collect(Collectors.groupingBy(WarehouseStockTransferDto.WarehouseStockTransferDetail::getTransferInWarehouseId));

        eachWarehouseTransferIn.forEach((w, details) -> {
            //调入仓库
            DoctorWareHouse transferInWarehouse = doctorWareHouseDao.findById(w);
            //调入单据
            DoctorWarehouseStockHandle transferInStockHandle = doctorWarehouseStockHandleManager.create(stockDto, transferInWarehouse, WarehouseMaterialHandleType.TRANSFER_IN);

            details.forEach(detail -> {
                warehouseTransferManager.create(detail, stockDto, transferOutStockHandle, wareHouse);
                doctorWarehouseStockManager.out(detail.getMaterialId(), detail.getQuantity(), wareHouse);

                warehouseTransferManager.create(detail, stockDto, transferInStockHandle, transferInWarehouse);
                doctorWarehouseStockManager.in(detail.getMaterialId(), detail.getQuantity(), transferInWarehouse);
            });
        });

        return transferOutStockHandle;
    }

    @Override
    protected void delete(DoctorWarehouseMaterialHandle materialHandle) {

        DoctorWarehouseMaterialHandle transferIn = doctorWarehouseMaterialHandleDao.findById(materialHandle.getRelMaterialHandleId());
        warehouseTransferManager.delete(transferIn);
        warehouseTransferManager.delete(materialHandle);

        DoctorWareHouse transferOutWareHouse = new DoctorWareHouse();
        transferOutWareHouse.setId(materialHandle.getWarehouseId());
        transferOutWareHouse.setWareHouseName(materialHandle.getWarehouseName());
        doctorWarehouseStockManager.in(materialHandle.getMaterialId(), materialHandle.getQuantity(), transferOutWareHouse);

        DoctorWareHouse transferInWareHouse = new DoctorWareHouse();
        transferInWareHouse.setId(transferIn.getWarehouseId());
        transferInWareHouse.setWareHouseName(transferIn.getWarehouseName());
        doctorWarehouseStockManager.out(transferIn.getMaterialId(), transferIn.getQuantity(), transferInWareHouse);
    }

    @Override
    protected void changed(DoctorWarehouseMaterialHandle materialHandle, WarehouseStockTransferDto.WarehouseStockTransferDetail detail, DoctorWarehouseStockHandle stockHandle, WarehouseStockTransferDto stockDto, DoctorWareHouse wareHouse) {

    }
}
