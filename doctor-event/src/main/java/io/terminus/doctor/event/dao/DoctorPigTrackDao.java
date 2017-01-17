package io.terminus.doctor.event.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorSuggestPig;
import io.terminus.doctor.event.dto.DoctorSuggestPigSearch;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigStatusCount;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigTrackDao extends MyBatisDao<DoctorPigTrack>{

    /**
     * 更新公猪的当前配种次数
     * @param boarId  公猪id
     * @param currentParity  当前配种次数
     */
    public void updateBoarCurrentParity(Long boarId, Integer currentParity) {
        getSqlSession().update(sqlId("updateBoarCurrentParity"), ImmutableMap.of("boarId", boarId, "currentParity", currentParity));
    }

    public void deleteByFarmId(Long farmId) {
        getSqlSession().delete(sqlId("deleteByFarmId"), farmId);
    }

    /**
     * 查询猪舍内猪状态
     * @param barnId 猪舍id
     * @return 猪状态
     */
    public List<Integer> findStatusByBarnId(Long barnId) {
        return getSqlSession().selectList(sqlId("findStatusByBarnId"), barnId);
    }

    /**
     * 根据猪舍id查询猪跟踪
     * @param barnId 猪舍id
     * @return 猪跟踪
     */
    public List<DoctorPigTrack> findByBarnId(Long barnId) {
        return getSqlSession().selectList(sqlId("findByBarnId"), barnId);
    }

    /**
     * 根据猪场id查询猪跟踪
     * @param farmId 猪场id
     * @return 猪跟踪
     */
    public List<DoctorPigTrack> findByFarmIdAndStatus(Long farmId, Integer status) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndStatus"), ImmutableMap.of("farmId", farmId, "status", status));
    }

    /**
     * pigIds 获取对应的PigTrack
     * @param pigIds
     * @return
     */
    public List<DoctorPigTrack> findByPigIds(List<Long> pigIds){
        return this.getSqlSession().selectList(sqlId("findByPigIds"),pigIds);
    }

    public DoctorPigTrack findByPigId(Long pigId){
        List<DoctorPigTrack> pigs = findByPigIds(Lists.newArrayList(pigId));
        if (pigs != null && pigs.size() > 0) {
            return pigs.get(0);
        }
        return null;
    }

    public DoctorPigTrack findByEventId(Long relEventId){
        return this.getSqlSession().selectOne(sqlId("findByEventId"), relEventId);
    }

    /**
     * 猪的当前最大的id, 这个是dump搜素引擎用的
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
     * @param lastId lastId 最大的猪id
     * @param since  起始更新时间
     * @param limit  个数
     */
    public List<DoctorPigTrack> listSince(Long lastId, String since, int limit) {
        return getSqlSession().selectList(sqlId("listSince"),
                ImmutableMap.of("lastId", lastId, "limit", limit, "since", since));
    }

    /**
     * 更新 extra_message 信息
     * @param pigTrack
     * @return
     */
    public int updateExtraMessage(DoctorPigTrack pigTrack) {
        return getSqlSession().update("updateExtraMessage", pigTrack);
    }

    /**
     * 统计不同的状态母猪数量信息(统计一个猪场的数据)
     * @return
     */
    public List<DoctorPigStatusCount> countPigTrackByStatus(Long farmId){
        return getSqlSession().selectList(sqlId("countPigTrackByStatus"), farmId);
    }

    /**
     * 根据猪舍ids统计猪的数量
     * @param barnIds 猪舍ids
     * @return 猪的数量
     */
    public Long countByBarnIds(List<Long> barnIds) {
        return this.getSqlSession().selectOne(sqlId("countByPigTypes"), ImmutableMap.of(barnIds, barnIds));
    }

    /**
     * 根据猪群id查询哺乳母猪的跟踪
     * @param groupId 猪群id
     * @return 母猪跟踪
     */
    public List<DoctorPigTrack> findFeedSowTrackByGroupId(Long groupId) {
        return getSqlSession().selectList(sqlId("findFeedSowTrackByGroupId"), ImmutableMap.of("groupId", groupId, "status", PigStatus.FEED.getKey()));
    }

    /**
     * 根据事件suggestpig
     * @param suggestPigSearch 查询条件
     * @return
     */
    public List<DoctorSuggestPig> suggestPigs(DoctorSuggestPigSearch suggestPigSearch) {
        return getSqlSession().selectList(sqlId("suggestPigs"), suggestPigSearch);
    }
}
