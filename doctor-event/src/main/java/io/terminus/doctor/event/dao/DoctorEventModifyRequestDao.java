package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import org.springframework.stereotype.Repository;

/**
 * Created by xjn on 17/3/9.
 * 事件编辑dao
 */
@Repository
public class DoctorEventModifyRequestDao extends MyBatisDao<DoctorEventModifyRequest>{
}
