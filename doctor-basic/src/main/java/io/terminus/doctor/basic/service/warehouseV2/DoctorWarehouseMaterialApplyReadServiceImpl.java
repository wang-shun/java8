package io.terminus.doctor.basic.service.warehouseV2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroup;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroupDetail;
import io.terminus.doctor.common.enums.WareHouseType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMaterialApplyReadServiceImpl implements DoctorWarehouseMaterialApplyReadService {

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Override
    public Response<DoctorWarehouseMaterialApply> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseMaterialApplyDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse material apply by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.find.fail");
        }
    }

    @Override
    @ExceptionHandle("doctor.warehouse.material.apply.find.fail")
    public Response<DoctorWarehouseMaterialApply> findByMaterialHandle(Long materialHandleId) {

        return Response.ok(doctorWarehouseMaterialApplyDao.findMaterialHandle(materialHandleId));
    }

    @Override
    @ExceptionHandle("doctor.warehouse.material.apply.find.fail")
    public Response<List<DoctorWarehouseMaterialApply>> findByFarmAndPigGroup(Long farmId, Long groupId) {
        List<DoctorWarehouseMaterialApply> applies = doctorWarehouseMaterialApplyDao.list(DoctorWarehouseMaterialApply.builder()
                .farmId(farmId)
                .pigGroupId(groupId)
                .build());
        return Response.ok(applies);
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialApply>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialApplyDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse material apply by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApply>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialApplyDao.advList(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApply>> list(DoctorWarehouseMaterialApply criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialApplyDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApply>> listOrderByHandleDate(DoctorWarehouseMaterialApply criteria, Integer limit) {
        try {
            return Response.ok(doctorWarehouseMaterialApplyDao.listAndOrderByHandleDate(criteria, limit));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.list.fail");
        }
    }

    @Override
    public Response<Map<Integer, DoctorWarehouseMaterialApply>> listEachWarehouseTypeLastApply(Long farmId) {

        Map<Integer, DoctorWarehouseMaterialApply> eachWarehouseTypeLastApply = new HashMap<>();
        Stream.of(WareHouseType.values()).mapToInt(WareHouseType::getKey).forEach(type -> {
            List<DoctorWarehouseMaterialApply> lastApply = doctorWarehouseMaterialApplyDao.listAndOrderByHandleDate(DoctorWarehouseMaterialApply.builder().build(), 1);
            if (null == lastApply || lastApply.isEmpty())
                eachWarehouseTypeLastApply.put(type, null);
            else
                eachWarehouseTypeLastApply.put(type, lastApply.get(0));
        });


        return Response.ok(eachWarehouseTypeLastApply);
    }

    @Override
    @ExceptionHandle("doctor.warehouse.material.apply.list.fail")
    public Response<List<DoctorWarehouseMaterialApply>> month(Long warehouseId, Integer applyYear, Integer applyMonth, String skuName) {

        return Response.ok(doctorWarehouseMaterialApplyDao.list(DoctorWarehouseMaterialApply.builder()
                .warehouseId(warehouseId)
                .materialName(StringUtils.isBlank(skuName) ? null : skuName)
                .applyYear(applyYear)
                .applyMonth(applyMonth)
                .build()));
    }

    @Override
    public Response<Map<String,Object>> selectPigGroupApply(Integer farmId, String pigType, String pigName, String pigGroupName,
                                                                                                Integer skuType, String skuName, Date openAt, Date closeAt){
        List<DoctorWarehouseMaterialApplyPigGroup> pigGroupList =doctorWarehouseMaterialApplyDao.selectPigGroupApply(farmId,pigType,pigName,pigGroupName,skuType,skuName,openAt,closeAt);
        Double allQuantity = 0.0;
        Double allAmount = 0.0;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        for(int i = 0;i<pigGroupList.size(); i++){
            if(pigGroupList.get(i).getQuantity() != null){
                allQuantity =pigGroupList.get(i).getQuantity() + allQuantity;
            }
            if(pigGroupList.get(i).getAmount() != null) {
                allAmount = pigGroupList.get(i).getAmount() + allAmount;
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("pigGroupList",pigGroupList);
        map.put("allQuantity",allQuantity);
        map.put("allAmount",allAmount);
        return Response.ok(map);
    }

    @Override
    public Response<List<Map>> piggeryReport(DoctorWarehouseMaterialApply criteria) {
        return Response.ok(doctorWarehouseMaterialApplyDao.piggeryReport(criteria));
    }

    @Override
    public Response<List<Map>> piggeryDetails(DoctorWarehouseMaterialApply criteria) {
        return Response.ok(doctorWarehouseMaterialApplyDao.piggeryDetails(criteria));
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApplyPigGroupDetail>> selectPigGroupApplyDetail(Long pigGroupId, Long materiaId) {
        return Response.ok(doctorWarehouseMaterialApplyDao.selectPigGroupApplyDetail(pigGroupId, materiaId));
    }

}
