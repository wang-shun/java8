package io.terminus.doctor.event.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.common.utils.PostRequest;
import io.terminus.doctor.event.dto.DoctorSuggestPig;
import io.terminus.doctor.event.dto.DoctorSuggestPigSearch;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPigStatusCount;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-04-25
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorPigTrackDao extends MyBatisDao<DoctorPigTrack>{

    public Boolean update( DoctorPigTrack t) {

        //通知物联网接口(孔景军)
            /*
            当母猪状态变更为进场、哺乳，配种、断奶、妊娠检查阴性、妊娠检查返情、妊娠检查流产时，需调用物联网提供的接口，以此通知网联网。
             */
        if(t.getStatus() != null && t.getPigType() == 1){
            if(t.getStatus() == 1){
                Map<String,String> params = Maps.newHashMap();
                params.put("pigId",t.getPigId().toString());
                params.put("newStatus","1");
                new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
            }
            if(t.getStatus() == 3){
                Map<String,String> params = Maps.newHashMap();
                params.put("pigId",t.getPigId().toString());
                params.put("newStatus","3");
                new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
            }
            if(t.getStatus() == 4){
                Map<String,String> params = Maps.newHashMap();
                params.put("pigId",t.getPigId().toString());
                params.put("newStatus","4");
                new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
            }
            if(t.getStatus() == 7){
                Map<String,String> params = Maps.newHashMap();
                params.put("pigId",t.getPigId().toString());
                params.put("newStatus","7");
                new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
            }
            if(t.getStatus() == 8){
                Map<String,String> params = Maps.newHashMap();
                params.put("pigId",t.getPigId().toString());
                params.put("newStatus","8");
                new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
            }
            if(t.getStatus() == 9){
                Map<String,String> params = Maps.newHashMap();
                params.put("pigId",t.getPigId().toString());
                params.put("newStatus","9");
                new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
            }
            if(t.getStatus() == 5){
                //得到母猪的最后一次妊娠检查事件
                Integer status = this.sqlSession.selectOne(this.sqlId("getLastEvent"), t.getPigId());
                if(status != null){
                    if(status == 2){
                        Map<String,String> params = Maps.newHashMap();
                        params.put("pigId",t.getPigId().toString());
                        params.put("newStatus","51");
                        new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
                    }
                    if(status == 3){
                        Map<String,String> params = Maps.newHashMap();
                        params.put("pigId",t.getPigId().toString());
                        params.put("newStatus","52");
                        new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
                    }
                    if(status == 4){
                        Map<String,String> params = Maps.newHashMap();
                        params.put("pigId",t.getPigId().toString());
                        params.put("newStatus","53");
                        new PostRequest().postRequest("/api/iot/pig/sow-status-change",params);
                    }
                }
            }
        }
        //当母猪下的仔猪数量发生变动时，需要调用物联网提供的接口，以此通知物联网。(孔景军)
        if(t.getUnweanQty() != null && t.getPigType() == 1){
            Map<String,String> params = Maps.newHashMap();
            params.put("pigId",t.getPigId().toString());
            params.put("newQuantity",t.getUnweanQty().toString());
            new PostRequest().postRequest("/api/iot/pig/ sow-stock-change",params);
        }
        return this.sqlSession.update(this.sqlId("update"), t) == 1;
    }

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
     * 获取猪所在的最新当前猪舍
     *
     * @param pigId
     * @return
     * */
    public DoctorPigTrack queryLastEventBarnName(Long pigId) {
        return this.getSqlSession().selectOne(sqlId("queryLastEventBarnName"), pigId);
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

    /**
     * 母猪详情页的导出
     * @param farmId
     * @param pigId
     * @param eventSize
     * @return
     */
    public List<Map> findSowPigDetailExpotr(Long farmId, Long pigId, Integer eventSize){
        return this.getSqlSession().selectList(sqlId("findSowPigDetailExpotr"),ImmutableMap.of("farmId",farmId,"pigId",pigId,"eventSize",eventSize));
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
    public List<DoctorPigTrack> findFeedSowTrackByGroupId(Long farmId, Long groupId) {
        return getSqlSession().selectList(sqlId("findFeedSowTrackByGroupId"), ImmutableMap.of("farmId", farmId, "groupId", groupId, "status", PigStatus.FEED.getKey()));
    }

    /**
     * 根据事件suggestpig
     * @param suggestPigSearch 查询条件
     * @return
     */
    public List<DoctorSuggestPig> suggestPigs(DoctorSuggestPigSearch suggestPigSearch) {
        return getSqlSession().selectList(sqlId("suggestPigs"), suggestPigSearch);
    }


    /**
     * 更新当前猪舍下猪的猪舍名
     * @param currentBarnId 当前猪舍id
     * @param currentBarnName 新猪舍名
     * @return
     */
    public Boolean updateCurrentBarnName(Long currentBarnId, String currentBarnName){
        return getSqlSession().update(sqlId("updateCurrentBarnName"),
                ImmutableMap.of("currentBarnId", currentBarnId, "currentBarnName", currentBarnName)) == 1;
    }

    /**
     * 获取猪的当前状态
     * @param list 猪ids
     * @return 返回的track中只包含pigId 和 status 字段
     */
    public List<DoctorPigTrack> queryCurrentStatus(List<Long> list) {
        return getSqlSession().selectList(sqlId("queryCurrentStatus"), list);
    }

    /**
     * 获取猪舍下各种状态猪
     * @param barnId
     * @return
     */
    public List<DoctorPigStatusCount> getStatusPigForBarn(Long barnId) {
        return getSqlSession().selectList(sqlId("getStatusPigForBarn"), barnId);
    }

    /**
     * 根据猪场id查询猪跟踪
     * @param barnId 猪场id
     * @return 猪跟踪
     */
    public List<DoctorPigTrack> findByBarnIdAndStatus(Long barnId, Integer status) {
        return getSqlSession().selectList(sqlId("findByBarnIdAndStatus"), ImmutableMap.of("barnId", barnId, "status", status));
    }

    public Boolean flushCurrentParity(Long pigId, Integer parity) {
        return getSqlSession().update(sqlId("flushCurrentParity"), ImmutableMap.of("pigId", pigId, "parity", parity)) == 1;
    }

    /**
     * 通过猪场id查猪场存在的猪
     * @param farmId
     * @return
     */
    public List<Long> selectPigIds(Long farmId) {
        return getSqlSession().selectList(sqlId("selectPigIds"),farmId);
    }

    public Long queryCurrentEventId(Long pigId){
        return getSqlSession().selectOne(sqlId("queryCurrentEventId"), pigId);
    }
}
