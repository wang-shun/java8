package io.terminus.doctor.event.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPig;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigDao extends MyBatisDao<DoctorPig> {

    /**
     * 条目数量信息统计
     * @param criteria
     * @return
     */
    public Long count(Map<String,Object> criteria){
        return this.getSqlSession().selectOne(sqlId(super.COUNT), criteria);
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
    public List<DoctorPig> listSince(Long lastId, String since, int limit) {
        return getSqlSession().selectList(sqlId("listSince"),
                ImmutableMap.of("lastId", lastId, "limit", limit, "since", since));
    }
}
