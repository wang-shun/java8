package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorRevertLog;
import org.springframework.stereotype.Repository;

/**
 * Desc: 回滚记录表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorRevertLogDao extends MyBatisDao<DoctorRevertLog> {

}
