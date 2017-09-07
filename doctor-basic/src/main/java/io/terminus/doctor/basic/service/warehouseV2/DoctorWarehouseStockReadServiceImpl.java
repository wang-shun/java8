package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStock;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseStockReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-18 09:41:24
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseStockReadServiceImpl implements DoctorWarehouseStockReadService {

    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;

    @Override
    public Response<DoctorWarehouseStock> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseStockDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouseV2 stock by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseStock>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseStockDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouseV2 stock by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.paging.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseStock>> paging(Integer pageNo, Integer pageSize, DoctorWarehouseStock criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseStockDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouseV2 stock by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.paging.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseStock>> pagingMergeVendor(Integer pageNo, Integer pageSize, DoctorWarehouseStock criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseStockDao.pagingMergeVendor(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouseV2 stock by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.paging.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseStock>> pagingMergeVendor(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseStockDao.pagingMergeVendor(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouseV2 stock by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseStock>> list(DoctorWarehouseStock criteria) {
        try {
            return Response.ok(doctorWarehouseStockDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouseV2 stock, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseStock>> listMergeVendor(DoctorWarehouseStock criteria) {
        try {
            return Response.ok(doctorWarehouseStockDao.listMergeVendor(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouseV2 stock, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseStock>> list(Long farmID, Long materialID) {
        try {

            DoctorWarehouseStock criteria = new DoctorWarehouseStock();
            criteria.setFarmId(farmID);
            criteria.setMaterialId(materialID);
            return Response.ok(doctorWarehouseStockDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouseV2 stock, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouseV2.stock.list.fail");
        }
    }

    @Override
    public Response<DoctorWarehouseStock> findOneByCriteria(DoctorWarehouseStock criteria) {

        List<DoctorWarehouseStock> stocks = doctorWarehouseStockDao.list(criteria);
        if (null == stocks || stocks.isEmpty())
            return Response.ok(null);

        return Response.ok(stocks.get(0));
    }

    @Override
    public Response<Boolean> existed(DoctorWarehouseStock criteria) {
        Response<Paging<DoctorWarehouseStock>> listResponse = this.paging(1, 1, criteria);
        if (!listResponse.isSuccess())
            return Response.fail(listResponse.getError());

        return Response.ok(!listResponse.getResult().isEmpty());
    }
}