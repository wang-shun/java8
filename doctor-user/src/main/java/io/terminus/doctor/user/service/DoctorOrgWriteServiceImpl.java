package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.model.DoctorOrg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RpcProvider
public class DoctorOrgWriteServiceImpl implements DoctorOrgWriteService{

    private final DoctorOrgDao doctorOrgDao;

    @Autowired
    public DoctorOrgWriteServiceImpl(DoctorOrgDao doctorOrgDao){
        this.doctorOrgDao = doctorOrgDao;
    }

    @Override
    public Response<Long> createOrg(DoctorOrg org) {
        Response<Long> response = new Response<>();
        try {
            doctorOrgDao.create(org);
            response.setResult(org.getId());
        } catch (Exception e) {
            log.error("create org failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("create.org.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updateOrg(DoctorOrg org) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.update(org));
        } catch (Exception e) {
            log.error("update org failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.org.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> deleteOrg(Long orgId) {
        Response<Boolean> response = new Response<>();
        try {
            response.setResult(doctorOrgDao.delete(orgId));
        } catch (Exception e) {
            log.error("delete org failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("delete.org.failed");
        }
        return response;
    }


    @Override
    public Response<Boolean> updateOrgName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updateName(id,name));
        }catch (Exception e){
            log.error("update org failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.org.failed");
        }
        return response;
    }


    @Override
    public Response<Boolean> updateBarnName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updateBarnName(id,name));
        }catch (Exception e){
            log.error("update Barn failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.Barn.failed");
        }
        return response;
    }


    @Override
    public Response<Boolean> updateFarmName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updateFarmsName(id,name));
        }catch (Exception e){
            log.error("update Farms failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.Farms.failed");
        }
        return response;
    }


    @Override
    public Response<Boolean> updateGroupEventName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updateGroupEventName(id,name));
        }catch (Exception e){
            log.error("update GroupEvent failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.GroupEvent.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updateGroupName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updateGroupName(id,name));
        }catch (Exception e){
            log.error("update Group failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.Group.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updatePigEventsName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updatePigEventsName(id,name));
        }catch (Exception e){
            log.error("update PigEvents failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.PigEvents.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updatePigScoreApplyName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updatePigScoreApplyName(id,name));
        }catch (Exception e){
            log.error("update PigScoreApply failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.PigScoreApply.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updatePigName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updatePigName(id,name));
        }catch (Exception e){
            log.error("update Pig failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.Pig.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updateGroupDaileName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updateGroupDaileName(id,name));
        }catch (Exception e){
            log.error("update GroupDaile failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.GroupDaile.failed");
        }
        return response;
    }

    @Override
    public Response<Boolean> updatePigDailieName(Long id, String name) {
        Response<Boolean> response=new Response<>();
        try{
            response.setResult(doctorOrgDao.updatePigDailieName(id,name));
        }catch (Exception e){
            log.error("update PigDailie failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("update.PigDailie.failed");
        }
        return response;
    }

    /**
     * 根据id查找公司名称
     * @param id
     * @return
     */
    @Override
    public Response<DoctorOrg> findName(Long id) {
        Response<DoctorOrg> response=new Response<>();
        try {
            response.setResult(doctorOrgDao.findName(id));
        } catch (Exception e) {
            log.error("find name by id failed, orgId:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            response.setError("find.name.by.id.failed");
        }
        return response;
    }

}
