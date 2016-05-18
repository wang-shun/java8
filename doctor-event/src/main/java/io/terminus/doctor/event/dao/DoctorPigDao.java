package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPig;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigDao extends MyBatisDao<DoctorPig> {

    /**
     * 条目数量信息统计
     * @param criteria
     * @return
     */
    public Long count(Map<String,Object> criteria){
        return this.getSqlSession().selectOne(sqlId(super.COUNT), criteria);
    }

}
