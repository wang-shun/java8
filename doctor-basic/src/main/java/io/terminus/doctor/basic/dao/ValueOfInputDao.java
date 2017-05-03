package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.ValueOfInput;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Desc: 投入品价值
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/14
 */
@Repository
public class ValueOfInputDao extends MyBatisDao<ValueOfInput> {

    public List<ValueOfInput> rankingValueOfInput(Long farmId, Integer type) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("farmId", farmId);
        criteria.put("type", type);
        return getSqlSession().selectList(sqlId("rankingValueOfInput"), criteria);
    }
}
