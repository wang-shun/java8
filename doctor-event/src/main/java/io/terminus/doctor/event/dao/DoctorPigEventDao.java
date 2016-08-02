package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    /**
     * 通过pigId 修改Event相关事件信息
     * @param params 修改对应的参数
     * @return
     */
    public Boolean updatePigEventFarmIdByPigId(Map<String, Object> params){
        return this.getSqlSession().update(sqlId("updatePigEventFarmIdByPigId"), params) >= 0;
    }

    public Long countPigEventTypeDuration(Long farmId, Integer eventType, Date startDate, Date endDate){
        return this.getSqlSession().selectOne(sqlId("countPigEventTypeDuration"),
                ImmutableMap.of("farmId", farmId, "eventType", eventType,
                        "startDate", startDate, "endDate", endDate));
    }

    public List<Long> queryAllFarmInEvent(){
        return this.getSqlSession().selectList(sqlId("queryAllFarmInEvent"));
    }

    /**
     * 根据猪场id和Kind查询
     * @param farmId 猪场id
     * @param kind 猪类(公猪, 母猪)
     * @return 事件list
     */
    public List<DoctorPigEvent> findByFarmIdAndKind(Long farmId, Integer kind) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndKind"), ImmutableMap.of("farmId", farmId, "kind", kind));
    }

    /**
     * 仅更新relEventId
     * @param pigEvent relEventId
     */
    public void updateRelEventId(DoctorPigEvent pigEvent){
        getSqlSession().update(sqlId("updateRelEventId"), pigEvent);
    }
}
