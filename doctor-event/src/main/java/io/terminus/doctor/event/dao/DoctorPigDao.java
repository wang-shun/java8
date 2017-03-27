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

    public void deleteByFarmId(Long farmId) {
        getSqlSession().delete(sqlId("deleteByFarmId"), farmId);
    }

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
     * 通过farmId 获取未离场的PigCode
     * @param farmId
     * @return
     */
    public List<String> findPresentPigCodesByFarmId(Long farmId){
        return this.getSqlSession().selectList(sqlId("findPresentPigCodesByFarmId"), farmId);
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

    public DoctorPig findPigByFarmIdAndPigCodeAndSex(Long farmId, String pigCode, Integer sex) {
        return getSqlSession().selectOne(sqlId("findPigByFarmIdAndPigCodeAndSex"), ImmutableMap.of("farmId", farmId, "pigCode", pigCode, "sex", sex));
    }

    /**
     * 获取猪的数量（未离场的）
     */
    public Long getPigCount(Long farmId, DoctorPig.PigSex sex) {
        return getSqlSession().selectOne(sqlId("getPigCount"), ImmutableMap.of("farmId", farmId, "sex", sex.getKey()));
    }

    public List<DoctorPig> getPigSexList(Long farmId, DoctorPig.PigSex sex) {
        return getSqlSession().selectList(sqlId("getPigSexList"), ImmutableMap.of("farmId", farmId, "sex", sex.getKey()));
    }

    /**
     * 更改猪场名
     * @param farmId 需要更改的猪场id
     * @param farmName 新的猪场名
     */
    public void updateFarmName(Long farmId, String farmName) {
        getSqlSession().update(sqlId("updateFarmName"), ImmutableMap.of("farmId", farmId, "farmName", farmName));
    }
}
