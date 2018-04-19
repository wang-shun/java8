package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorMaterialCodeDao;
import io.terminus.doctor.basic.dao.DoctorMaterialVendorDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseSkuDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockOutDto;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteServiceImpl;
import io.terminus.doctor.common.exception.InvalidException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Created by sunbo@terminus.io on 2017/9/12.
 */
@Component
public class DoctorWarehouseStockManager {

    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;
    @Autowired
    private DoctorMaterialCodeDao doctorMaterialCodeDao;
    @Autowired
    private DoctorMaterialVendorDao doctorMaterialVendorDao;

    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;


    public DoctorWarehouseStock in(WarehouseStockInDto inDto, WarehouseStockInDto.WarehouseStockInDetailDto detailDto, DoctorWarehouseStockWriteServiceImpl.StockContext context, DoctorWarehouseSku sku) {
        //find stock
        DoctorWarehouseStock stock = getStock(inDto.getWarehouseId(), detailDto.getMaterialId()).orElseGet(() -> {
            return DoctorWarehouseStock.builder()
                    .farmId(context.getWareHouse().getFarmId())
                    .warehouseId(context.getWareHouse().getId())
                    .warehouseName(context.getWareHouse().getWareHouseName())
                    .warehouseType(context.getWareHouse().getType())
                    .skuId(sku.getId())
                    .skuName(sku.getName())
                    .quantity(detailDto.getQuantity())
                    .build();
        });

        if (null != stock.getId()) {
            stock.setQuantity(stock.getQuantity().add(detailDto.getQuantity()));
            doctorWarehouseStockDao.update(stock);
        } else
            doctorWarehouseStockDao.create(stock);

        return stock;
    }

    public void in(WarehouseStockInDto.WarehouseStockInDetailDto inDetail, DoctorWareHouse wareHouse) {
        //find stock
        DoctorWarehouseStock stock = getStock(wareHouse.getId(), inDetail.getMaterialId()).orElseGet(() -> {

            DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(inDetail.getMaterialId());

            return DoctorWarehouseStock.builder()
                    .farmId(wareHouse.getFarmId())
                    .warehouseId(wareHouse.getId())
                    .warehouseName(wareHouse.getWareHouseName())
                    .warehouseType(wareHouse.getType())
                    .skuId(sku.getId())
                    .skuName(sku.getName())
                    .quantity(inDetail.getQuantity())
                    .build();
        });

        if (null != stock.getId()) {
            stock.setQuantity(stock.getQuantity().add(inDetail.getQuantity()));
            doctorWarehouseStockDao.update(stock);
        } else
            doctorWarehouseStockDao.create(stock);
    }

    //    @Transactional(propagation = Propagation.NESTED)
    public DoctorWarehouseStock out(WarehouseStockOutDto outDto, WarehouseStockOutDto.WarehouseStockOutDetail detailDto, DoctorWarehouseStockWriteServiceImpl.StockContext context, DoctorWarehouseSku sku, DoctorBasic unit) {
        DoctorWarehouseStock stock = getStock(outDto.getWarehouseId(), detailDto.getMaterialId()).orElseThrow(() ->
                new InvalidException("stock.not.found", context.getWareHouse().getWareHouseName(), detailDto.getMaterialId()));

        if (stock.getQuantity().compareTo(detailDto.getQuantity()) < 0)
            throw new InvalidException("stock.not.enough", stock.getWarehouseName(), stock.getSkuName(), stock.getQuantity(), unit.getName());

        stock.setQuantity(stock.getQuantity().subtract(detailDto.getQuantity()));
        doctorWarehouseStockDao.update(stock);

        return stock;
    }


    public Optional<DoctorWarehouseStock> getStock(Long warehouseId, Long materialId) {
        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(DoctorWarehouseStock.builder()
                .warehouseId(warehouseId)
                .skuId(materialId)
                .build());
        if (null == stocks || stocks.isEmpty())
            return Optional.empty();
        else
            return Optional.of(stocks.get(0));
    }

}
