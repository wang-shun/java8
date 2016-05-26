package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import org.springframework.stereotype.Repository;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigSnapshotDao extends MyBatisDao<DoctorPigSnapshot>{

    public Boolean deleteByEventId(Long eventId){
        return this.getSqlSession().delete(sqlId("deleteByEventId"),eventId) == 1;
    }

    /**
     * 通过EventId 获取对应的事件信息
     * @param eventId
     * @return
     */
    public DoctorPigSnapshot queryByEventId(Long eventId){
        return this.getSqlSession().selectOne(sqlId("queryByEventId"), eventId);
    }
}
