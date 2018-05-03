package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.warehouseV2.CompanyReportDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSettlementService;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 13:22:27
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseMaterialHandleDao extends MyBatisDao<DoctorWarehouseMaterialHandle> {

    /**
     * 支持bigType参数，对多个type的or查询
     * 支持startDate和endDate，对handle_date的范围查询
     *
     * @param offset
     * @param limit
     * @param criteria
     * @return
     */
    public Paging<DoctorWarehouseMaterialHandle> advPaging(Integer offset, Integer limit, Map<String, Object> criteria) {

        if (criteria == null) {
            criteria = Maps.newHashMap();
        }
        Long total = (Long) this.sqlSession.selectOne(this.sqlId("advCount"), criteria);
        if (total.longValue() <= 0L) {
            return new Paging(0L, Collections.emptyList());
        } else {
            ((Map) criteria).put("offset", offset);
            ((Map) criteria).put("limit", limit);
            List<DoctorWarehouseMaterialHandle> datas = this.sqlSession.selectList(this.sqlId("advPaging"), criteria);
            return new Paging(total, datas);
        }
    }


    /**
     * 支持bigType参数，对多个type的or查询
     * 支持startDate和endDate，对handle_date的范围查询
     *
     * @param criteria
     * @return
     */
    public List<DoctorWarehouseMaterialHandle> advList(Map<?, ?> criteria) {

        return this.sqlSession.selectList(this.sqlId("advList"), criteria);
    }

    public List<DoctorWarehouseMaterialHandle> findByStockHandle(Long stockHandleId) {
        return this.list(DoctorWarehouseMaterialHandle.builder()
                .deleteFlag(WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue())
                .stockHandleId(stockHandleId)
                .build());
    }


    public List<DoctorWarehouseMaterialHandle> findAfter(Long warehouseId, Long skuId, Date handleDate, boolean includeHandleDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("skuId", skuId);
        criteria.put("handleDate", handleDate);
        criteria.put("includeHandleDate", includeHandleDate);
        return this.sqlSession.selectList(this.sqlId("findAfterByDate"), criteria);
    }

    /**
     * 根据单据明细统计历史某一个节点的库存量
     *
     * @return
     */
    public BigDecimal getHistoryStock(Long warehouseId, Long skuId, Date handleDate, boolean include) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("skuId", skuId);
        criteria.put("handleDate", handleDate);
        criteria.put("include", include);

        return this.sqlSession.selectOne(this.sqlId("countHistoryStock"), criteria);
    }

    /**
     * 获取会计年月内的明细单据
     *
     * @param orgId          公司id
     * @param settlementDate 会计年月
     * @return
     */
    public List<DoctorWarehouseMaterialHandle> findByOrgAndSettlementDate(Long orgId, Date settlementDate) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("orgId", orgId);
        criteria.put("settlementDate", settlementDate);

        return this.sqlSession.selectList(this.sqlId("findByOrgAndSettlementDate"), criteria);
    }

    /**
     * 获取最早一笔退料入库单据的事件日期
     *
     * @return
     */
    public Date findFirstRefundHandleDate(List<Long> outMaterialHandleIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("outMaterialHandleIds", outMaterialHandleIds);
        return this.sqlSession.selectOne(this.sqlId("findFirstRefundHandleDate"), params);
    }

    @Deprecated
    public void reverseSettlement(Long farmId, Integer year, Integer month) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("farmId", farmId);
        criteria.put("year", year);
        criteria.put("month", month);

        this.sqlSession.update(this.sqlId("reverseSettlement"), criteria);
    }

    /**
     * 重置单价和金额
     *
     * @param orgId          公司id
     * @param settlementDate 会计年月
     */
    public void reverseSettlement(Long orgId, Date settlementDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("orgId", orgId);
        criteria.put("settlementDate", settlementDate);

        this.sqlSession.update(this.sqlId("reverseSettlementByOrg"), criteria);
    }

    /**
     * 通过明细单据统计某个会计年月的发生额和发生量
     *
     * @param warehouseId    仓库id
     * @param settlementDate 会计年月
     * @return
     */
    @Deprecated
    public AmountAndQuantityDto findBalanceByAccountingDate(Long warehouseId, Date settlementDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("settlementDate", settlementDate);

        Map<String, BigDecimal> result = this.sqlSession.selectOne(this.sqlId("findBalanceByAccountingDate"), criteria);
        return new AmountAndQuantityDto((result.get("amount")), result.get("quantity"));
    }

    /**
     * 通过明细单据统计某个会计年月的发生额和发生量
     *
     * @param warehouseId    仓库id
     * @param skuId          物料id
     * @param settlementDate 会计年月
     * @return amount and quantity
     */
    public AmountAndQuantityDto findBalanceBySettlementDate(Long warehouseId, Long skuId, Date settlementDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("skuId", skuId);
        criteria.put("settlementDate", settlementDate);

        Map<String, BigDecimal> result = this.sqlSession.selectOne(this.sqlId("findBalanceBySettlementDate"), criteria);
        return new AmountAndQuantityDto((result.get("amount")), result.get("quantity"));
    }

    /**
     * 获取指定明细获取上一笔明细
     *
     * @param materialHandle
     * @return
     */
    public DoctorWarehouseMaterialHandle findPrevious(DoctorWarehouseMaterialHandle materialHandle, WarehouseMaterialHandleType handleType) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", materialHandle.getWarehouseId());
        criteria.put("materialHandleId", materialHandle.getId());
        criteria.put("handleDate", materialHandle.getHandleDate());
        if (null != handleType)
            criteria.put("type", handleType.getValue());

        List<DoctorWarehouseMaterialHandle> materialHandles = this.sqlSession.selectList(this.sqlId("findPrevious"), criteria);
        if (materialHandles.isEmpty())
            return null;

        return materialHandles.get(0);
    }

    public Integer getWarehouseMaterialHandleCount(Long warehouseId) {
        Map<String, String> m = new HashMap<>();
        m.put("id", warehouseId.toString());
        Integer count = this.sqlSession.selectOne(this.sqlId("getWarehouseMaterialHandleCount"), warehouseId);
        return count;
    }

    /**
     * 统计出库单据已退数量
     *
     * @return
     */
    public BigDecimal countQuantityAlreadyRefund(Long materialHandleId) {
        return this.sqlSession.selectOne(this.sqlId("countQuantityAlreadyRefund"), materialHandleId);
    }

    public void updateHandleDateAndSettlementDate(Calendar handleDate, Date settlementDate, Long materialHandleId) {
        Map<String, Object> params = new HashMap<>();
        params.put("materialHandleId", materialHandleId);
        params.put("handleDate", handleDate.getTime());
        params.put("year", handleDate.get(Calendar.YEAR));
        params.put("month", handleDate.get(Calendar.MONTH) + 1);
        params.put("settlementDate", settlementDate);

        this.sqlSession.update(this.sqlId("updateHandleDateAndSettlementDate"), params);
    }

    //得到领料出库的数量
    public BigDecimal findLibraryById(Long id) {
        BigDecimal quantity = this.sqlSession.selectOne(this.sqlId("findLibraryById"), id);
        return quantity;
    }


    //得到在此之前退料入库的数量和
    public BigDecimal findRetreatingById(DoctorWarehouseMaterialHandle materialHandle) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("relMaterialHandleId", materialHandle.getRelMaterialHandleId());
        criteria.put("settlementDate", materialHandle.getSettlementDate());
        BigDecimal quantity = this.sqlSession.selectOne(this.sqlId("findRetreatingById"), criteria);
        return quantity;
    }

    public DoctorWarehouseMaterialHandle findByStockHandleId(Long id) {
        return this.sqlSession.selectOne(this.sqlId("findByStockHandleId"), id);
    }

    //查公司结算列表
    public List<Map> listByFarmIdTime(Map<String, Object> criteria) {
        List<Map> resultList = this.sqlSession.selectList("listByFarmIdTime", criteria);

       /* resultList.stream().forEach(map -> {

            map.put("type", WarehouseMaterialHandleType.IN.getValue());

            map.put("inAmount", this.sqlSession.selectOne("selectSumAmount", map));

            map.put("type", WarehouseMaterialHandleType.OUT.getValue());

            map.put("outAmount", this.sqlSession.selectOne("selectSumAmount", map));

        });*/

        return resultList;
    }

    public List<Map<String, Object>> wlbdReport(
            Long farmId,
            String settlementDate, Integer pigBarnType,
            Long pigBarnId, Long pigGroupId, Integer handlerType,
            Integer type, Long warehouseId, String materialName
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("settlementDate", settlementDate);
        params.put("pigBarnType", pigBarnType);
        params.put("pigBarnId", pigBarnId);
        params.put("pigGroupId", pigGroupId);
        params.put("handlerType", handlerType);
        params.put("type", type);
        params.put("warehouseId", warehouseId);
        params.put("materialName", materialName);
        return this.sqlSession.selectList("wlbdReport", params);
    }

    public List<Map<String, Object>> getPigBarnNameOption(Long farmId, Integer pigType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("farmId", farmId);
        params.put("pigType", pigType);
        return this.sqlSession.selectList("getPigBarnNameOption", params);
    }

    public List<Map<String, Object>> getPigGroupNameOption(Long farmId, Long barnId) {
        Map<String, Long> params = Maps.newHashMap();
        params.put("farmId", farmId);
        params.put("barnId", barnId);
        return this.sqlSession.selectList("getPigGroupNameOption", params);
    }

    public List<Map<String, Object>> getWareHouseDataOption(Long farmId) {
        return this.sqlSession.selectList("getWareHouseDataOption", farmId);
    }

    public List<Map<String,Object>> selectFarmsByOrgId(Long orgId) {
        return this.sqlSession.selectList("selectFarmsByOrgId", orgId);
    }

    public List<Map<String,Object>> selectCompanyReportInfo(Map<String, Object> criteria) {
        return this.sqlSession.selectList("selectCompanyReportInfo",criteria);
    }

    //<!--退料入库-->
    //<!--得到仓库类型，仓库名称，仓库管理员，所属公司-->
    public List<Map> getFarmData(Long id) {
        return this.sqlSession.selectList(this.sqlId("getFarmData"),id);
    }

    //<!--得到领料出库的物料名称-->
    public List<Map> getMaterialNameByID(Long id) {
        return this.sqlSession.selectList(this.sqlId("getMaterialNameByID"),id);
    }

    //<!--根据物料名称得到 物料名称，物料编号，厂家，规格，单位，可退数量，备注-->
    public List<Map> getDataByMaterialName(Long stockHandleId,String materialName,String handleDate) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("stockHandleId", stockHandleId);
        map.put("materialName", materialName);
        map.put("handleDate", handleDate);
        return this.sqlSession.selectList(this.sqlId("getDataByMaterialName"), map);
    }

}
