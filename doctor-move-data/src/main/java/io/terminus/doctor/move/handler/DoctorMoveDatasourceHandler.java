package io.terminus.doctor.move.handler;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.basic.dao.DoctorMoveDatasourceDao;
import io.terminus.doctor.basic.model.DoctorMoveDatasource;
import io.terminus.doctor.move.model.B_ChangeReason;
import io.terminus.doctor.move.sql.DoctorSqlFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Desc: move-data数据源信息读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-27
 */
@Slf4j
@Component
public class DoctorMoveDatasourceHandler implements CommandLineRunner {

    private final DoctorMoveDatasourceDao doctorMoveDatasourceDao;
    private final DoctorSqlFactory doctorSqlFactory;
    private final Map<Long, JdbcTemplate> jdbcMap = Maps.newHashMap();

    @Autowired
    public DoctorMoveDatasourceHandler(DoctorMoveDatasourceDao doctorMoveDatasourceDao,
                                       DoctorSqlFactory doctorSqlFactory) {
        this.doctorMoveDatasourceDao = doctorMoveDatasourceDao;
        this.doctorSqlFactory = doctorSqlFactory;
    }

    @PostConstruct
    public void init() {
        List<DoctorMoveDatasource> moves = doctorMoveDatasourceDao.listAll();
        moves.forEach(move -> jdbcMap.put(move.getId(), getJdbcTempalte(move)));
    }

    /**
     * 查询基础数据
     * @param id 数据源id
     * @return 基础数据
     */
    public <T> Response<List<T>> findAllData(Long id, Class<T> clazz, DoctorMoveTableEnum table) {
        try {
            if (clazz != table.getClazz()) {
                return Response.fail("class.not.equal");
            }

            JdbcTemplate jdbcTemplate = jdbcMap.get(id);
            if (jdbcTemplate == null) {
                return Response.fail("jdbc.not.found");
            }

            List<Map<String, Object>> map = jdbcTemplate.queryForList(getSql(table));
            return Response.ok(BeanMapper.mapList(map, clazz));
        } catch (Exception e) {
            log.error("find all data failed, id:{}, clazz:{}, table:{}, cause:{}", id, clazz, table, Throwables.getStackTraceAsString(e));
            return Response.fail("move.data.find.all.fail");
        }
    }

    public <T> Response<List<T>> findByHbsSql(Long id, Class<T> clazz, String hbsName) {
        try {
            JdbcTemplate jdbcTemplate = jdbcMap.get(id);
            if (jdbcTemplate == null) {
                return Response.fail("jdbc.not.found");
            }

            List<Map<String, Object>> map = jdbcTemplate.queryForList(doctorSqlFactory.getSql(hbsName, null));
            return Response.ok(BeanMapper.mapList(map, clazz));
        } catch (Exception e) {
            log.error("find all data failed, id:{}, clazz:{}, cause:{}", id, clazz, Throwables.getStackTraceAsString(e));
            return Response.fail("move.data.find.by.sql.fail");
        }
    }


    //获取JdbcTemplate
    private JdbcTemplate getJdbcTempalte(DoctorMoveDatasource db){
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
        return new JdbcTemplate(dataSource);
    }

    //查询全部的语句
    private static String getSql(DoctorMoveTableEnum table) {
        return "SELECT * FROM " + table.name();
    }

    @Override
    public void run(String... strings) throws Exception {
        List<B_ChangeReason> reasons = findByHbsSql(1L, B_ChangeReason.class, "changeReason").getResult();
        System.out.println(reasons);
    }
}
