package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseUnitOrgDao;
import io.terminus.doctor.basic.dto.DoctorWareHouseBasicDto;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseUnitOrg;

import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseUnitOrgReadService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-30 16:08:20
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseUnitOrgReadServiceImpl implements DoctorWarehouseUnitOrgReadService {

    @Autowired
    private DoctorWarehouseUnitOrgDao doctorWarehouseUnitOrgDao;
    @Autowired
    private DoctorBasicDao doctorBasicDao;

    @Override
    public Response<DoctorWarehouseUnitOrg> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseUnitOrgDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse unit org by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.unit.org.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseUnitOrg>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseUnitOrgDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse unit org by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.unit.org.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseUnitOrg>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseUnitOrgDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse unit org, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.unit.org.list.fail");
        }
    }

    @Override
    @ExceptionHandle("basic.find.fail")
    public Response<List<DoctorBasic>> list() {
        return Response.ok(doctorBasicDao.findValidByType(DoctorBasic.Type.UNIT.getValue()));
    }

    @Override
    public Response<List<DoctorBasic>> findByOrgId(Long orgId) {
        return Response.ok(doctorBasicDao.findByIds(
                doctorWarehouseUnitOrgDao.findByOrg(orgId)
                        .stream()
                        .map(DoctorWarehouseUnitOrg::getUnitId)
                        .collect(Collectors.toList()))
                .stream()
                .filter(u -> u.getIsValid().intValue() == 1)
                .collect(Collectors.toList())
        );
    }
}
