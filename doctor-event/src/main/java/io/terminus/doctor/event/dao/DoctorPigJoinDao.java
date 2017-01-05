package io.terminus.doctor.event.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.Constants;
import io.terminus.doctor.event.dto.search.SearchedPig;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Desc: 猪join
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/22
 */
@Repository
public class DoctorPigJoinDao extends MyBatisDao<SearchedPig> {

    public Paging<SearchedPig> pigPagingWithJoin(Map<String, Object> params, Integer offset, Integer limit) {
        if (params == null) {    //如果查询条件为空
            params = Maps.newHashMap();
        }

        Long total = sqlSession.selectOne(sqlId(COUNT), params);
        if (total <= 0){
            return new Paging<>(0L, Collections.emptyList());
        }
        params.put(Constants.VAR_OFFSET, offset);
        params.put(Constants.VAR_LIMIT, limit);
        List<SearchedPig> datas = sqlSession.selectList(sqlId(PAGING), params);
        return new Paging<>(total, datas);
    }
}
