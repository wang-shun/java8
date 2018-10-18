package io.terminus.doctor.event.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.common.utils.PostRequest;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Desc: 猪群卡片明细表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorGroupTrackDao extends MyBatisDao<DoctorGroupTrack> {

    /**
     * 根据猪群id查询猪群Track信息
     * @param groupId   猪群id
     * @return
     */
    public DoctorGroupTrack findByGroupId(Long groupId) {
        return getSqlSession().selectOne("findGroupTrackByGroupId", groupId);
    }
    @Override
    public Boolean update(DoctorGroupTrack t) {
        if(t.getQuantity() != null){
            /*当猪群的存栏数量发生变动时，通知物联网接口（孔景军）*/
            Map<String,String> param = Maps.newHashMap();
            param.put("pigGroupId",t.getGroupId().toString());
            param.put("newQuantity",t.getQuantity().toString());
            new PostRequest().postRequest("api/iot/pig/ group-stock-change",param);
        }
        return this.sqlSession.update(this.sqlId("update"), t) == 1;
    }
    /**
     * 重新计算下日龄
     * @param groupIds 猪群ids
     */
    public void incrDayAge(List<Long> groupIds) {
        getSqlSession().update(sqlId("incrDayAge"), groupIds);
    }

    /**
     * 猪群的当前最大的id, 这个是dump搜素引擎用的
     *
     * @return 当前最大的id
     */
    public Long maxId() {
        Long count = getSqlSession().selectOne(sqlId("maxId"));
        return MoreObjects.firstNonNull(count, 0L);
    }

    /**
     * 查询id小于lastId内且更新时间大于since的limit个猪, 这个是dump搜素引擎用的
     *
     * @param lastId lastId 最大的猪群id
     * @param since  起始更新时间
     * @param limit  个数
     */
    public List<DoctorGroupTrack> listSince(Long lastId, String since, int limit) {
        return getSqlSession().selectList(sqlId("listSince"),
                ImmutableMap.of("lastId", lastId, "limit", limit, "since", since));
    }

    public void deleteByGroupId(Long groupId) {
        getSqlSession().delete("deleteGroupTrackByGroupId", groupId);
    }

    /**
     * 根据查询日期统计每个猪场的待出栏猪只数
     * @param criteria 查询条件
     * @return
     */
    public List<Map<String, Object>> queryFattenOutBySumAt(Map<String, Object> criteria){
        return getSqlSession().selectList(sqlId("queryFattenOutBySumAt"), criteria);
    }

    /**
     * 查询猪群关联的track列表
     * @param list 猪群ids
     * @return
     */
    public List<DoctorGroupTrack> findsByGroups(List<Long> list) {
        return getSqlSession().selectList(sqlId("findsByGroups"), list);
    }
    /**
     * 统计指定猪群的存栏之和
     * @param list
     * @return
     */
    public Integer sumPigletCount(List<Long> list) {
        return getSqlSession().selectOne(sqlId("sumPigletCount"), list);
    }

    public Integer findSex(Long groupId){
        Map<String,Object> map = new HashMap<>();
        map.put("groupId", groupId);
        return getSqlSession().selectOne(sqlId("findSex"), map);
    }
}
