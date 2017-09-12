package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorWarehousePurchaseDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockInDto;
import io.terminus.doctor.basic.enums.WarehousePurchaseHandleFlag;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehousePurchase;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockWriteService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/9/12.
 */
@Component
public class DoctorWarehousePurchaseManager {


    @Autowired
    private DoctorWarehousePurchaseDao doctorWarehousePurchaseDao;

    @Transactional
    public DoctorWarehousePurchase in(AbstractWarehouseStockDto stockDto, WarehouseStockInDto.WarehouseStockInDetailDto detail, DoctorWarehouseStock stock) {

        DoctorWarehousePurchase purchase = new DoctorWarehousePurchase();
        purchase.setFarmId(stock.getFarmId());
        purchase.setWarehouseId(stock.getWarehouseId());
        purchase.setWarehouseName(stock.getWarehouseName());
        purchase.setWarehouseType(stock.getWarehouseType());
        purchase.setMaterialId(detail.getMaterialId());
        if (StringUtils.isBlank(detail.getVendorName()))
            purchase.setVendorName(DoctorWarehouseStockWriteService.DEFAULT_VENDOR_NAME);
        else
            purchase.setVendorName(detail.getVendorName());
        purchase.setQuantity(detail.getQuantity());
        purchase.setHandleQuantity(new BigDecimal(0));
        purchase.setUnitPrice(detail.getUnitPrice());
        purchase.setHandleDate(stockDto.getHandleDate().getTime());
        purchase.setHandleYear(stockDto.getHandleDate().get(Calendar.YEAR));
        purchase.setHandleMonth(stockDto.getHandleDate().get(Calendar.MONTH) + 1);
        purchase.setHandleFinishFlag(WarehousePurchaseHandleFlag.NOT_OUT_FINISH.getValue());
        doctorWarehousePurchaseDao.create(purchase);
        return purchase;
    }
}
