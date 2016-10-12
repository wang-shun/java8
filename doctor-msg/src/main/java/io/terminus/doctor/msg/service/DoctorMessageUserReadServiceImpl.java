package io.terminus.doctor.msg.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.dao.DoctorMessageUserDao;
import io.terminus.doctor.msg.dto.DoctorMessageSearchDto;
import io.terminus.doctor.msg.dto.DoctorMessageUserDto;
import io.terminus.doctor.msg.model.DoctorMessageUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by xiao on 16/10/11.
 */
@Service
@RpcProvider
@Slf4j
public class DoctorMessageUserReadServiceImpl implements DoctorMessageUserReadService {
    @Autowired
    private DoctorMessageUserDao doctorMessageUserDao;
    @Override
    public Response<DoctorMessageUser> findDoctorMessageUserById(Long id) {
        try {
            return Response.ok(doctorMessageUserDao.findById(id));
        } catch (Exception e) {
            log.error("find.message.user.failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.message.user.failed");
        }
    }

    @Override
    public Response<Paging<DoctorMessageUser>> paging(DoctorMessageUserDto doctorMessageUserDto, Integer pageNo, Integer pageSize) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
            return Response.ok(doctorMessageUserDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), doctorMessageUserDto));
        } catch (Exception e) {
            log.error("paging.failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.failed");
        }
    }

    @Override
    public Response<Long> findNoReadCount(Long userId) {
        try{
            return Response.ok(doctorMessageUserDao.findNoReadCount(userId));
        } catch (Exception e) {
            log.error("find no read message count failed, user id is {}, cause by {}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("message.find.fail");
        }
    }

    @Override
    public Response<List<DoctorMessageUser>> findDoctorMessageUsersByCriteria(DoctorMessageUserDto doctorMessageUserDto) {
        try{
            return Response.ok(doctorMessageUserDao.list(doctorMessageUserDto));
        } catch (Exception e) {
            log.error("find message  user by criteria failed, criteria:{}, cause:{}", doctorMessageUserDto, Throwables.getStackTraceAsString(e));
            return Response.fail("message.user.find.fail");
        }
    }

    @Override
    public Response<List<Long>> findBusinessListByCriteria(DoctorMessageUserDto doctorMessageUserDto) {
        try {
            return Response.ok(doctorMessageUserDao.findBusinessListByCriteria(doctorMessageUserDto));
        } catch (Exception e) {
            log.error("find.message.count.by.criteria.failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.message.count.by.criteria.failed");
        }
    }
}
