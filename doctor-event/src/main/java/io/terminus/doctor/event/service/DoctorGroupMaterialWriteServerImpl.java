package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupMaterialDao;
import io.terminus.doctor.event.model.DoctorMasterialDatailsGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by terminus on 2017/4/18.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorGroupMaterialWriteServerImpl implements DoctorGroupMaterialWriteServer{

    @RpcConsumer
    private DoctorGroupMaterialDao doctorGroupMaterialDao;

    @Override
    public Response<Boolean> insterDoctorGroupMaterial(List<DoctorMasterialDatailsGroup> doctorMasterialDatailsGroups) {
        try {
            doctorGroupMaterialDao.insterDoctorGroupMaterials(doctorMasterialDatailsGroups);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("inster DoctorGroupMaterials fail, caues:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("inster DoctorGroupMaterials fai");
        }
    }

    @Override
    public Response<Boolean> deleteDoctorGroupMaterial() {
        try {
            doctorGroupMaterialDao.deleteDoctorGroupMaterials();
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("delete DoctorGroupMaterials fail, caues:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("delete DoctorGroupMaterials fai");
        }
    }
}
