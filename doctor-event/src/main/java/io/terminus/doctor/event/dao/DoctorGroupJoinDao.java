package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.search.SearchedGroup;
import org.springframework.stereotype.Repository;

/**
 * Desc: 猪群join
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/12/22
 */
@Repository
public class DoctorGroupJoinDao extends MyBatisDao<SearchedGroup> {

    public Long getPigCount(DoctorGroupSearchDto groupSearchDto) {
        return getSqlSession().selectOne(sqlId("getPigCount"), groupSearchDto);
    }

    /**获取断奶仔猪数
     * @param groupSearchDto
     * @return
     */
    public Long getWeanCount(DoctorGroupSearchDto groupSearchDto) {
        return getSqlSession().selectOne(sqlId("getWeanCount"), groupSearchDto);
    }
}
