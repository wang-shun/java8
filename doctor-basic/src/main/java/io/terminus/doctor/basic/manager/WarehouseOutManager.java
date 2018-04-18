package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dao.DoctorWarehouseSkuDao;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDetail;
import io.terminus.doctor.basic.dto.warehouseV2.AbstractWarehouseStockDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.apache.log4j.xml.SAXErrorHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * 领料出库
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseOutManager extends AbstractStockManager {


    @Override
    public void create(AbstractWarehouseStockDetail detail,
                       AbstractWarehouseStockDto stockDto,
                       DoctorWarehouseStockHandle stockHandle,
                       DoctorWareHouse wareHouse) {
        if (!DateUtil.inSameDate(stockHandle.getHandleDate(), new Date())) {
            //重算每个单据明细的beforeStockQuantity，并验证每个
            recalculate(stockHandle.getHandleDate(), wareHouse.getId(), detail.getQuantity().negate());
        }

        DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(detail.getMaterialId());
        if (null == sku) {
            throw new ServiceException("");
        }

        DoctorBasic unit = doctorBasicDao.findById(Long.parseLong(sku.getUnit()));
        if (null == unit)
            throw new ServiceException("");

        DoctorWarehouseMaterialHandle materialHandle = new DoctorWarehouseMaterialHandle();
        materialHandle.setStockHandleId(stockHandle.getId());
        materialHandle.setOrgId(stockDto.getOrgId());
        materialHandle.setFarmId(stockDto.getFarmId());
        materialHandle.setWarehouseId(stockDto.getWarehouseId());
        materialHandle.setWarehouseType(wareHouse.getType());
        materialHandle.setWarehouseName(wareHouse.getWareHouseName());
        materialHandle.setMaterialId(detail.getMaterialId());
        materialHandle.setMaterialName(sku.getName());
        materialHandle.setType(WarehouseMaterialHandleType.OUT.getValue());
        materialHandle.setUnit(unit.getName());
        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());
        materialHandle.setBeforeStockQuantity(getHistoryQuantity(stockHandle.getHandleDate(), wareHouse.getId()));
        materialHandle.setQuantity(detail.getQuantity());
        materialHandle.setHandleDate(stockHandle.getHandleDate());
        materialHandle.setHandleYear(stockDto.getHandleDate().get(Calendar.YEAR));
        materialHandle.setHandleMonth(stockDto.getHandleDate().get(Calendar.MONTH) + 1);
        materialHandle.setOperatorId(stockDto.getOperatorId());
        materialHandle.setOperatorName(stockDto.getOperatorName());
        materialHandle.setRemark(detail.getRemark());

        doctorWarehouseMaterialHandleDao.create(materialHandle);
    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle, Date handleDate) {

    }
}
