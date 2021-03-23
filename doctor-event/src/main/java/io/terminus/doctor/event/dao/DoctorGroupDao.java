package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.Dates;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorGroup;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Desc: 猪群卡片表Dao类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Repository
public class DoctorGroupDao extends MyBatisDao<DoctorGroup> {

    /**
     * 过滤掉已关闭的群
     */
    public List<DoctorGroup> findByCurrentBarnId(Long currentBarnId) {
        return getSqlSession().selectList(sqlId("findByCurrentBarnId"), currentBarnId);
    }

    public List<DoctorGroup> findByCurrentBarnIdAndQuantity(Long currentBarnId) {
        return getSqlSession().selectList(sqlId("findByCurrentBarnIdAndQuantity"), currentBarnId);
    }

    public List<DoctorGroup> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    public List<DoctorGroup> fingByStatus(Integer status) {
        return getSqlSession().selectList(sqlId("fingByStatus"), status);
    }

    public List<DoctorGroup> findBySearchDto(DoctorGroupSearchDto searchDto) {
        return getSqlSession().selectList(sqlId("findBySearchDto"), searchDto);
    }

    /**
     * 根据groupCode查询某猪场内的猪群
     * @param farmId    猪场id
     * @param groupCode 猪群号
     * @return 猪群
     */
    public DoctorGroup findByFarmIdAndGroupCode(Long farmId, String groupCode) {
        return getSqlSession().selectOne(sqlId("findByFarmIdAndGroupCode"), ImmutableMap.of("farmId", farmId, "groupCode", groupCode));
    }

    /**
     * 根据时间倒推出当时的猪群（通常只适用于产房群，因为产房只有一个猪群）
     * @param farmId    猪场id
     * @param barnId    猪舍id
     * @param date      日期
     * @return  猪群
     */
    public DoctorGroup findByFarmIdAndBarnIdAndDate(Long farmId, Long barnId, Date date) {
        if (farmId == null || barnId == null || date == null) {
            return null;
        }
        return getSqlSession().selectOne(sqlId("findByFarmIdAndBarnIdAndDate"), ImmutableMap.of("farmId", farmId, "barnId", barnId, "date", date));

    }

    /**
     * 查当前产房的唯一猪群
     */
    public DoctorGroup findCurrentFarrowByBarnId(Long barnId) {
        return getSqlSession().selectOne(sqlId("findCurrentFarrowByBarnId"),
                ImmutableMap.of("barnId", barnId, "status", DoctorGroup.Status.CREATED.getValue()));
    }

    /**
     * 更改猪场名
     * @param farmId 需要更改的猪场id
     * @param farmName 新的猪场名
     */
    public void updateFarmName(Long farmId, String farmName) {
        getSqlSession().update(sqlId("updateFarmName"), ImmutableMap.of("farmId", farmId, "farmName", farmName));
    }
    /**
     * 根据猪群的建群时间和关群时间来筛选符合条件的猪群
     */

    public List<DoctorGroup> findGroupId(Long farmId, Date startAt, Date endAt) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("farmId", farmId);
        map.put("startAt", startAt);
        map.put("endAt", endAt);

        return getSqlSession().selectList(sqlId("findGroupId"), map);
    }

    public List<DoctorGroup> findGroupIdByBranId(Long farmId, Long barnId) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("farmId", farmId);
        map.put("currentBarnId", barnId);
        return getSqlSession().selectList("findGroupByBarnId", map);
    }

    /**
     * 查找需要生成日统计的猪群
     * @param farmId
     * @param date
     * @return
     */
    public List<DoctorGroup> findByFarmIdAndDate(Long farmId, Date date) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndDate"), ImmutableMap.of("farmId", farmId, "openAt"
                , Dates.endOfDay(date), "colseAt", Dates.startOfDay(date)));
    }

    /**
     * 更新当前猪舍下的猪群的当前猪舍名
     * @param currentBarnId 当前猪舍id
     * @param currentBarnName 新猪舍名
     * @return
     */
    public Boolean updateCurrentBarnName(Long currentBarnId, String currentBarnName) {
        return getSqlSession().update(sqlId("updateCurrentBarnName"),
                ImmutableMap.of("currentBarnId", currentBarnId, "currentBarnName", currentBarnName)) == 1;
    }

    /**
     * 更新猪舍下所有猪群下管理员
     * @param currentBarnId 当前猪舍名
     * @param staffId 新管理员id
     * @param staffName 新管理员名
     * @return
     */
    public Boolean updateStaffName(Long currentBarnId, Long staffId, String staffName) {
        return getSqlSession().update(sqlId("updateStaffName"),
                ImmutableMap.of("currentBarnId", currentBarnId, "staffId", staffId, "staffName", staffName)) == 1;
    }

    /**
     * 查询猪场与当前开启猪群数量映射
     * @return
     */
    public List<Map<String, Object>> findFarmToGroupCount() {
        return getSqlSession().selectList(sqlId("findFarmToGroupCount"));
    }
    /**
     * 查询猪群
     * @param farmId 猪场列表
     * @param pigType 猪群类型
     * @param status 猪群状态
     * @return 猪群列表
     */
    public List<DoctorGroup> findByFarmIdAndPigTypeAndStatus(Long farmId, Integer pigType, Integer status) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndPigTypeAndStatus"), ImmutableMap.of("farmId"
                , farmId, "pigType", pigType, "status", status));
    }

    /**
     * 根据猪场id和外部id查询猪群
     * @param farmId 猪场id
     * @param outId 外部id
     * @return 猪群
     */
    public DoctorGroup findByFarmAndOutId(Long farmId, String outId) {
        return getSqlSession().selectOne(sqlId("findByFarmAndOutId"), ImmutableMap.of("farmId", farmId, "outId", outId));
    }

    /**
     * 删除猪场下的所有猪群
     * @param farmId 猪场id
     */
    public void deleteByFarmId(Long farmId) {
        deleteByFarmId(farmId, Lists.newArrayList());
    }

    public void deleteByFarmId(Long farmId, List<Integer> pigTypes)  {
        getSqlSession().delete(sqlId("deleteByFarmId"), ImmutableMap.of("farmId", farmId, "pigTypes", pigTypes));
    }

    /**
     * 查询指定时间处于开启状态的猪群
     * @param date 日期 yyyy-MM-dd
     * @return 猪群列表
     */
    public List<DoctorGroup> listOpenGroupsBy(String date){
        return getSqlSession().selectList(sqlId("listOpenGroupsBy"), date);
    }

    public List<DoctorGroup> findGroupByCurrentBarnIdFuzzy(Long currentBarnId,String groupCode) {
        Map map = Maps.newHashMap();
        map.put("currentBarnId",currentBarnId);
        map.put("groupCode",groupCode);
        return getSqlSession().selectList(sqlId("findGroupByCurrentBarnIdFuzzy"), map);
    }

    public Long findGroupQuantityByGroupCode(String groupCode){
        return getSqlSession().selectOne(sqlId("findGroupQuantityByGroupCode"),groupCode);
    }
}
