package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBiPagesDao;
import io.terminus.doctor.basic.model.DoctorBiPages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-05 13:13:41
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorBiPagesReadServiceImpl implements DoctorBiPagesReadService {

    @Autowired
    private DoctorBiPagesDao doctorBiPagesDao;

    @Override
    public Response<DoctorBiPages> findById(Long id) {
        try {
            return Response.ok(doctorBiPagesDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor bi pages by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.bi.pages.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorBiPages>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorBiPagesDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor bi pages by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.bi.pages.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorBiPages>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorBiPagesDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor bi pages, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.bi.pages.list.fail");
        }
    }

    @Override
    public Response<DoctorBiPages> findByName(String name) {
        try {
            List<DoctorBiPages> pages = doctorBiPagesDao.list(Collections.singletonMap("name", name));
            if (pages.isEmpty())
                return Response.ok(null);
            return Response.ok(pages.get(0));
        } catch (Exception e) {
            log.error("failed to list doctor bi pages, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.bi.pages.list.fail");
        }
    }
}
