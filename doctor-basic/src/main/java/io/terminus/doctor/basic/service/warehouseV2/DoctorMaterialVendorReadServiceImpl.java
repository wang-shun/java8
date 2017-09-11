package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorMaterialVendorDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorMaterialVendor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-09-11 18:34:04
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMaterialVendorReadServiceImpl implements DoctorMaterialVendorReadService {

    @Autowired
    private DoctorMaterialVendorDao doctorMaterialVendorDao;

    @Override
    public Response<DoctorMaterialVendor> findById(Long id) {
        try{
            return Response.ok(doctorMaterialVendorDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor material vendor by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.material.vendor.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorMaterialVendor>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorMaterialVendorDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor material vendor by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.material.vendor.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorMaterialVendor>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorMaterialVendorDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor material vendor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.material.vendor.list.fail");
        }
    }

    @Override
    public Response<List<DoctorMaterialVendor>> list(DoctorMaterialVendor criteria) {
        try{
            return Response.ok(doctorMaterialVendorDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor material vendor, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.material.vendor.list.fail");
        }
    }
}
