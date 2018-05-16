package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.enums.WarehouseMaterialApplyType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroup;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroupDetail;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseMaterialApplyDao extends MyBatisDao<DoctorWarehouseMaterialApply> {


    public List<DoctorWarehouseMaterialApply> listAndOrderByHandleDate(DoctorWarehouseMaterialApply criteria, Integer limit) {

        Map<String, Object> params = Maps.newHashMap();
        if (criteria != null) {
            Map<String, Object> objMap = (Map) JsonMapper.nonDefaultMapper().getMapper().convertValue(criteria, Map.class);
            params.putAll(objMap);
        }
        if (null != limit)
            params.put("limit", limit);

        return sqlSession.selectList(sqlId("listAndOrderByHandleDate"), params);
    }


    public List<DoctorWarehouseMaterialApply> advList(Map<String, Object> criteria) {
        return sqlSession.selectList(sqlId("advList"), criteria);
    }

    /**
     * @param materialHandleId
     * @return 如果是猪舍领用，返回猪舍领用
     * 如果是猪群领用，返回猪群领用那一条，屏蔽猪舍领用那一条
     * 如果没有，返回null
     */
    public DoctorWarehouseMaterialApply findMaterialHandle(Long materialHandleId) {
        List<DoctorWarehouseMaterialApply> applies = this.list(DoctorWarehouseMaterialApply.builder()
                .materialHandleId(materialHandleId)
                .build());
        if (applies.isEmpty())
            return null;


        Optional<DoctorWarehouseMaterialApply> groupApply = applies.stream()
                .filter(a -> a.getApplyType().intValue() == WarehouseMaterialApplyType.GROUP.getValue()
                        || a.getApplyType().intValue() == WarehouseMaterialApplyType.SOW.getValue()
                ).findAny();

        return groupApply.orElse(applies.get(0));

//        return sqlSession.selectOne("findByMaterialHandle", materialHandleId);
    }

    /**
     * 删除猪群或母猪领用
     *
     * @param materialHandleId
     */
    public void deleteGroupApply(Long materialHandleId) {
        this.sqlSession.delete(this.sqlId("deleteGroupApply"), materialHandleId);
    }

    /**
     * 更新猪舍领用
     *
     * @param materialHandleId
     */
    public void updateBarnApply(Long materialHandleId, DoctorWarehouseMaterialApply groupApply) {

        Map<String, Object> params = new HashMap<>();
        params.put("settlementDate", groupApply.getSettlementDate());
        params.put("quantity", groupApply.getQuantity());
        params.put("handleDate", groupApply.getApplyDate());
        params.put("year", groupApply.getApplyYear());
        params.put("month", groupApply.getApplyMonth());
        params.put("barnId", groupApply.getPigBarnId());
        params.put("barnName", groupApply.getPigBarnName());
        params.put("pigType", groupApply.getPigType());
        params.put("staffId", groupApply.getApplyStaffId());
        params.put("staffName", groupApply.getApplyStaffName());
        params.put("materialHandleId", materialHandleId);

        this.sqlSession.update(this.sqlId("updateBarnApply"), params);
    }

    /**
     * 猪群饲料指定时间段内领用和
     *
     * @param groupId 猪群id
     * @param startAt 开始时间 yy-MM-dd
     * @param endAt   结束时间
     * @return 和
     */
    public Double sumGroupFeedApply(Long groupId, String startAt, String endAt) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("groupId", groupId);
        map.put("startAt", startAt);
        map.put("endAt", endAt);
        return getSqlSession().selectOne(sqlId("sumGroupFeedApply"), map);
    }


    public void deleteByMaterialHandle(Long materialHandleId) {
        getSqlSession().delete("deleteByMaterialHandle", materialHandleId);
    }

    public void reverseSettlement(Long orgId, Date settlementDate) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("orgId", orgId);
        map.put("settlementDate", settlementDate);
        this.sqlSession.update(this.sqlId("reverseSettlement"), map);
    }

    public void updateUnitPriceAndAmountByMaterialHandle(Long materialHandleId, BigDecimal unitPrice, BigDecimal amount) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("materialHandleId", materialHandleId);
        map.put("unitPrice", unitPrice);
        map.put("amount", amount);
        this.sqlSession.update(this.sqlId("updateUnitPriceAndAmountByMaterialHandle"), map);
    }

    /**
     * 猪群领用报表
     *
     * @param farmId
     * @param pigType
     * @param pigName
     * @param pigGroupName
     * @param skuType
     * @param skuName
     * @param openAt
     * @param closeAt
     * @return
     */
    public List<DoctorWarehouseMaterialApplyPigGroup> selectPigGroupApply(Integer farmId, String pigType, String pigName, String pigGroupName,

                                                                                      Integer skuType, String skuName, String openAt, String closeAt){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date openAt1 = null;
        Date closeAt1 = null;
        try {
            if(openAt != null){
                openAt1 = sdf.parse(openAt);
            }
            if(closeAt != null) {
                closeAt1 = sdf.parse(closeAt);
            }
        }catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String,Object> map = Maps.newHashMap();
        map.put("farmId",farmId);
        map.put("pigType",pigType);
        map.put("pigName",pigName);
        map.put("pigGroupName",pigGroupName);
        map.put("skuType",skuType);
        map.put("skuName",skuName);
        map.put("openAt",openAt1);
        map.put("closeAt",closeAt1);
        return this.sqlSession.selectList(this.sqlId("selectPigGroupApply"),map);

    }

    /**
     * 猪舍领用报表
     *
     * @param criteria
     * @return
     */
    public List<Map> piggeryReport(DoctorWarehouseMaterialApply criteria) {
        Map<String, Object> params = Maps.newHashMap();
        if (criteria != null) {
            params.put("farmId", criteria.getFarmId());
            params.put("applyYear", criteria.getApplyYear());
            params.put("applyMonth", criteria.getApplyMonth());
            params.put("pigBarnId", criteria.getPigBarnId());
            params.put("pigType", criteria.getPigType());
            params.put("type", criteria.getType());
            params.put("materialName", criteria.getMaterialName());
        }
        return sqlSession.selectList(sqlId("piggeryReport"), params);
    }

    /**
     * 猪舍领用详情
     *
     * @param criteria
     * @return
     */
    public List<Map> piggeryDetails(DoctorWarehouseMaterialApply criteria) {
        Map<String, Object> params = Maps.newHashMap();
        if (criteria != null) {
            params.put("applyYear", criteria.getApplyYear());
            params.put("applyMonth", criteria.getApplyMonth());
            params.put("pigBarnId", criteria.getPigBarnId());
            params.put("materialName", criteria.getMaterialName());
//            Map<String, Object> objMap = (Map) JsonMapper.nonDefaultMapper().getMapper().convertValue(criteria, Map.class);
//            params.putAll(objMap);
        }
        return sqlSession.selectList(sqlId("piggeryDetails"), params);
    }

    /**
     * 猪群领用报表详情
     *
     * @param pigGroupId
     * @param skuId
     * @return
     */
    public List<DoctorWarehouseMaterialApplyPigGroupDetail> selectPigGroupApplyDetail(Long pigGroupId, Long skuId) {
        Map<String, Long> map = Maps.newHashMap();
        map.put("pigGroupId", pigGroupId);
        map.put("skuId", skuId);
        return this.sqlSession.selectList(this.sqlId("selectPigGroupApplyDetail"), map);
    }
}

