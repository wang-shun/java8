package io.terminus.doctor.basic.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 基础物料表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */
@Repository
public class DoctorBasicMaterialDao extends MyBatisDao<DoctorBasicMaterial> {

    public List<DoctorBasicMaterial> findByType(Integer type) {
        return getSqlSession().selectList(sqlId("findByType"), MapBuilder.<String, Integer>of().put("type", type).map());
    }

    /**
     * 物料的当前最大的id, 这个是dump搜素引擎用的
     *
     * @return 当前最大的id
     */
    public Long maxId() {
        Long count = getSqlSession().selectOne(sqlId("maxId"));
        return MoreObjects.firstNonNull(count, 0L);
    }

    /**
     * 查询id小于lastId内且更新时间大于since的limit个物料, 这个是dump搜素引擎用的
     *
     * @param lastId lastId 最大的猪id
     * @param since  起始更新时间
     * @param limit  个数
     */
    public List<DoctorBasicMaterial> listSince(Long lastId, String since, int limit) {
        return getSqlSession().selectList(sqlId("listSince"),
                ImmutableMap.of("lastId", lastId, "limit", limit, "since", since));
    }
}
