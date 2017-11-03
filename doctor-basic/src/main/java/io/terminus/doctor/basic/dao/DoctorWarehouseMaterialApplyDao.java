package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseMaterialApplyDao extends MyBatisDao<DoctorWarehouseMaterialApply> {


    public List<DoctorWarehouseMaterialApply> listAndOrderByHandleDate(DoctorWarehouseMaterialApply criteria, Integer limit) {

        Map<String, Object> params = Maps.newHashMap();
        if (criteria != null) {
            Map<String, Object> objMap = (Map) JsonMapper.nonDefaultMapper().getMapper().convertValue(criteria, Map.class);
            params.putAll(objMap);
        }
        if (null != limit)
            params.put("limit", limit);

        return sqlSession.selectList(sqlId("listAndOrderByHandleDate"), params);
    }


    public List<DoctorWarehouseMaterialApply> advList(Map<String, Object> criteria){
        return sqlSession.selectList(sqlId("advList"), criteria);
    }

    /**
     * 猪群饲料指定时间段内领用和
     * @param groupId 猪群id
     * @param startAt 开始时间 yy-MM-dd
     * @param endAt 结束时间
     * @return 和
     */
    public Double sumGroupFeedApply(Long groupId, String startAt, String endAt) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("groupId", groupId);
        map.put("startAt", startAt);
        map.put("endAt", endAt);
        return getSqlSession().selectOne(sqlId("sumGroupFeedApply"),map);
    }

}

