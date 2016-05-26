package io.terminus.doctor.event.dao;

import com.google.common.collect.Lists;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigTrackDao extends MyBatisDao<DoctorPigTrack>{

    /**
     * pigIds 获取对应的PigTrack
     * @param pigIds
     * @return
     */
    public List<DoctorPigTrack> findByPigIds(List<Long> pigIds){
        return this.getSqlSession().selectList(sqlId("findByPigIds"),pigIds);
    }

    public DoctorPigTrack findByPigId(Long pigId){
        List<DoctorPigTrack> pigs = findByPigIds(Lists.newArrayList(pigId));
        if (pigs != null && pigs.size() > 0) {
            return pigs.get(0);
        }
        return null;
    }

    public DoctorPigTrack findByEventId(Long relEventId){
        return this.getSqlSession().selectOne(sqlId("findByEventId"), relEventId);
    }
}
