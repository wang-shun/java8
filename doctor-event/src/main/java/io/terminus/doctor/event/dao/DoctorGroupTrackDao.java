package io.terminus.doctor.event.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪群卡片明细表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorGroupTrackDao extends MyBatisDao<DoctorGroupTrack> {

    /**
     * 根据猪群id查询猪群Track信息
     * @param groupId   猪群id
     * @return
     */
    public DoctorGroupTrack findByGroupId(Long groupId) {
        return getSqlSession().selectOne("findByGroupId", groupId);
    }

    /**
     * 重新计算下日龄
     * @param groupIds 猪群ids
     */
    public void incrDayAge(List<Long> groupIds) {
        getSqlSession().update(sqlId("incrDayAge"), groupIds);
    }

    /**
     * 猪群的当前最大的id, 这个是dump搜素引擎用的
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
     * @param lastId lastId 最大的猪群id
     * @param since  起始更新时间
     * @param limit  个数
     */
    public List<DoctorGroupTrack> listSince(Long lastId, String since, int limit) {
        return getSqlSession().selectList(sqlId("listSince"),
                ImmutableMap.of("lastId", lastId, "limit", limit, "since", since));
    }
}
