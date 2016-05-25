package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Constants;
import io.terminus.common.utils.JsonMapper;
import io.terminus.parana.user.impl.dao.UserDao;
import io.terminus.parana.user.model.User;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author  陈增辉, 2016年05月24日
 * 对已有UserDao的扩展, 可以重写UserDao 和MyBatisDao 中已有的方法, 也可以添加新方法
 */
@Repository
public class UserDaoExt extends UserDao {
    private static final String NAMESPACE = "UserExtend.";

    public void updateRoles(List<Long> userIds, List<String> roles){
        sqlSession.update(NAMESPACE + "batchUpdateRoles", ImmutableMap.of("userIds", userIds, "rolesJson", JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(roles)));
    }

    /**
     * 查询所有对象列表
     * @return 所有对象列表
     */
    @Override
    public List<User> listAll(){
        return list((User)null);
    }

    /**
     * 查询对象列表
     * @param user 用户对象
     * @return 查询到的对象列表
     */
    @Override
    public List<User> list(User user){
        return sqlSession.selectList(NAMESPACE + "list", user);
    }

    /**
     * 查询对象列表
     * @param criteria Map查询条件
     * @return 查询到的对象列表
     */
    @Override
    public List<User> list(Map<?, ?> criteria){
        return sqlSession.selectList(NAMESPACE + "list", criteria);
    }

    public Long maxId(){
        return sqlSession.selectOne(NAMESPACE + "maxId");
    }

    public Date minDate() {
        return sqlSession.selectOne(NAMESPACE + "minDate");
    }

    public Paging<User> paging(Integer offset, Integer limit){
        return this.paging(offset, limit, ImmutableMap.of());
    }
    @SuppressWarnings("unchecked")
    public Paging<User> paging(Integer offset, Integer limit, User criteria){
        Map<String, Object> params = Maps.newHashMap();
        if (criteria != null) {    //查询条件不为空
            Map<String, Object> objMap = JsonMapper.nonDefaultMapper().getMapper().convertValue(criteria, Map.class);
            params.putAll(objMap);
        }
        Long total = sqlSession.selectOne(NAMESPACE + "count", criteria);
        if (total <= 0){
            return new Paging<>(0L, Collections.<User>emptyList());
        }
        params.put(Constants.VAR_OFFSET, offset);
        params.put(Constants.VAR_LIMIT, limit);
        List<User> datas = sqlSession.selectList(NAMESPACE + "paging", params);
        return new Paging<>(total, datas);
    }

    public Paging<User> paging(Integer offset, Integer limit, Map<String, Object> criteria) {
        if (criteria == null) {    //如果查询条件为空
            criteria = Maps.newHashMap();
        }
        Long total = sqlSession.selectOne(NAMESPACE + "count", criteria);
        if (total <= 0){
            return new Paging<>(0L, Collections.<User>emptyList());
        }
        criteria.put(Constants.VAR_OFFSET, offset);
        criteria.put(Constants.VAR_LIMIT, limit);
        List<User> datas = sqlSession.selectList(NAMESPACE + "paging", criteria);
        return new Paging<>(total, datas);
    }

    /**
     * 分页查询，offset， limit都丢在map里面
     * @param criteria 所有查询参数
     * @return 查询到的分页对象
     */
    public Paging<User> paging(Map<String, Object> criteria) {
        if (criteria == null) {    //如果查询条件为空
            criteria = Maps.newHashMap();
        }
        Long total = sqlSession.selectOne(NAMESPACE + "count", criteria);
        if (total <= 0){
            return new Paging<>(0L, Collections.<User>emptyList());
        }
        List<User> datas = sqlSession.selectList(NAMESPACE + "paging", criteria);
        return new Paging<>(total, datas);
    }
}
