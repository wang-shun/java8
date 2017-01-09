package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.MapBuilder;
import io.terminus.common.utils.Params;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.dao.DoctorFarmBasicDao;
import io.terminus.doctor.basic.dto.DoctorBasicMaterialSearchDto;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 基础物料表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */
@Slf4j
@Service
@RpcProvider
public class DoctorBasicMaterialReadServiceImpl implements DoctorBasicMaterialReadService {

    private final DoctorBasicMaterialDao doctorBasicMaterialDao;

    private final DoctorFarmBasicDao doctorFarmBasicDao;

    @Autowired
    public DoctorBasicMaterialReadServiceImpl(DoctorBasicMaterialDao doctorBasicMaterialDao,
                                              DoctorFarmBasicDao doctorFarmBasicDao) {
        this.doctorBasicMaterialDao = doctorBasicMaterialDao;
        this.doctorFarmBasicDao = doctorFarmBasicDao;
    }

    @Override
    public Response<DoctorBasicMaterial> findBasicMaterialById(Long basicMaterialId) {
        try {
            return Response.ok(doctorBasicMaterialDao.findById(basicMaterialId));
        } catch (Exception e) {
            log.error("find basicMaterial by id failed, basicMaterialId:{}, cause:{}", basicMaterialId, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorBasicMaterial>> pagingBasicMaterialByTypeFilterBySrm(DoctorBasicMaterialSearchDto basicMaterial) {
        try {
            PageInfo page = PageInfo.of(basicMaterial.getPageNo(), basicMaterial.getSize());
            return Response.ok(doctorBasicMaterialDao.paging(page.getOffset(), page.getLimit(), Params.filterNullOrEmpty(BeanMapper.convertObjectToMap(basicMaterial))));
        } catch (Exception e) {
            log.error("find basicMaterial filter by srm failed, basicMaterial:{}, cause:{}", basicMaterial, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBasicMaterial>> findBasicMaterialByTypeFilterBySrm(Integer type, String srm, String exIds) {
        try {
            List<DoctorBasicMaterial> basicMaterials = doctorBasicMaterialDao.findByType(type);
            if (notEmpty(srm)) {
                basicMaterials = basicMaterials.stream()
                        .filter(basic -> notEmpty(basic.getSrm()) && basic.getSrm().toLowerCase().contains(srm.toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (notEmpty(exIds)) {
                List<Long> exMaterialIds = Splitters.splitToLong(exIds, Splitters.COMMA);
                basicMaterials = basicMaterials.stream()
                        .filter(material -> !exMaterialIds.contains(material.getId()))
                        .collect(Collectors.toList());
            }
            return Response.ok(basicMaterials);
        } catch (Exception e) {
            log.error("find basicMaterial filter by srm failed, type:{}, srm:{}, exIds:{}, cause:{}", type, srm, exIds, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBasicMaterial>> findAllBasicMaterials() {
        try{
            List<DoctorBasicMaterial> basicMaterials = doctorBasicMaterialDao.list( MapBuilder.<String, Integer>of().put("isValid", 1).map());
            return Response.ok(basicMaterials);
        }catch (Exception e){
            log.error("find basicMaterial all failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBasicMaterial>> findBasicMaterialsOwned(Long farmId, Long type, String srm) {
        try{
            DoctorFarmBasic doctorFarmBasic = doctorFarmBasicDao.findByFarmId(farmId);
            List idList = doctorFarmBasic.getMaterialIdList();
            List<DoctorBasicMaterial> doctorBasicMaterialList = doctorBasicMaterialDao.findByIdsAndType(type, idList);
            if (notEmpty(srm)) {
                doctorBasicMaterialList = doctorBasicMaterialList.stream()
                        .filter(basic -> notEmpty(basic.getSrm()) && basic.getSrm().toLowerCase().contains(srm.toLowerCase()))
                        .collect(Collectors.toList());
            }
            return Response.ok(doctorBasicMaterialList);
        }catch (Exception e){
            log.error("find basicMaterials failed, farmId:{}, type:{}, cause:{}", farmId, type, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.find.fail");
        }
    }
}
