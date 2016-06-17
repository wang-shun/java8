package io.terminus.doctor.event.dao;

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

    public List<DoctorGroup> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    public List<DoctorGroup> fingByStatus(Integer status) {
        return getSqlSession().selectList(sqlId("fingByStatus"), status);
    }

    public List<DoctorGroup> findBySearchDto(DoctorGroupSearchDto searchDto) {
        return getSqlSession().selectList(sqlId("findBySearchDto"), searchDto);
    }

}
