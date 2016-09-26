package io.terminus.doctor.warehouse.service;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-18
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Service
@RpcProvider
public class DoctorMaterialInfoReadServiceImpl implements DoctorMaterialInfoReadService{

    private final DoctorMaterialInfoDao doctorMaterialInfoDao;

    @Autowired
    public DoctorMaterialInfoReadServiceImpl(DoctorMaterialInfoDao doctorMaterialInfoDao){
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
    }


    @Override
    @Deprecated
    public Response<Paging<DoctorMaterialInfo>> pagingMaterialInfos(Long farmId, Integer type, Integer canProduce, String materialName, Integer pageNo, Integer pageSize) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            Map<String,Object> params = Maps.newHashMap();
            params.put("farmId",farmId);
            params.put("type", type);
            params.put("canProduce", canProduce);
            params.put("materialName", Strings.emptyToNull(materialName));
            return Response.ok(doctorMaterialInfoDao.paging(pageInfo.getOffset(),pageInfo.getLimit(), params));
        }catch (Exception e){
            log.error("paging material info fail ,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.materialInfo.fail");
        }
    }

    @Override
    @Deprecated
    public Response<DoctorMaterialInfo> queryById(@NotNull(message = "input.id.empty") Long id) {
        try{
            return Response.ok(doctorMaterialInfoDao.findById(id));
        }catch (Exception e){
            log.error("material query by id error, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.materialInfo.fail");
        }
    }
}
