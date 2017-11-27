package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseItemOrgDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseItemOrg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-11-02 22:15:38
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseItemOrgReadServiceImpl implements DoctorWarehouseItemOrgReadService {

    @Autowired
    private DoctorWarehouseItemOrgDao doctorWarehouseItemOrgDao;
    @Autowired
    private DoctorBasicMaterialDao doctorBasicMaterialDao;

    @Override
    public Response<DoctorWarehouseItemOrg> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseItemOrgDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse item org by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.item.org.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseItemOrg>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseItemOrgDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse item org by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.item.org.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseItemOrg>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseItemOrgDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse item org, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.item.org.list.fail");
        }
    }

    @Override
    @ExceptionHandle("doctor.warehouse.item.org.list.fail")
    public Response<List<DoctorBasicMaterial>> findByOrgId(Long orgId) {
        return Response.ok(doctorBasicMaterialDao.findByIds(doctorWarehouseItemOrgDao.findByOrg(orgId).stream().map(DoctorWarehouseItemOrg::getItemId).collect(Collectors.toList())));
    }

    @Override
    @ExceptionHandle("doctor.warehouse.item.org.list.fail")
    public Response<List<DoctorBasicMaterial>> suggest(Integer type, Long orgId, String name) {

        List<Long> itemIds = doctorWarehouseItemOrgDao.findByOrg(orgId).stream().map(DoctorWarehouseItemOrg::getItemId).collect(Collectors.toList());
        if (itemIds.isEmpty())
            return Response.ok(Collections.emptyList());

        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("ids", itemIds);
        if (StringUtils.isNotBlank(name))
            params.put("name", name);

        return Response.ok(doctorBasicMaterialDao.list(params));
    }
}
