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
     * 根据变动类型和输入码查询
     * @param changeTypeId 变动类型id
     * @param srm 不区分大小写模糊匹配
     * @return 变动原因列表
     */
    public List<DoctorChangeReason> findByChangeTypeIdAndSrm(Long changeTypeId, String srm) {
        return getSqlSession().selectList(sqlId("findByChangeTypeIdAndSrm"), ImmutableMap.of("changeTypeId", changeTypeId, "srm", srm));
    }
}
