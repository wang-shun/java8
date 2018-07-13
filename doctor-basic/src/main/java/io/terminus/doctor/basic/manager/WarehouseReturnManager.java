package io.terminus.doctor.basic.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockRefundDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Override
    public DoctorWarehouseMaterialHandle create(WarehouseStockRefundDto.WarehouseStockRefundDetailDto detail,
                                                WarehouseStockRefundDto stockDto,
                                                DoctorWarehouseStockHandle stockHandle,
                                                DoctorWareHouse wareHouse) {

        throw new UnsupportedOperationException();
    }

    @Override
    public void create(List<WarehouseStockRefundDto.WarehouseStockRefundDetailDto> details, WarehouseStockRefundDto stockDto, DoctorWarehouseStockHandle stockHandle, DoctorWareHouse wareHouse) {
        //出库单据
        DoctorWarehouseStockHandle outStockHandle = doctorWarehouseStockHandleDao.findById(stockDto.getOutStockHandleId());
        if (null == outStockHandle)
            throw new InvalidException("warehouse.stock.handle.not.found", stockDto.getOutStockHandleId());

        //获取出库明细，并按照物料分组
        Map<Long/*materialId*/, List<DoctorWarehouseMaterialHandle>> outMaterialHandleMap = doctorWarehouseMaterialHandleDao
                .findByStockHandle(outStockHandle.getId())
                .stream()
                .collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));


        details.forEach(d -> {
            if (!outMaterialHandleMap.containsKey(d.getMaterialId()))
                throw new InvalidException("material.not.allow.refund", d.getMaterialId());

            if (d.getApplyBarnId() == null && d.getApplyGroupId() == null)
                throw new ServiceException("apply.barn.or.apply.group.not.null");

            //根据领用的猪群或猪舍找到一条出库明细。一笔出库单据中同个物料领用到同个猪群或猪群只允许出现一条出库明细
            DoctorWarehouseMaterialHandle outMaterialHandle = doctorWarehouseMaterialHandleDao.findByApply(outStockHandle.getId(), d.getApplyGroupId(), d.getApplyBarnId());
            if (outMaterialHandle == null)
                throw new ServiceException("material.handle.not.found");

//            DoctorWarehouseMaterialHandle outMaterialHandle = outMaterialHandleMap.get(d.getMaterialId()).get(0);
            if (outMaterialHandle.getQuantity().compareTo(d.getQuantity().multiply(BigDecimal.valueOf(-1))) < 0)
                throw new InvalidException("quantity.not.enough.to.refund", outMaterialHandle.getQuantity());

            //已退数量
            BigDecimal alreadyRefundQuantity = doctorWarehouseMaterialHandleDao.countQuantityAlreadyRefund(outMaterialHandle.getId());
//            BigDecimal alreadyRefundQuantity = doctorWarehouseMaterialHandleDao.findRetreatingById(outMaterialHandle.getId(), null, stockHandleId);
            //计算可退数量
            if (outMaterialHandle.getQuantity().add(alreadyRefundQuantity).subtract(d.getFormerQuantity()).compareTo(d.getQuantity().multiply(BigDecimal.valueOf(-1))) < 0)
                throw new InvalidException("quantity.not.enough.to.refund", outMaterialHandle.getQuantity().add(alreadyRefundQuantity).subtract(d.getFormerQuantity()));

            DoctorWarehouseMaterialHandle materialHandle = buildMaterialHandle(d, stockDto, stockHandle, wareHouse);
            materialHandle.setType(WarehouseMaterialHandleType.RETURN.getValue());
            materialHandle.setRelMaterialHandleId(outMaterialHandle.getId());

            //入库类型，当天第一笔
            if (!DateUtil.inSameDate(stockDto.getHandleDate().getTime(), new Date())) {

                buildNewHandleDateForUpdate(materialHandle, stockDto.getHandleDate());

                if (materialHandle.getHandleDate().before(outMaterialHandle.getHandleDate()) &&
                        !materialHandle.getHandleDate().equals(outMaterialHandle.getHandleDate())) {
                    throw new ServiceException("refund.date.before.out.date");
                }

                //获取该笔明细之前的库存量，包括该事件日期
                BigDecimal historyQuantity = getHistoryQuantityInclude(materialHandle.getHandleDate(), wareHouse.getId(), d.getMaterialId());

                materialHandle.setBeforeStockQuantity(historyQuantity);

                historyQuantity = historyQuantity.add(d.getQuantity());

                //该笔单据明细之后单据明细需要重算
                recalculate(materialHandle.getHandleDate(), false, wareHouse.getId(), d.getMaterialId(), historyQuantity);
            } else {
                BigDecimal currentQuantity = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(d.getMaterialId(), wareHouse.getId())
                        .orElse(DoctorWarehouseStock.builder().quantity(new BigDecimal(0)).build())
                        .getQuantity();
                materialHandle.setBeforeStockQuantity(currentQuantity);
            }
            doctorWarehouseMaterialHandleDao.create(materialHandle);
            //领用记录中增加退料数量
            doctorWarehouseMaterialApplyDao.findAllByMaterialHandle(outMaterialHandle.getId()).forEach(apply -> {
                //退料数量记录为负数
                if (apply.getRefundQuantity() == null)
                    apply.setRefundQuantity(new BigDecimal(0));
                apply.setRefundQuantity(apply.getRefundQuantity().add(d.getQuantity()));
                doctorWarehouseMaterialApplyDao.update(apply);
            });

        });

    }

    @Override
    public void delete(DoctorWarehouseMaterialHandle materialHandle) {

        materialHandle.setDeleteFlag(WarehouseMaterialHandleDeleteFlag.DELETE.getValue());
        doctorWarehouseMaterialHandleDao.update(materialHandle);

//        if (!DateUtil.inSameDate(materialHandle.getHandleDate(), new Date())) {
        //删除历史单据明细
        recalculate(materialHandle);
//        }

        //领用记录中减少退料数量
        doctorWarehouseMaterialApplyDao.findAllByMaterialHandle(materialHandle.getId()).forEach(apply -> {
            //退料数量记录为负数
            if (apply.getRefundQuantity() == null)
                apply.setRefundQuantity(new BigDecimal(0));
            apply.setRefundQuantity(apply.getRefundQuantity().add(materialHandle.getQuantity()));
            doctorWarehouseMaterialApplyDao.update(apply);
        });

    }


}
