package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseVendorDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseVendorOrgDao;
import io.terminus.doctor.basic.enums.WarehouseVendorDeleteFlag;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendor;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseVendorOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-26 15:57:14
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseVendorReadServiceImpl implements DoctorWarehouseVendorReadService {

    @Autowired
    private DoctorWarehouseVendorDao doctorWarehouseVendorDao;
    @Autowired
    private DoctorWarehouseVendorOrgDao doctorWarehouseVendorOrgDao;

    @Override
//    @Cacheable(value = "cache_warehouse_vendor")
    public Response<DoctorWarehouseVendor> findById(Long id) {
        try {

            DoctorWarehouseVendor vendor = doctorWarehouseVendorDao.findById(id);
            if (null == vendor)
                return Response.ok(null);
            if (vendor.getDeleteFlag().intValue() == WarehouseVendorDeleteFlag.DELETE.getValue())
                return Response.ok(null);

            return Response.ok(vendor);
        } catch (Exception e) {
            log.error("failed to find doctor warehouse vendor by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.find.fail");
        }
    }

    @Override
    @ExceptionHandle("doctor.warehouse.vendor.fail.fail")
    public Response<String> findNameById(Long id) {
        DoctorWarehouseVendor vendor = doctorWarehouseVendorDao.findById(id);
        if (null == vendor)
            return Response.ok("");

        return Response.ok(vendor.getName());
    }

    @Override
    public Response<Paging<DoctorWarehouseVendor>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            criteria.put("deleteFlag", WarehouseVendorDeleteFlag.NORMAL.getValue());
            return Response.ok(doctorWarehouseVendorDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse vendor by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseVendor>> list(Map<String, Object> criteria) {
        try {
            criteria.put("deleteFlag", WarehouseVendorDeleteFlag.NORMAL.getValue());
            return Response.ok(doctorWarehouseVendorDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse vendor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseVendor>> findByOrg(Long orgId) {
        try {
            return Response.ok(doctorWarehouseVendorDao.findByIds(
                    doctorWarehouseVendorOrgDao.findByOrg(orgId)
                            .stream()
                            .map(DoctorWarehouseVendorOrg::getVendorId)
                            .collect(Collectors.toList()))
                    .stream()
                    .filter(v -> v.getDeleteFlag().intValue() == WarehouseVendorDeleteFlag.NORMAL.getValue())
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse vendor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.vendor.list.fail");
        }
    }
}
