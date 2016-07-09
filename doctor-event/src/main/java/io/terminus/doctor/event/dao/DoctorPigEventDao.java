package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigEventDao extends MyBatisDao<DoctorPigEvent>{

    public DoctorPigEvent queryLastPigEventById(Long pigId){
        return this.getSqlSession().selectOne(sqlId("queryLastPigEventById"), pigId);
    }

    /**
     * 获取PigId 对应的 所有事件
     * @param pigId
     * @return
     */
    public List<DoctorPigEvent> queryAllEventsByPigId(Long pigId){
        return this.getSqlSession().selectList(sqlId("queryAllEventsByPigId"), pigId);
    }
}
