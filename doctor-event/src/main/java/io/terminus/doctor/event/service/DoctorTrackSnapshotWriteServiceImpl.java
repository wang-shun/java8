package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorTrackSnapshotDao;
import io.terminus.doctor.event.model.DoctorTrackSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-01 17:01:25
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorTrackSnapshotWriteServiceImpl implements DoctorTrackSnapshotWriteService {

    @Autowired
    private DoctorTrackSnapshotDao doctorTrackSnapshotDao;

    @Override
    public Response<Long> create(DoctorTrackSnapshot doctorTrackSnapshot) {
        try{
            doctorTrackSnapshotDao.create(doctorTrackSnapshot);
            return Response.ok(doctorTrackSnapshot.getId());
        }catch (Exception e){
            log.error("failed to create doctor track snapshot, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.track.snapshot.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorTrackSnapshot doctorTrackSnapshot) {
        try{
            return Response.ok(doctorTrackSnapshotDao.update(doctorTrackSnapshot));
        }catch (Exception e){
            log.error("failed to update doctor track snapshot, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.track.snapshot.update.fail");
        }
    }

   @Override
    public Response<Boolean> delete(Long id) {
        try{
            return Response.ok(doctorTrackSnapshotDao.delete(id));
        }catch (Exception e){
            log.error("failed to delete doctor track snapshot by id:{}, cause:{}", id,  Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.track.snapshot.delete.fail");
        }
    }

}