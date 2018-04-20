package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockRefundDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 退料入库
 * Created by sunbo@terminus.io on 2018/4/8.
 */
@Component
public class WarehouseReturnManager extends AbstractStockManager<WarehouseStockRefundDto.WarehouseStockRefundDetailDto, WarehouseStockRefundDto> {


    @Override
    public void create(WarehouseStockRefundDto.WarehouseStockRefundDetailDto detail,
                       WarehouseStockRefundDto stockDto,
                       DoctorWarehouseStockHandle stockHandle,
                       DoctorWareHouse wareHouse) {


    }

    @Override
    public void create(List<WarehouseStockRefundDto.WarehouseStockRefundDetailDto> details, WarehouseStockRefundDto stockDto, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {
        //出库单据
        DoctorWarehouseStockHandle outStockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getOutMaterialHandleId());
        if (null == outStockHandle)
            throw new ServiceException("stock.handle.not.found");
        int days = DateUtil.getDeltaDays(stockDto.getHandleDate().getTime(), outStockHandle.getHandleDate());
        if (days > 0)
            throw new ServiceException("refund.date.before.out.date");
        if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {
            //如果是历史单据，还不能是同一天，入库类型的单据会被放在这一天的00:00:00
            if (days == 0)
                throw new ServiceException("refund.date.before.out.date");
        }

        Map<Long, List<DoctorWarehouseMaterialHandle>> outMaterialHandleMap = doctorWarehouseMaterialHandleDao
                .findByStockHandle(outStockHandle.getId())
                .stream()
                .collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));


        details.forEach(d -> {
            if (!outMaterialHandleMap.containsKey(d.getMaterialId()))
                throw new ServiceException("material.not.allow.refund");

            DoctorWarehouseMaterialHandle outMaterialHandle = outMaterialHandleMap.get(d.getMaterialId()).get(0);
            if (outMaterialHandle.getQuantity().compareTo(d.getQuantity()) < 0)
                throw new ServiceException("out.quantity.not.allow.refund");

            //计算可退数量
            BigDecimal alreadyRefundQuantity = doctorWarehouseMaterialHandleDao.countQuantityAlreadyRefund(outMaterialHandle.getId());
            if (outMaterialHandle.getQuantity().subtract(alreadyRefundQuantity).compareTo(d.getQuantity()) < 0)
                throw new ServiceException("out.quantity.not.allow.refund");

            DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(d, stockDto, stockHandle, wareHouse);
            materialHandle.setType(WarehouseMaterialHandleType.RETURN.getValue());
            materialHandle.setRelMaterialHandleId(outMaterialHandle.getId());

            //入库类型，当天第一笔
            if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {

                materialHandle.setHandleDate(this.buildNewHandleDate(WarehouseMaterialHandleType.RETURN, stockDto.getHandleDate()));

                //获取该笔明细之前的库存量，包括该事件日期
                BigDecimal historyQuantity = getHistoryQuantityInclude(stockDto.getHandleDate().getTime(), wareHouse.getId(), d.getMaterialId());

                materialHandle.setBeforeStockQuantity(historyQuantity);

                historyQuantity = historyQuantity.subtract(d.getQuantity());

                //该笔单据明细之后单据明细需要重算
                recalculate(stockDto.getHandleDate().getTime(), false, wareHouse.getId(), d.getMaterialId(), historyQuantity);
            } else {
                BigDecimal currentQuantity = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(d.getMaterialId(), wareHouse.getId())
                        .orElse(DoctorWarehouseStock.builder().quantity(new BigDecimal(0)).build())
                        .getQuantity();
                materialHandle.setBeforeStockQuantity(currentQuantity);
            }
            doctorWarehouseMaterialHandleDao.create(materialHandle);
        });

    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {

        if (!DateUtil.inSameDate(materialHandle.getHandleDate(), new Date())) {
            //删除历史单据明细
            recalculate(materialHandle);
        }

        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(materialHandle);
    }
}
