package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪群事件表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorGroupEventDao extends MyBatisDao<DoctorGroupEvent> {

    public List<DoctorGroupEvent> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

}
