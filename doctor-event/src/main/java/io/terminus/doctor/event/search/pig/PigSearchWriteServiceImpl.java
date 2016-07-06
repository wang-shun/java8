package io.terminus.doctor.event.search.pig;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.search.api.IndexExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪 搜索写Service
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Service
@Slf4j
public class PigSearchWriteServiceImpl implements PigSearchWriteService {

    @Autowired
    private IndexExecutor indexExecutor;

    @Autowired
    private IndexedPigFactory indexedPigFactory;

    @Autowired
    private IndexedPigTaskAction indexedPigTaskAction;

    @Autowired
    private DoctorPigDao doctorPigDao;

    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;


    @Override
    public Response<Boolean> index(Long pigId) {
        try {
            DoctorPig pig = doctorPigDao.findById(pigId);
            DoctorPigTrack pigTrack = doctorPigTrackDao.findByPigId(pigId);
            IndexedPig indexedPig = indexedPigFactory.create(pig, pigTrack);
            log.info("IndexedPig is {}", indexedPig);
            if (indexedPig != null) {
                indexExecutor.submit(indexedPigTaskAction.indexTask(indexedPig));
            }
            return Response.ok(Boolean.TRUE);
        }catch (Exception e) {
            log.error("pig indexed failed, pig(id={}), cause by: {}",
                    pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.index.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long pigId) {
        try {
            indexExecutor.submit(indexedPigTaskAction.deleteTask(pigId));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e) {
            log.error("pig delete failed, pig(id={}), cause by: {}",
                    pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.delete.fail");
        }
    }

    @Override
    public Response<Boolean> update(Long pigId) {
        try {
            // 暂时不删除(只索引)
            return index(pigId);
        }catch (Exception e) {
            log.error("pig update failed, pig(id={}), cause by: {}",
                    pigId, Throwables.getStackTraceAsString(e));
            return Response.fail("pig.update.fail");
        }
    }
}
