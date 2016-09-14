package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import org.springframework.stereotype.Repository;

/**
 * Desc: 猪群批次总结表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */
@Repository
public class DoctorGroupBatchSummaryDao extends MyBatisDao<DoctorGroupBatchSummary> {

    /**
     * 根据猪群id查询猪群批次总结信息
     * @param groupId   猪群id
     * @return 猪群跟踪
     */
    public DoctorGroupBatchSummary findByGroupId(Long groupId) {
        return getSqlSession().selectOne("findGroupBatchSummaryByGroupId", groupId);
    }

    /**
     * 根据groupId删除
     * @param groupId 猪群id
     */
    public void deleteByGroupId(Long groupId) {
        getSqlSession().delete("deleteByGroupId", groupId);
    }
}
