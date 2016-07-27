package io.terminus.doctor.basic.datasource;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorMoveDatasourceDao;
import io.terminus.doctor.basic.model.DoctorMoveDatasource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Desc: move-data数据源信息读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-27
 */
@Slf4j
@Component
public class DoctorMoveDatasourceHandler {

    private final DoctorMoveDatasourceDao doctorMoveDatasourceDao;

    @Autowired
    public DoctorMoveDatasourceHandler(DoctorMoveDatasourceDao doctorMoveDatasourceDao) {
        this.doctorMoveDatasourceDao = doctorMoveDatasourceDao;
    }

    /**
     * 获取 JdbcTemplate
     *
     * @param moveDatasourceId 主键id
     * @return JdbcTemplate
     */
    public Response<JdbcTemplate> findMoveDataJdbcTemplateById(Long moveDatasourceId) {
        try {
            DoctorMoveDatasource datasource = doctorMoveDatasourceDao.findById(moveDatasourceId);
            if (datasource == null) {
                return Response.fail("moveDatasource.find.fail");
            }
            return Response.ok(new JdbcTemplate(getDataSource(datasource)));
        } catch (Exception e) {
            log.error("find move data failed, id:{}, cause:{}", moveDatasourceId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.move.data.fail");
        }
    }

    private DataSource getDataSource(DoctorMoveDatasource db){
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(db.getDriver());
        dataSource.setUrl(db.getUrl());
        dataSource.setUsername(db.getUsername());
        dataSource.setPassword(db.getPassword());
        dataSource.setMaxActive(2);
        dataSource.setMaxIdle(2);
        dataSource.setDefaultAutoCommit(false);
        dataSource.setInitialSize(2);
        dataSource.setTimeBetweenEvictionRunsMillis(600000L);
        dataSource.setMinEvictableIdleTimeMillis(1800000L);
        return dataSource;
    }
}
