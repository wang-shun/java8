package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigEvent;
import jdk.nashorn.internal.ir.annotations.Immutable;
import org.springframework.stereotype.Repository;
import sun.util.resources.cldr.ga.LocaleNames_ga;

import java.util.Date;
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

    public Integer countPigEventTypeDuration(Long farmId, Integer eventType, Date startDate, Date endDate){
        return this.getSqlSession().selectOne(sqlId("countPigEventTypeDuration"),
                ImmutableMap.of("farmId", farmId, "eventType", eventType, "startDate", startDate, "endDate", endDate));
    }
}
