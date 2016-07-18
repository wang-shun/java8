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
    public Response<PrimaryUser> findPrimaryUserById(Long id) {
        try {
            PrimaryUser primaryUser = primaryUserDao.findById(id);
            if (primaryUser == null) {
                log.warn("primaryUser not found, id={}", id);
                return Response.fail("primaryUser.not.found");
            }
            return Response.ok(primaryUser);
        } catch (Exception e) {
            log.error("find primaryUser failed, id={}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("primaryUser.find.fail");
        }
    }

    @Override
    public Response<Optional<PrimaryUser>> findPrimaryUserByUserId(Long userId) {
        try {
            PrimaryUser primaryUser = primaryUserDao.findByUserId(userId);
            return Response.ok(Optional.fromNullable(primaryUser));
        } catch (Exception e) {
            log.error("find primaryUser by userId={} failed, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("primaryUser.find.fail");
        }
    }

    @Override
    public Response<Paging<PrimaryUser>> primaryUserPagination(Long userId, Integer status, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            PrimaryUser criteria = new PrimaryUser();
            criteria.setUserId(userId);
            criteria.setStatus(status);
            return Response.ok(primaryUserDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging seller failed, userId={}, status={}, pageNo={}, size={}, cause:{}",
                    userId, status, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("primaryUser.paging.fail");
        }
    }

    @Override
    public Response<Sub> findSubById(Long id) {
        try {
            Sub sub = subDao.findById(id);
            if (sub == null) {
                log.warn("sub not found, id={}", id);
                return Response.fail("sub.not.found");
            }
            return Response.ok(sub);
        } catch (Exception e) {
            log.error("find sub by id={} failed, cause:{}",
                    id, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.find.fail");
        }
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
    public Response<Paging<Sub>> subPagination(Long parentUserId, Long roleId, String roleName, String userName,
                                               String realName, Integer status, Integer pageNo, Integer size) {
        try {
            PageInfo page = new PageInfo(pageNo, size);
            Sub criteria = new Sub();
            criteria.setParentUserId(parentUserId);
            criteria.setStatus(status);
            criteria.setRoleId(roleId);
            criteria.setRoleName(roleName);
            criteria.setUserName(userName);
            criteria.setRealName(realName);
            return Response.ok(subDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging sub seller failed, parentUserId={}, status={}, pageNo={}, size={}, cause:{}",
                    parentUserId, status, pageNo, size, Throwables.getStackTraceAsString(e));
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
    public Response<List<Sub>> findSubByUserIds(List<Long> subUserIds){
        try{
            return Response.ok(subDao.findByUserIds(subUserIds));
        } catch (Exception e) {
            log.error("find sub info by sub user id failed, subUserIds={}, cause by {}", subUserIds, Throwables.getStackTraceAsString(e));
            return Response.fail("sub.find.fail");
        }
    }
}
