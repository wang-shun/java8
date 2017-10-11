package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.IotRole;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xjn on 17/10/11.
 */
@Repository
public class IotRoleDao extends MyBatisDao<IotRole> {

    /**
     * 获取所有有效物联网角色
     * @return 所有物联网角色
     */
    public List<IotRole> listEffected() {
        return getSqlSession().selectList(sqlId("listEffected"));
    }
}
