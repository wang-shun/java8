package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.IotUserRole;
import org.springframework.stereotype.Repository;

/**
 * Created by xjn on 17/10/11.
 */
@Repository
public class IotUserRoleDao extends MyBatisDao<IotUserRole>{

    /**
     * 更新角色名
     * @param iotRoleId 要更改的角色id
     * @param iotRoleName 新的角色名
     * @return 是否成功
     */
    public Boolean updateIotRoleName(Long iotRoleId, String iotRoleName) {
        return getSqlSession().update(sqlId("updateIotRoleName"),
                ImmutableMap.of("iotRoleId", iotRoleId, "iotRoleName", iotRoleName)) == 1;
    }
}
