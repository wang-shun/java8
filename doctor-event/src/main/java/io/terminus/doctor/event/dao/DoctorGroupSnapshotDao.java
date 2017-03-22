package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorGroupSnapshot;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪群快照表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorGroupSnapshotDao extends MyBatisDao<DoctorGroupSnapshot> {

    public DoctorGroupSnapshot findGroupSnapshotByToEventId(Long toEventId) {
        return getSqlSession().selectOne(sqlId("findGroupSnapshotByToEventId"), toEventId);
    }

    /**
     * 获取事件执行前的镜像
     * @param eventId 事件id
     * @return 镜像
     */
    public DoctorGroupSnapshot queryByEventId(Long eventId) {
        return getSqlSession().selectOne(sqlId("queryByEventId"), eventId);
    }

    /**
     * 获取没有镜像的猪群id列表
     * @return 猪群id列表
     */
    public List<Long> queryNotSnapshotGroupId() {
        return getSqlSession().selectList(sqlId("queryNotSnapshotGroupId"));
    }

    /**
     * 获取from_event_id 是空 的猪群id列表
     * @return 猪群id列表
     */
    public List<DoctorGroupSnapshot> queryByFromEventIdIsNull() {
        return getSqlSession().selectList(sqlId("queryByFromEventIdIsNull"));
    }

    public Boolean deleteByGroupId(Long groupId) {
        return getSqlSession().delete(sqlId("deleteByGroupId"), groupId) == 1;
    }
}
