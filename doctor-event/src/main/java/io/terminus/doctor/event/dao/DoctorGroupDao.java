package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 猪群卡片表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorGroupDao extends MyBatisDao<DoctorGroup> {

    /**
     * 过滤掉已关闭的群
     */
    public List<DoctorGroup> findByCurrentBarnId(Long currentBarnId) {
        return getSqlSession().selectList(sqlId("findByCurrentBarnId"), currentBarnId);
    }

    public List<DoctorGroup> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    public List<DoctorGroup> fingByStatus(Integer status) {
        return getSqlSession().selectList(sqlId("fingByStatus"), status);
    }

    public List<DoctorGroup> findBySearchDto(DoctorGroupSearchDto searchDto) {
        return getSqlSession().selectList(sqlId("findBySearchDto"), searchDto);
    }

    /**
     * 根据groupCode查询某猪场内的猪群
     * @param farmId    猪场id
     * @param groupCode 猪群号
     * @return 猪群
     */
    public DoctorGroup findByFarmIdAndGroupCode(Long farmId, String groupCode) {
        return getSqlSession().selectOne(sqlId("findByFarmIdAndGroupCode"), ImmutableMap.of("farmId", farmId, "groupCode", groupCode));
    }
}
