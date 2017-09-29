package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorWarehouseStockMonthlyDao;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockMonthly;

import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-09-29 13:22:37
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseStockMonthlyReadServiceImpl implements DoctorWarehouseStockMonthlyReadService {

    @Autowired
    private DoctorWarehouseStockMonthlyDao doctorWarehouseStockMonthlyDao;

    @Override
    public Response<DoctorWarehouseStockMonthly> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseStockMonthlyDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse stock monthly by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.monthly.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseStockMonthly>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseStockMonthlyDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse stock monthly by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.monthly.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseStockMonthly>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseStockMonthlyDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse stock monthly, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.monthly.list.fail");
        }
    }

    @Override
    public Response<DoctorWarehouseStockMonthly> findByWarehouseAndMaterial(Long warehouseId, Long materialId) {
        try {
            List<DoctorWarehouseStockMonthly> stockMonthlies = doctorWarehouseStockMonthlyDao.list(DoctorWarehouseStockMonthly.builder()
                    .warehouseId(warehouseId)
                    .materialId(materialId)
                    .build());
            if (stockMonthlies.isEmpty())
                return Response.ok(null);

            return Response.ok(stockMonthlies.get(0));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse stock monthly, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.monthly.find.fail");
        }
    }


    @Override
    public Response<AmountAndQuantityDto> countWarehouseBalance(Long warehouseId, int handleYear, int handleMonth) {

        try {
            List<DoctorWarehouseStockMonthly> stockMonthlies = doctorWarehouseStockMonthlyDao.list(DoctorWarehouseStockMonthly.builder()
                    .warehouseId(warehouseId)
                    .handleYear(handleYear)
                    .handleMonth(handleMonth)
                    .build());
            if (stockMonthlies.isEmpty())
                return Response.ok(new AmountAndQuantityDto(0, new BigDecimal(0)));

            long totalAmount = 0;
            BigDecimal totalQuantity = new BigDecimal(0);
            for (DoctorWarehouseStockMonthly stockMonthly : stockMonthlies) {
                totalAmount += stockMonthly.getBalacneAmount();
                totalQuantity = totalQuantity.add(stockMonthly.getBalanceQuantity());
            }
            return Response.ok(new AmountAndQuantityDto(totalAmount, totalQuantity));
        } catch (Exception e) {
            log.error("failed to count doctor warehouse stock monthly, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.monthly.count.fail");
        }

    }
}
