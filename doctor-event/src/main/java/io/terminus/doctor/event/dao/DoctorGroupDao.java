package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorGroup;
import org.springframework.stereotype.Repository;

import java.util.Date;
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

    /**
     * 根据时间倒推出当时的猪群（通常只适用于产房群，因为产房只有一个猪群）
     * @param farmId    猪场id
     * @param barnId    猪舍id
     * @param date      日期
     * @return  猪群
     */
    public DoctorGroup findByFarmIdAndBarnIdAndDate(Long farmId, Long barnId, Date date) {
        if (farmId == null || barnId == null || date == null) {
            return null;
        }
        return getSqlSession().selectOne(sqlId("findByFarmIdAndBarnIdAndDate"), ImmutableMap.of("farmId", farmId, "barnId", barnId, "date", date));

    }

    /**
     * 查当前产房的唯一猪群
     */
    public DoctorGroup findCurrentFarrowByBarnId(Long barnId) {
        return getSqlSession().selectOne(sqlId("findCurrentFarrowByBarnId"),
                ImmutableMap.of("barnId", barnId, "status", DoctorGroup.Status.CREATED.getValue()));
    }

    /**
     * 更改猪场名
     * @param farmId 需要更改的猪场id
     * @param farmName 新的猪场名
     */
    public void updateFarmName(Long farmId, String farmName) {
        getSqlSession().update(sqlId("updateFarmName"), ImmutableMap.of("farmId", farmId, "farmName", farmName));
    }

    public List<DoctorGroup> findByFarmIdAndDate(Long farmId, Date date) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndDate"), ImmutableMap.of("farmId", farmId, "date", date));
    }
}
