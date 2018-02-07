package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.search.DoctorGroupCountDto;
import io.terminus.doctor.event.dto.search.SearchedGroup;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    /**
     * 获取猪场各个类型猪数量
     * @param farmId 猪场id
     * @return
     */
    public DoctorGroupCountDto findGroupCount(Long farmId) {
        return getSqlSession().selectOne(sqlId("findGroupCount"), farmId);
    }

    /**
     * 查询猪舍各个已新建猪群日龄列表
     * @param barnId 猪舍id
     * @return 日龄列表
     */
    public List<Integer> dayAgeListForBarn(Long barnId) {
        return getSqlSession().selectList(sqlId("dayAgeListForBarn"), barnId);
    }
}
