package io.terminus.doctor.user.service;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.user.dao.PrimaryUserDao;
import io.terminus.doctor.user.dao.SubDao;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.model.Sub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by houluyao on 16/5/24.
 */
@Slf4j
@Service
@RpcProvider
public class PrimaryUserReadServiceImpl implements PrimaryUserReadService {

    private final PrimaryUserDao primaryUserDao;

    private final SubDao subDao;

    @Autowired
    public PrimaryUserReadServiceImpl(PrimaryUserDao primaryUserDao, SubDao subDao) {
        this.primaryUserDao = primaryUserDao;
        this.subDao = subDao;
    }

    @Override
    public Response<Optional<Sub>> findSubSellerByParentUserIdAndUserId(Long parentUserId, Long userId) {
        try {
            Sub sub = subDao.findByParentUserIdAndUserId(parentUserId, userId);
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
    public Response<PrimaryUser> findPrimaryByFarmId(@NotNull(message = "farm.id.not.null") Long farmId) {
        try {
            return Response.ok(primaryUserDao.findPrimaryByFarmId(farmId));
        } catch (Exception e) {
            log.error("find primary by farmId failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.primary.by.farmId.failed");
        }
    }
}
