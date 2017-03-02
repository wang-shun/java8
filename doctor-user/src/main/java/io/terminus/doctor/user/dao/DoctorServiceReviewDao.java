package io.terminus.doctor.user.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.MapBuilder;
import io.terminus.doctor.user.model.DoctorServiceReview;
import io.terminus.doctor.user.model.DoctorServiceReviewExt;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Desc: 用户服务审批表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Repository
public class DoctorServiceReviewDao extends MyBatisDao<DoctorServiceReview> {
    /**
     * 修改服务状态
     * @param userId 申请服务的用户的id
     * @param reviewerId 审核人id
     * @param type 服务类型, 参见枚举 ServiceReview.Type
     * @param status 相应服务的新状态
     * @return
     */
    public boolean updateStatus(Long userId, Long reviewerId, DoctorServiceReview.Type type, DoctorServiceReview.Status status){
        return sqlSession.update(sqlId("updateStatus"), ImmutableMap.of("userId", userId, "reviewerId", reviewerId, "type", type.getValue(), "status", status.getValue())) == 1;
    }
    public boolean updateStatus(Long userId, DoctorServiceReview.Type type, DoctorServiceReview.Status status){
        return sqlSession.update(sqlId("updateStatus"), ImmutableMap.of("userId", userId, "type", type.getValue(), "status", status.getValue())) == 1;
    }

    /**
     * 为用户初始化所有服务的数据, 状态status都是未开通0
     * @param userId 用户id
     * @param userMobile 用户手机号,冗余字段,可以为空
     * @return 插入的数据的行数, 理论上应该等于枚举 ServiceReview.Type 的数量
     */
    public boolean initData(Long userId, String userMobile, String realName){
        int[] types = Stream.of(DoctorServiceReview.Type.values()).mapToInt(DoctorServiceReview.Type::getValue).toArray();
        Map<Object, Object> param = MapBuilder.newHashMap().put("userId", userId, "types", types, "userMobile", userMobile, "realName", realName).map();
        return sqlSession.insert(sqlId("initData"), param) == types.length;
    }

    public List<DoctorServiceReview> findByUserId(Long userId){
        return sqlSession.selectList(sqlId("findByUserId"), userId);
    }

    public DoctorServiceReview findByUserIdAndType(Long userId, DoctorServiceReview.Type type){
        return sqlSession.selectOne(sqlId("findByUserIdAndType"), ImmutableMap.of("userId", userId, "type", type.getValue()));
    }

    public Paging<DoctorServiceReviewExt> pagingExt(Map<String, Object> criteria){
        if (criteria == null) {    //如果查询条件为空
            criteria = Maps.newHashMap();
        }

        Long total = sqlSession.selectOne(sqlId(COUNT), criteria);
        if (total <= 0){
            return new Paging<>(0L, Collections.<DoctorServiceReviewExt>emptyList());
        }

        List<DoctorServiceReviewExt> datas = sqlSession.selectList(sqlId(PAGING), criteria);
        return new Paging<>(total, datas);
    }
}
