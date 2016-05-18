package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.dto.DoctorMaterialProductRatioDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-18
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Slf4j
@Component
public class DoctorMaterialInfoReadServiceImpl implements DoctorMaterialInfoReadService{

    private final DoctorMaterialInfoDao doctorMaterialInfoDao;

    @Autowired
    public DoctorMaterialInfoReadServiceImpl(DoctorMaterialInfoDao doctorMaterialInfoDao){
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
    }

    @Override
    public Response<List<DoctorMaterialInfo>> queryMaterialInfos(Long farmId) {
        try{
            return Response.ok(doctorMaterialInfoDao.findByFarmId(farmId));
        }catch (Exception e){
            log.error("query material infos error , cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.materialInfos.fail");
        }
    }

    @Override
    public Response<Paging<DoctorMaterialInfo>> pagingMaterialInfos(String farmId, Integer type, Integer pageNo, Integer pageSize) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorMaterialInfoDao.paging(pageInfo.getOffset(),pageInfo.getLimit(), ImmutableMap.of("farmId",farmId, "type",type)));
        }catch (Exception e){
            log.error("paging material infos fail ,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.materialInfo.fail");
        }
    }

    @Override
    public Response<Boolean> createMaterialProductRatioInfo(DoctorMaterialProductRatioDto doctorMaterialProductRatioDto) {
        try{
            DoctorMaterialInfo doctorMaterialInfo = doctorMaterialInfoDao.findById(doctorMaterialProductRatioDto.getMaterialId());
            checkState(!isNull(doctorMaterialInfo),"doctor.materialRatio.error");
            doctorMaterialInfo.setExtraMap(ImmutableMap.of("produceRatio",doctorMaterialProductRatioDto.getRatio()));
            doctorMaterialInfoDao.update(doctorMaterialInfo);
            return Response.ok(Boolean.TRUE);
        }catch (Exception e){
            log.error("create material product ratio fail,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("create.materialProduct.fail");
        }
    }
}
