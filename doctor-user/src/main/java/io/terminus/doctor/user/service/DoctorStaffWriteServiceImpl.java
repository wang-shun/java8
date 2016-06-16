package io.terminus.doctor.user.service;


import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.event.CacheEvent;
import io.terminus.doctor.common.event.CacheMessage;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.event.UserStaffInfoModifyEvent;
import io.terminus.doctor.user.model.DoctorStaff;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DoctorStaffWriteServiceImpl implements DoctorStaffWriteService{
    private final DoctorStaffDao doctorStaffDao;
    private final CoreEventDispatcher coreEventDispatcher;
    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorStaffWriteServiceImpl(DoctorStaffDao doctorStaffDao,
                                       CoreEventDispatcher coreEventDispatcher){
        this.doctorStaffDao = doctorStaffDao;
        this.coreEventDispatcher = coreEventDispatcher;
    }


    @Override
    public Response<Long> createDoctorStaff(DoctorStaff staff) {
        Response<Long> response = new Response<>();
        try{
            doctorStaffDao.create(staff);
            this.publish(staff.getUserId());
            response.setResult(staff.getId());
        }catch(Exception e){
            log.error("createDoctorStaff failed, cause : {]", Throwables.getStackTraceAsString(e));
            response.setError("create.doctor.staff.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updateDoctorStaff(DoctorStaff staff) {
        Response<Boolean> response = new Response<>();
        try{
            this.publish(staff.getUserId());
            response.setResult(doctorStaffDao.update(staff));
        }catch(Exception e){
            log.error("createDoctorStaff failed, cause : {]", Throwables.getStackTraceAsString(e));
            response.setError("update.doctor.staff.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> deleteDoctorStaff(Long staffId) {
        Response<Boolean> response = new Response<>();
        try{
            DoctorStaff staff = doctorStaffDao.findById(staffId);
            if(staff != null){
                this.publish(staff.getUserId());
                doctorStaffDao.delete(staffId);
            }
            response.setResult(true);
        }catch(Exception e){
            log.error("createDoctorStaff failed, cause : {]", Throwables.getStackTraceAsString(e));
            response.setError("delete.doctor.staff.failed");
        }
        return response;
    }

    /**
     * 当用户的员工信息更新或删除时分发一个事件,监听器将会使缓存中的相应数据失效
     * @param userId
     */
    private void publish(Long userId) {
        if (publisher != null){
            try {
                publisher.publish(CacheEvent.toBytes(CacheEvent.make(CacheMessage.USER_STAFF_INFO.getValue(), userId)));
            } catch (Exception e) {
                log.error("failed to publish cache event, cause: {}", Throwables.getStackTraceAsString(e));
            }
        } else {
            coreEventDispatcher.publish(new UserStaffInfoModifyEvent(userId));
        }
    }
}
