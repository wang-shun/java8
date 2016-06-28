package io.terminus.doctor.event.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
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
     * 查询猪舍内猪状态
     * @param barnId 猪舍id
     * @return 猪状态
     */
    public List<Integer> findStatusByBarnId(Long barnId) {
        return getSqlSession().selectList(sqlId("findStatusByBarnId"), barnId);
    }

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

    /**
     * 猪的当前最大的id, 这个是dump搜素引擎用的
     *
     * @return 当前最大的id
     */
    public Long maxId() {
        Long count = getSqlSession().selectOne(sqlId("maxId"));
        return MoreObjects.firstNonNull(count, 0L);
    }

    /**
     * 查询id小于lastId内且更新时间大于since的limit个猪, 这个是dump搜素引擎用的
     *
     * @param lastId lastId 最大的猪id
     * @param since  起始更新时间
     * @param limit  个数
     */
    public List<DoctorPigTrack> listSince(Long lastId, String since, int limit) {
        return getSqlSession().selectList(sqlId("listSince"),
                ImmutableMap.of("lastId", lastId, "limit", limit, "since", since));
    }
}
