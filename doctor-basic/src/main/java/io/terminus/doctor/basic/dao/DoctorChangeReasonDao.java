package io.terminus.doctor.basic.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 变动类型表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorChangeReasonDao extends MyBatisDao<DoctorChangeReason> {

    /**
     * 根据变动类型查询
     * @param farmId 猪场id
     * @param changeTypeId 变动类型id
     * @return 变动原因列表
     */
    public List<DoctorChangeReason> findByFarmIdChangeTypeId(Long farmId, Long changeTypeId) {
        return getSqlSession().selectList(sqlId("findByFarmIdChangeTypeId"), ImmutableMap.of("farmId", farmId, "changeTypeId", changeTypeId));
    }
}
