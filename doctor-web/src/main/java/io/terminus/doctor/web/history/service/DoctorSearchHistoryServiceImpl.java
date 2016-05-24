package io.terminus.doctor.web.history.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.web.history.dao.DoctorSearchHistoryDao;
import io.terminus.doctor.web.history.enums.SearchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/24
 */
@Slf4j
@Service
public class DoctorSearchHistoryServiceImpl implements DoctorSearchHistoryService {

    private final DoctorSearchHistoryDao doctorSearchHistoryDao;

    @Autowired
    public DoctorSearchHistoryServiceImpl(DoctorSearchHistoryDao doctorSearchHistoryDao) {
        this.doctorSearchHistoryDao = doctorSearchHistoryDao;
    }

    @Override
    public Response<Boolean> createBarnSearchHistory(Long userId, String barnName) {
        try {
            doctorSearchHistoryDao.setWord(userId, SearchType.BARN, barnName);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("create barn search history failed, userId:{}, barnName:{}, cause:{}",
                    userId, barnName, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.search.history.create.fail");
        }
    }

    @Override
    public Response<Set<String>> findBarnSearchHistory(Long userId) {
        try {
            return Response.ok(doctorSearchHistoryDao.getWords(userId, SearchType.BARN));
        } catch (Exception e) {
            log.error("find barn search history failed, userId:{}, cause:{}",
                    userId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.search.history.find.fail");
        }
    }

    @Override
    public Response<Boolean> deleteBarnSearchHistory(Long userId, String barnName) {
        try {
            doctorSearchHistoryDao.deleteWord(userId, SearchType.BARN, barnName);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("delete barn search history failed, userId:{}, barnName:{}, cause:{}",
                    userId, barnName, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.search.history.delete.fail");
        }
    }

    @Override
    public Response<Boolean> deleteAllBarnSearchHistories(Long userId) {
        try {
            doctorSearchHistoryDao.deleteAllWords(userId, SearchType.BARN);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("delete barn search history failed, userId:{}, cause:{}",
                    userId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.search.history.delete.fail");
        }
    }
}
