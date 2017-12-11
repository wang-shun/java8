package io.terminus.doctor.event.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 */
public class DoctorStatisticDao {
    private final SqlSessionTemplate sqlSession;

    @Autowired
    public DoctorStatisticDao(SqlSessionTemplate sqlSession) {
        this.sqlSession = sqlSession;
    }

    private static String sqlId(String id) {
        return "DoctorStatistic." + id;
    }


}
