package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.user.model.PrimaryUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Effet
 */
@Slf4j
@Repository
public class PrimaryUserDao extends MyBatisDao<PrimaryUser> {

    public Boolean create(PrimaryUser primaryUser) {
        log.info("=====================================PrimaryUserDao.PrimaryUser.create");
        return this.sqlSession.insert(this.sqlId("create"), primaryUser) == 1;
    }

    public PrimaryUser findByUserId(Long userId) {
        return getSqlSession().selectOne(sqlId("findByUserId"), userId);
    }

    public PrimaryUser findIncludeFrozenByUserId(Long userId){
        return sqlSession.selectOne(sqlId("findIncludeFrozenByUserId"), userId);
    }

    /**
     * 获取关联猪场主账号列表
     * @param farmId 猪场id
     * @return 主账号
     */
    public PrimaryUser findPrimaryByFarmId(Long farmId) {
        return getSqlSession().selectOne(sqlId("findPrimaryByFarmId"), farmId);
    }

    public PrimaryUser findPrimaryByFarmIdAndStatus(Long farmId, Integer status ) {
        return getSqlSession().selectOne(sqlId("findPrimaryByFarmIdAndStatus"), ImmutableMap.of("farmId", farmId, "status", status));
    }

    /**
     * 获取所有关联猪场不为空的列表
     * @return 主账户类别
     */
    public List<PrimaryUser> findAllRelFarmId() {
        return getSqlSession().selectList(sqlId("findAllRelFarmId"));
    }

    public Boolean freezeByUser(Long userId) {
        return getSqlSession().update(sqlId("freezeByUser"), userId) == 1;
    }
}
