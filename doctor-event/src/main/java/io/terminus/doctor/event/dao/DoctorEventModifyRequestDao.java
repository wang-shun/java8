package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xjn on 17/3/9.
 * 事件编辑dao
 */
@Repository
public class DoctorEventModifyRequestDao extends MyBatisDao<DoctorEventModifyRequest>{


    /**
     * 查询所需状态的编辑请求
     * @param status 状态
     * @return 请求列表
     */
    public List<DoctorEventModifyRequest> listByStatus(Integer status) {
        return getSqlSession().selectList(sqlId("listByStatus"), status);
    }

    /**
     * 批量更新编辑请求状态
     * @param ids 请求id列表
     * @param status 更新状态
     * @return 更新结果
     */
    public Boolean batchUpdateStatus(List<Long> ids, Integer status) {
        return getSqlSession().update(sqlId("batchUpdateStatus"), ImmutableMap.of("ids", ids, "status", status)) == 1;
    }

}
