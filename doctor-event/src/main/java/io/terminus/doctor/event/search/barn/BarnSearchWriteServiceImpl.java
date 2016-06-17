package io.terminus.doctor.event.search.barn;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.search.api.IndexExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
public class BarnSearchWriteServiceImpl implements BarnSearchWriteService {

    @Autowired
    private IndexExecutor indexExecutor;

    @Autowired
    private IndexedBarnFactory indexedBarnFactory;

    @Autowired
    private IndexedBarnTaskAction indexedBarnTaskAction;

    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Override
    public Response<Boolean> index(Long barnId) {
        try {
            IndexedBarn indexedBarn = indexedBarnFactory.create(doctorBarnDao.findById(barnId));
            if (indexedBarn != null) {
                indexExecutor.submit(indexedBarnTaskAction.indexTask(indexedBarn));
            }
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("barn indexed failed, barn(id={}), cause by: {}",
                barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.index.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long barnId) {
        try {
            indexExecutor.submit(indexedBarnTaskAction.deleteTask(barnId));
            return Response.ok(Boolean.TRUE);
        }catch (Exception e) {
            log.error("barn delete failed, barn(id={}), cause by: {}",
                    barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.delete.fail");
        }
    }

    @Override
    public Response<Boolean> update(Long barnId) {
        try {
            // 暂时不删除(只索引)
            return index(barnId);
        }catch (Exception e) {
            log.error("barn update failed, barn(id={}), cause by: {}",
                    barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.update.fail");
        }
    }
}
