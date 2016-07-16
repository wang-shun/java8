package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
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

    @Autowired
    public DoctorBasicMaterialReadServiceImpl(DoctorBasicMaterialDao doctorBasicMaterialDao) {
        this.doctorBasicMaterialDao = doctorBasicMaterialDao;
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
    public Response<List<DoctorBasicMaterial>> finaBasicMaterialByTypeFilterBySrm(Integer type, String srm) {
        try {
            List<DoctorBasicMaterial> basicMaterials = doctorBasicMaterialDao.findByType(type);
            if (notEmpty(srm)) {
                return Response.ok(basicMaterials);
            }
            return Response.ok(basicMaterials.stream()
                    .filter(basic -> notEmpty(basic.getSrm()) && basic.getSrm().toLowerCase().contains(srm.toLowerCase()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("find basicMaterial filter by srm failed, type:{}, srm:{}, cause:{}", type, srm, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.find.fail");
        }
    }

}
