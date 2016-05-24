package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.event.model.DoctorBarn;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪舍表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorBarnDao extends MyBatisDao<DoctorBarn> {

    public List<DoctorBarn> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    public List<DoctorBarn> findByEnums(Long farmId, Integer pigType, Integer canOpenGroup, Integer status) {
        return getSqlSession().selectList(sqlId("findByEnums"), MapBuilder.<String, Object>newHashMap()
                .put("farmId", farmId)
                .put("pigType", pigType)
                .put("canOpenGroup", canOpenGroup)
                .put("status", status)
                .map());
    }
}
