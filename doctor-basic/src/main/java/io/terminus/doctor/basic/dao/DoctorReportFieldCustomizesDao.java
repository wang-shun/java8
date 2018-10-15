package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.basic.model.DoctorReportFieldCustomizes;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 17:11:01
 * Created by [ your name ]
 */
@Repository
public class DoctorReportFieldCustomizesDao extends MyBatisDao<DoctorReportFieldCustomizes> {


    public void deleteByFarmAndType(Long farmId, List<Long> typeIds,Integer type) {

        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("ids", typeIds);
        params.put("type", type);
        this.sqlSession.delete(this.sqlId("deleteByFarmAndType"), params);
    }


}
