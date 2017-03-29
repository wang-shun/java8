package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.event.model.DoctorGroupInfoCheck;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪群数据校验表Dao类
 * Date: 2017-03-25
 */
@Repository
public class DoctorGroupInfoCheckDao extends MyBatisDao<DoctorGroupInfoCheck> {

    public List<DoctorGroupInfoCheck> getGroupCheckDatas(Integer offset, Integer limit, Long farmId){
        return sqlSession.selectList(sqlId("checkGroupData"), MapBuilder.newHashMap().put("offset", offset, "limit", limit, "farmId", farmId).map());
    }

    public boolean deletebyFarmIdAndSumAt(Long farmId, Date sumAt) {
        return sqlSession.delete(sqlId("deletebyFarmIdAndSumAt"), MapBuilder.newHashMap().put("farmId", farmId, "sumAt", DateUtil.toDateString(sumAt)).map()) == 1;
    }
}
