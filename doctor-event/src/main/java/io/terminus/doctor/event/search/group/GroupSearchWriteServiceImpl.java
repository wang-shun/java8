package io.terminus.doctor.event.search.group;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.search.api.IndexExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 猪群 搜索写Service
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Service
@Slf4j
public class GroupSearchWriteServiceImpl implements GroupSearchWriteService {

    @Autowired
    private IndexExecutor indexExecutor;

    @Autowired
    private IndexedGroupFactory indexedGroupFactory;

    @Autowired
    private IndexedGroupTaskAction indexedGroupTaskAction;

    @Autowired
    private DoctorGroupDao doctorGroupDao;

    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;


    @Override
    public Response<Boolean> index(Long groupId) {
        try {
            DoctorGroup group = doctorGroupDao.findById(groupId);
            DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(groupId);
            IndexedGroup indexedGroup = indexedGroupFactory.create(group, groupTrack);
            indexExecutor.submit(indexedGroupTaskAction.indexTask(indexedGroup));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e) {
            log.error("group indexed failed, group(id={}), cause by: {}",
                    groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.index.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long groupId) {
        try {
            indexExecutor.submit(indexedGroupTaskAction.deleteTask(groupId));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e) {
            log.error("group delete failed, group(id={}), cause by: {}",
                    groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.delete.fail");
        }
    }

    @Override
    public Response<Boolean> update(Long groupId) {
        try {
            // 暂时不删除(只索引)
            return index(groupId);
        }catch (Exception e) {
            log.error("group update failed, group(id={}), cause by: {}",
                    groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("group.update.fail");
        }
    }
}
