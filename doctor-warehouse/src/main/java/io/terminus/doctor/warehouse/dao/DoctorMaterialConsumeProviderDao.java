package io.terminus.doctor.warehouse.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorMaterialConsumeProviderDao extends MyBatisDao<DoctorMaterialConsumeProvider>{

    /**
     * 查询指定仓库最近一次领用/添加事件
     * @param wareHouseId 仓库id, 不可为空
     * @param eventType 事件类型, 可为空
     * @return
     */
    public DoctorMaterialConsumeProvider findLastEvent(Long wareHouseId, DoctorMaterialConsumeProvider.EVENT_TYPE eventType){
        Map<String, Object> param = new HashMap<>();
        param.put("wareHouseId", wareHouseId);
        if(eventType != null){
            param.put("eventType", eventType.getValue());
        }
        return sqlSession.selectOne(sqlId("findLastEvent"), ImmutableMap.copyOf(param));
    }
}
