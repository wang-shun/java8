package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorGroupElicitRecord;
import org.springframework.stereotype.Repository;

/**
 * Created by xjn on 17/3/30.
 */
@Repository
public class DoctorGroupElicitRecordDao extends MyBatisDao<DoctorGroupElicitRecord>{

    /**
     * 查询最新版本
     * @param pigId
     * @return
     */
    public Integer findLastVersion(Long pigId) {
        return getSqlSession().selectOne(sqlId("findLastVersion"), pigId);
    }

}
