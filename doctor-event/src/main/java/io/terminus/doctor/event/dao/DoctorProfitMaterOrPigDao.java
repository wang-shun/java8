package io.terminus.doctor.event.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorProfitMaterialOrPig;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by terminus on 2017/4/12.
 */
@Repository
public class DoctorProfitMaterOrPigDao extends MyBatisDao<DoctorProfitMaterialOrPig>{

    /**
     * 读出数据
     * @return
     */
    public List<DoctorProfitMaterialOrPig> findProfitMaterialOrPig(@NotNull(message = "farmId is null")Long farmId, Map<String, Object> map) {
        map.put("farmId", farmId);
        return getSqlSession().selectList(sqlId("findProfit"), map);

    }

    /**
     * 删除数据
     */
    public void deleteProfitMaterialOrPig(Date sumTime) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("sumTime", sumTime);
        getSqlSession().delete(sqlId("delete"), map);
    }

    /**
     * 更新数据
     * @return
     */
    public Boolean updateProfitMaterialOrPig(List<DoctorProfitMaterialOrPig> doctorProfitMaterialOrPig) {
        return null;
    }
    /**
     * 写去数据
     * @return
     */
    public void insertProfitMaterialOrPig(List<DoctorProfitMaterialOrPig> doctorProfitMaterialOrPig) {
        getSqlSession().insert(sqlId("createsProfit"),doctorProfitMaterialOrPig);
    }
}
