package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.DoctorFarmMoveError;
import org.springframework.stereotype.Repository;

/**
 * Created by xjn on 17/9/6.
 */
@Repository
public class DoctorFarmMoveErrorDao extends MyBatisDao<DoctorFarmMoveError>{
    public void deleteAll(){
        getSqlSession().delete(sqlId("deleteAll"));
    }
}
