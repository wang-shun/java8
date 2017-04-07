package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * 通过EventId 获取事件发生前的镜像
     * @param eventId 事件id
     * @return 镜像
     */
    public DoctorPigSnapshot queryByEventId(Long eventId){
        return this.getSqlSession().selectOne(sqlId("queryByEventId"), eventId);
    }

    /**
     * 获取猪最新的snapshot
     * @param pigId
     * @return
     */
    public DoctorPigSnapshot queryLastByPigId(Long pigId){
        return getSqlSession().selectOne(sqlId("queryLastByPigId"), pigId);
    }

    public List<DoctorPigSnapshot> findByPigId(Long pigId){
        return getSqlSession().selectList(sqlId("findByPigId"), pigId);
    }

    /**
     * 获取由toEventId导致的镜像
     * @param toEventId 事件id
     * @return 镜像
     */
    public DoctorPigSnapshot findByToEventId(Long toEventId) {
        return getSqlSession().selectOne(sqlId("findByToEventId"), toEventId);
    }

    /**
     * 获取没有镜像的猪id列表
     * @return 猪id列表
     */
    public List<Long> queryNotSnapshotPigId() {
        return getSqlSession().selectList(sqlId("queryNotSnapshotPigId"));
    }

    /**
     * 根据猪id删除猪所有镜像
     * @param pigId 猪id
     */
    public void deleteForPigId(Long pigId) {
        getSqlSession().delete(sqlId("deleteForPigId"), pigId);
    }

    /**
     * 删除猪某一事件所对应镜像后的镜像(包括所在镜像)
     * @param pigId
     * @param id
     */
    public void deleteAfterAndInclude(Long pigId, Long id) {
        getSqlSession().delete(sqlId("deleteAfterAndInclude"), ImmutableMap.of("pigId", pigId, "id", id));
    }
}
