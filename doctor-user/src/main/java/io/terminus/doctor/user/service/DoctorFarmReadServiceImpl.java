package io.terminus.doctor.user.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.user.dao.DoctorFarmDao;
import io.terminus.doctor.user.dao.DoctorOrgDao;
import io.terminus.doctor.user.dto.FarmCriteria;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorFarmInformation;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.parana.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Slf4j
@Service
@RpcProvider
public class DoctorFarmReadServiceImpl implements DoctorFarmReadService{

    private final DoctorFarmDao doctorFarmDao;
    private final DoctorOrgDao doctorOrgDao;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    @Autowired
    public DoctorFarmReadServiceImpl(DoctorFarmDao doctorFarmDao,
                                     DoctorOrgDao doctorOrgDao,
                                     DoctorUserDataPermissionReadService doctorUserDataPermissionReadService){
        this.doctorFarmDao = doctorFarmDao;
        this.doctorOrgDao = doctorOrgDao;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
    }

    @Override
    public Response<DoctorFarm> findFarmById(Long farmId) {
        Response<DoctorFarm> response = new Response<>();
        try {
            response.setResult(doctorFarmDao.findById(farmId));
        } catch (Exception e) {
            log.error("find farm by id failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.farm.by.id.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorFarm>> findFarmsByUserId(Long userId) {
        Response<List<DoctorFarm>> response = new Response<>();
        try {
            List<Long> ids = RespHelper.orServEx(this.findFarmIdsByUserId(userId));
            if(Arguments.isNullOrEmpty(ids)){
                log.info("no farm find, userId = {}", userId);
                response.setResult(Lists.newArrayList());
                return response;
            }
            response.setResult(doctorFarmDao.findByIds(ids));
        } catch (ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find farms by userId failed, cause : {}", Throwables.getStackTraceAsString(e));
            response.setError("find.farms.by.userId.failed");
        }
        return response;
    }

    @Override
    public Response<List<Long>> findFarmIdsByUserId(Long userId){
        Response<List<Long>> response = new Response<>();
        try {
            DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
            if (permission != null) {
                response.setResult(permission.getFarmIdsList());
            } else {
                response.setResult(Lists.newArrayList());
            }
        } catch (ServiceException e) {
            response.setError(e.getMessage());
        } catch (Exception e) {
            log.error("find farms by userId failed, userId:{},cause:{}", userId, Throwables.getStackTraceAsString(e));
            response.setError("find.farms.by.userId.failed");
        }
        return response;
    }

    @Override
    public Response<List<DoctorFarm>> findAllFarms() {
        try {
            return Response.ok(doctorFarmDao.findAll());
        } catch (Exception e) {
            log.error("find all farms failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("farm.find.fail");
        }
    }

    @Override
    public Response<List<DoctorFarm>> findFarmsByOrgId(@NotNull(message = "orgId.not.null") Long orgId) {
        try {

            return Response.ok(doctorFarmDao.findByOrgId(orgId));
        } catch (Exception e) {
            log.error("find farms by orgId failed, orgId:{}, cause:{}", orgId, Throwables.getStackTraceAsString(e));
            return Response.fail("farm.find.fail");
        }
    }
    //ysq新增
    @Override
    public Response<List<DoctorFarm>> findFarmsByOrgId1(@NotNull(message = "orgId.not.null") Long orgId) {
        try {

            return Response.ok(doctorFarmDao.findByOrgId1(orgId));
        } catch (Exception e) {
            log.error("find farms by orgId failed, orgId:{}, cause:{}", orgId, Throwables.getStackTraceAsString(e));
            return Response.fail("farm.find.fail");
        }
    }

    @Override
    public Response<List<DoctorFarm>> findFarmsByIds(List<Long> ids) {
        try {
            if (Arguments.isNullOrEmpty(ids)) {
                return Response.ok(Lists.newArrayList());
            }
            return Response.ok(doctorFarmDao.findFarmsByIds(ids));
        } catch (Exception e) {
            log.error("find farms by ids failed, ids:{} cause:{}", ids, Throwables.getStackTraceAsString(e));
            return Response.fail("find.farms.by.ids.failed");
        }
    }

    @Override
    public Response<List<DoctorFarm>> findFarmsBy(Long orgId, Integer isIntelligent) {
        try {
            return Response.ok(doctorFarmDao.findFarmsBy(orgId, isIntelligent));
        } catch (Exception e) {
            log.error("find farms by failed, orgId:{}, isIntelligent:{}, cause:{}",
                    orgId, isIntelligent, Throwables.getStackTraceAsString(e));
            return Response.fail("find.farms.by.failed");
        }
    }

    @Override
    public Response<Paging<DoctorFarm>> pagingFarm(FarmCriteria farmCriteria, Integer pageNo, Integer pageSize) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
            return Response.ok(doctorFarmDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), farmCriteria));
        } catch (Exception e) {
            log.error("paging farm failed, doctorFarm:{},cause:{}",
                    farmCriteria, Throwables.getStackTraceAsString(e));
            return Response.fail("paging.farm.failed");
        }
    }

    @Override
    public Response<DoctorFarm> findByNumber(String number) {
        try {
            return Response.ok(doctorFarmDao.findByNumber(number));
        } catch (Exception e) {
            log.error("find by number failed, number:{},cause:{}", number, Throwables.getStackTraceAsString(e));
            return Response.fail("find.by.number.failed");
        }
    }

    //  ---------------------  新增代码  2018年8月28日17:50:56 ----------------------
    @Override
    public List<DoctorFarmInformation> findSubordinatePig(Date date) {
        return doctorFarmDao.findSubordinatePig(date);
    }

   //ysq新增
    @Override
    public List<DoctorOrg> findOrgByParentId(Long parent) {
            return doctorOrgDao.findOrgByParent(parent);
    }

    @Override
    public Response<List<DoctorFarm>> findFarmsByGroupId(@NotNull(message = "groupId.not.null") Long groupId) {
        try {

            return Response.ok(doctorFarmDao.findFarmsByGroupId(groupId));
        } catch (Exception e) {
            log.error("find farms by orgId failed, orgId:{}, cause:{}", groupId, Throwables.getStackTraceAsString(e));
            return Response.fail("farm.find.fail");
        }
    }
}
