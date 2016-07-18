package io.terminus.doctor.user.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

/**
 * Desc: 用户数据权限表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-18
 */
@Repository
public class DoctorUserDataPermissionDao extends MyBatisDao<DoctorUserDataPermission> {

    public DoctorUserDataPermission findByUserId(Long userId){
        return sqlSession.selectOne(sqlId("findByUserId"), userId);
    }

    public List<DoctorUserDataPermission> findByUserIds(List<Long> userIds){
        if(userIds == null || userIds.isEmpty()){
            return Collections.emptyList();
        }
        return sqlSession.selectList(sqlId("findByUserIds"), userIds);
    }
}
