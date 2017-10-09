package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.DoctorFarmExport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xjn on 17/3/23.
 */
@Repository
public class DoctorFarmExportDao extends MyBatisDao<DoctorFarmExport>{

    public List<DoctorFarmExport> query(DoctorFarmExport doctorFarmExport) {
        return getSqlSession().selectList(sqlId("query"), doctorFarmExport);
    }
}
