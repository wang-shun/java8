package io.terminus.doctor.event.dao;

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
     * 母猪设置离场
     * @param id
     * @return
     */
    public Boolean removalPig(Long id){
        return this.getSqlSession().update(sqlId("removalPig"), id) == 1;
    }

    public List<DoctorPig> findPigsByFarmId(Long farmId) {
        return getSqlSession().selectList("findPigsByFarmId", farmId);
    }

    /**
     * 通过FarmId 获取参数 信息
     * @param farmId
     * @return
     */
    public List<String> findPigCodesByFarmId(Long farmId){
        return this.getSqlSession().selectList(sqlId("findPigCodesByFarmId"), farmId);
    }

    /**
     * 通过CompanyId 获取对应的PigCode
     * @param companyId
     * @return
     */
    public List<String> findPigCodesByCompanyId(Long companyId){
        return this.getSqlSession().selectList(sqlId("findPigCodesByCompanyId"), companyId);
    }

    /**
     * 根据猪场id和猪类查猪
     * @param farmId  猪场id
     * @param pigType 猪类(公猪, 母猪)
     * @return 猪list
     */
    public List<DoctorPig> findPigsByFarmIdAndPigType(Long farmId, Integer pigType) {
        return getSqlSession().selectList(sqlId("findPigsByFarmIdAndPigType"), ImmutableMap.of("farmId", farmId, "pigType", pigType));
    }
}
