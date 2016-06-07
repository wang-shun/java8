package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.parana.user.model.UserProfile;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc: 用户个人信息扩展Dao
 * Mail: houly@terminus.io
 * Data: 下午9:31 16/6/6
 * Author: houly
 */
@Repository
public class UserProfileExtraDao extends MyBatisDao<UserProfile>{

    /**
     * 以用户id查询用户信息
     * @param userIds 用户id集合
     * @return 用户信息
     */
    public List<UserProfile> findByUserIds(List<Long> userIds){
        return getSqlSession().selectList(sqlId("findByUserIds"), userIds);
    }

}
