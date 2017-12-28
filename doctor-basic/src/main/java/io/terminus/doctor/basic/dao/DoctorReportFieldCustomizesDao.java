package io.terminus.doctor.basic.dao;

import io.terminus.doctor.basic.model.DoctorReportFieldCustomizes;
import io.terminus.common.mysql.dao.MyBatisDao;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 17:11:01
 * Created by [ your name ]
 */
@Repository
public class DoctorReportFieldCustomizesDao extends MyBatisDao<DoctorReportFieldCustomizes> {


    public void deleteByType(List<Long> typeIds) {
        this.sqlSession.delete(this.sqlId("deleteByType"), Collections.singletonMap("ids", typeIds));
    }


}
