package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.UserBind;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public class UserBindDao extends MyBatisDao<UserBind> {

    public UserBind findUnique(Long userId, TargetSystem targetSystem){
        return getSqlSession().selectOne(sqlId("findUnique"), ImmutableMap.of("userId", userId, "targetSystem", targetSystem.value()));
    }

    public List<UserBind> findByUserId(Long userId){
        return getSqlSession().selectList(sqlId("findByUserId"), userId);
    }
}
