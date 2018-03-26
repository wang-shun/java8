package io.terminus.doctor.user.service;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.user.dao.DoctorServiceStatusDao;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.dao.UserDaoExt;
import io.terminus.doctor.user.model.DoctorServiceStatus;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import io.terminus.parana.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by houluyao on 16/5/24.
 */
@Slf4j
@Service
@RpcProvider
public class PrimaryUserReadServiceImpl implements PrimaryUserReadService {

    private final PrimaryUserDao primaryUserDao;

    private final SubDao subDao;

    private final UserDaoExt userDao;

    private final DoctorServiceStatusDao doctorServiceStatusDao;

    @Autowired
    public PrimaryUserReadServiceImpl(PrimaryUserDao primaryUserDao, SubDao subDao, UserDaoExt userDao, DoctorServiceStatusDao doctorServiceStatusDao) {
        this.primaryUserDao = primaryUserDao;
        this.subDao = subDao;
        this.userDao = userDao;
        this.doctorServiceStatusDao = doctorServiceStatusDao;
    }

    @Override
    public Response<Optional<Sub>> findSubSellerByParentUserIdAndUserId(Long parentUserId, Long userId) {
        try {
            Sub sub = subDao.findByUserId(userId);
            return Response.ok(Optional.fromNullable(sub));
        } catch (Exception e) {
            log.error("find sub seller by parentUserId={} and userId={} failed, cause:{}",
                    parentUserId, userId, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.find.fail");
        }
    }

    @Override
    public Response<Paging<Sub>> subPagination(Long farmId, Long roleId, String roleName, String userName,
                                               String realName, Integer status, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            Sub criteria = new Sub();
            //criteria.setParentUserId(parentUserId);
            criteria.setFarmId(farmId);
            criteria.setStatus(status);
            criteria.setRoleId(roleId);
            criteria.setRoleName(roleName);
            criteria.setUserName(userName);
            criteria.setRealName(realName);
            return Response.ok(subDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging sub seller failed, farmId={}, status={}, pageNo={}, size={}, cause:{}",
                    farmId, status, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.paging.fail");
        }
    }

    @Override
    public Response<List<Sub>> findByConditions(Long parentUserId, Long roleId, String roleName, String userName,
                                         String realName, Integer status, Integer limit){
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("parentUserId", parentUserId);
            map.put("roleId", roleId);
            map.put("roleName", roleName);
            map.put("userName", userName);
            map.put("realName", realName);
            map.put("status", status);
            return Response.ok(subDao.findByConditions(map, limit));
        } catch (Exception e) {
            log.error("find sub failed, parentUserId={}, status={}, cause:{}", parentUserId, status, Throwables.getStackTraceAsString(e));
            return Response.fail("find.sub.by.conditions.fail");
        }
    }

    @Override
    public Response<List<Sub>> findAllActiveSubs() {
        try{
            return Response.ok(subDao.findAllActiveSubs());
        } catch (Exception e) {
            log.error("find all active subs failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("active.sub.find.fail");
        }
    }

    @Override
    public Response<Sub> findSubByUserId(Long subUserId){
        try{
            return Response.ok(subDao.findByUserId(subUserId));
        } catch (Exception e) {
            log.error("find sub info by sub user id failed, subUserId={}, cause by {}", subUserId, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.find.fail");
        }
    }

    @Override
    public Response<List<PrimaryUser>> findAllPrimaryUser() {
        try {
            return Response.ok(primaryUserDao.listAll());
        } catch (Exception e) {
            log.error("find.all.primary.user.failed, cause{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.all.primary.user.failed");
        }
    }

    @Override
    public Response<Sub> findSubById(@NotNull(message = "subId.not.null") Long subId) {
        try {
            return Response.ok(subDao.findById(subId));
        } catch (Exception e) {
            log.error("find sub by id failed, subId:{}, cause:{}", subId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.sub.failed");
        }
    }

    @Override
    public Response<Boolean> updateSub(Sub sub) {
        try {
            return Response.ok(subDao.update(sub));
        } catch (Exception e) {
            log.error("update sub failed, sub:{}, cause:{}", sub, Throwables.getStackTraceAsString(e));
            return Response.fail("update.sub.failed");
        }
    }

    @Override
    public Response<List<Sub>> findSubsByFarmId(@NotNull(message = "farm.id.not.null") Long farmId) {
        try {
            return Response.ok(subDao.findSubsByFarmId(farmId));
        } catch (Exception e) {
            log.error("find subs by farmId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.subs.by.farmId.failed");
        }
    }

    @Override
    public Response<List<Sub>> findSubsByFarmIdAndStatus(@NotNull(message = "farm.id.not.null") Long farmId, Integer status) {
        try {
            return Response.ok(subDao.findSubsByFarmIdAndStatus(farmId, status));
        } catch (Exception e) {
            log.error("find subs by farmId and status failed, farmId:{}, status:{}, cause:{}",
                    farmId, status, Throwables.getStackTraceAsString(e));
            return Response.fail("find.subs.by.farmId.and.status");
        }
    }

    @Override
    public Response<PrimaryUser> findPrimaryByFarmId(@NotNull(message = "farm.id.not.null") Long farmId) {
        try {
            return Response.ok(primaryUserDao.findPrimaryByFarmId(farmId));
        } catch (Exception e) {
            log.error("find primary by farmId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.primary.by.farmId.failed");
        }
    }

    @Override
    public Response<PrimaryUser> findPrimaryByFarmIdAndStatus(@NotNull(message = "farm.id.not.null") Long farmId, Integer status) {
        try {
            return Response.ok(primaryUserDao.findPrimaryByFarmIdAndStatus(farmId, status));
        } catch (Exception e) {
            log.error("find primary by farmId and status failed, farmId:{}, status:{}, cause:{}"
                    , farmId, status, Throwables.getStackTraceAsString(e));
            return Response.fail("find.primary.by.farmId.and.status.failed");
        }
    }

    @Override
    public Response<Map<Long, String>> findFarmIdToUserName() {
        try {
            List<PrimaryUser> primaryUsers = primaryUserDao.findAllRelFarmId();
            return Response.ok(primaryUsers.stream().collect(Collectors.toMap(PrimaryUser::getUserId,
                    v ->userDao.findById(v.getUserId()).getName())));
        } catch (Exception e) {
            log.error("find farmId to user name ,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.farmId.to.user.name");
        }
    }

    @Override
    public Response<PrimaryUser> findByUserId(Long userId) {
        try {
            return Response.ok(primaryUserDao.findByUserId(userId));
        } catch (Exception e) {
            log.error("find by user id failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.by.user.id.failed");
        }
    }

    @Override
    public Response<Paging<User>> pagingOpenDoctorServiceUser(Long id, String name, String email, String mobile, Integer status, Integer type, Integer pageNo, Integer pageSize) {
        try {
            List<DoctorServiceStatus> doctorServiceStatuses = doctorServiceStatusDao.listAllOpenDoctorService();
            if (Arguments.isNullOrEmpty(doctorServiceStatuses)) {
                return Response.ok(Paging.empty());
            }

            List<Long> userIds = doctorServiceStatuses.stream().map(DoctorServiceStatus::getUserId).collect(Collectors.toList());
            PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
            return Response.ok(userDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), MapBuilder.<String, Object>of().put("id", id)
                    .put("name", name).put("email", email).put( "mobile", mobile).put( "status", status).put( "type", type).put( "ids", userIds).map()));
        } catch (Exception e) {
            log.error("paging open doctor service user failed,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.open.doctor.service.user.failed");
        }
    }
}
