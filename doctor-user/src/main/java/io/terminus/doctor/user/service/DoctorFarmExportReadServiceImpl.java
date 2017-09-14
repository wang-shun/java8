package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorFarmExportDao;
import io.terminus.doctor.user.model.DoctorFarmExport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xjn on 17/9/6.
 */
@Slf4j
@RpcProvider
@Service
public class DoctorFarmExportReadServiceImpl implements DoctorFarmExportReadService{
    @Autowired
    private DoctorFarmExportDao doctorFarmExportDao;
    @Override
    public Response<List<DoctorFarmExport>> findFarmExportRecord(String farmName) {
        try {
            List<DoctorFarmExport> list = doctorFarmExportDao.query(DoctorFarmExport.builder().farmName(farmName).build())
                    .stream().map(doctorFarmExport -> {
                        doctorFarmExport.setStatusName(DoctorFarmExport.Status
                                .from(doctorFarmExport.getStatus()).getDesc());
                        return doctorFarmExport;
                    }).collect(Collectors.toList());
            return Response.ok(list);
        } catch (Exception e) {
            log.error("find farm export record failed, farmName:{}, cause:{}"
                    , farmName, Throwables.getStackTraceAsString(e));
            return Response.fail("find.farm.export.record.failed");
        }
    }
}
