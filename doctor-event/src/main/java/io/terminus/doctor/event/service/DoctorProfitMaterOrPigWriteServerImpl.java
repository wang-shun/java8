package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorParityMonthlyReportDao;
import io.terminus.doctor.event.dao.DoctorProfitMaterOrPigDao;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Created by terminus on 2017/4/12.
 */
@Slf4j
@Service
@RpcProvider
public class DoctorProfitMaterOrPigWriteServerImpl implements DoctorProfitMaterOrPigWriteServer{
    @RpcConsumer
    private DoctorProfitMaterOrPigDao doctorProfitMaterOrPigDao;

    @Override
    public Response<Boolean> updateDoctorProfitMaterialOrPig(List<DoctorProfitMaterialOrPig> doctorProfitMaterialOrPig) {
        return null;
    }

    @Override
    public Response<Boolean> insterDoctorProfitMaterialOrPig(List<DoctorProfitMaterialOrPig> doctorProfitMaterialOrPig) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    doctorProfitMaterOrPigDao.insertProfitMaterialOrPig(doctorProfitMaterialOrPig);
                }
            }, "insterDoctorProfitMaterialOrPig").start();
            return Response.ok(Boolean.TRUE);

        }catch (Exception e) {
            log.error("insert.DoctorProfitMaterialOrPig.fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("insert.DoctorProfitMaterialOrPig.fail");
        }
    }

    @Override
    public Response<Boolean> deleteDoctorProfitMaterialOrPig(Date sumTime) {
        try {
            doctorProfitMaterOrPigDao.deleteProfitMaterialOrPig(sumTime);
            return Response.ok(Boolean.TRUE);
        }catch (Exception e) {
            log.error("delete.DoctorProfitMaterialOrPig.fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("delete DoctorProfitMaterialOrPig fail");
        }
    }
}
