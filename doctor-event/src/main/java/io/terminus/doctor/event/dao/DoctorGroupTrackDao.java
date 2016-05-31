package io.terminus.doctor.event.dao;

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
}
