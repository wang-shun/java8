package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseSkuDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-13 13:14:30
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseSkuReadServiceImpl implements DoctorWarehouseSkuReadService {

    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;

    @Override
    public Response<List<DoctorWarehouseSku>> findWarehouseSkuByOrgAndName(Long orgId, String name) {
        return Response.ok(doctorWarehouseSkuDao.findWarehouseSkuByOrgAndName(orgId,name));
    }

    @Override
    public Response<DoctorWarehouseSku> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseSkuDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse sku by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.sku.find.fail");
        }
    }


    @Override
    public Response<List<DoctorWarehouseSku>> findByIds(List<Long> ids) {
        try {
            return Response.ok(doctorWarehouseSkuDao.findByIds(ids));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse sku by id:{}, cause:{}", StringUtils.join(ids,','), Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.sku.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseSku>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseSkuDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse sku by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.sku.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseSku>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseSkuDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse sku, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.sku.list.fail");
        }
    }

}
