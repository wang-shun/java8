package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorTrackSnapshotDao;
import io.terminus.doctor.event.model.DoctorTrackSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-03-01 17:01:25
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorTrackSnapshotReadServiceImpl implements DoctorTrackSnapshotReadService {

    @Autowired
    private DoctorTrackSnapshotDao doctorTrackSnapshotDao;

    @Override
    public Response<DoctorTrackSnapshot> findById(Long id) {
        try{
            return Response.ok(doctorTrackSnapshotDao.findById(id));
        }catch (Exception e){
            log.error("failed to find doctor track snapshot by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.track.snapshot.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorTrackSnapshot>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorTrackSnapshotDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("failed to paging doctor track snapshot by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.track.snapshot.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorTrackSnapshot>> list(Map<String, Object> criteria) {
        try{
            return Response.ok(doctorTrackSnapshotDao.list(criteria));
        }catch (Exception e){
            log.error("failed to list doctor track snapshot, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.track.snapshot.list.fail");
        }
    }

}
