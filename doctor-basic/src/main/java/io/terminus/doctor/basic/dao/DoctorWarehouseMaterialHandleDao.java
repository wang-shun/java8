package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.dto.warehouseV2.CompanyReportDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import io.terminus.doctor.basic.service.warehouseV2.DoctorWarehouseSettlementService;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 13:22:27
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseMaterialHandleDao extends MyBatisDao<DoctorWarehouseMaterialHandle> {

    // 结算误差（陈娟 2018-8-21）
    // 得到上月结存金额
    public Map<String, Object> getLastAmount(Long warehouseId, Long materialId, Date settlementDate) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        criteria.put("settlementDate", settlementDate);

        return this.sqlSession.selectOne(this.sqlId("getLastAmount"), criteria);
    }

    //得到本月结存金额
    public Map<String, Object> getThisAmount(Long warehouseId, Long materialId, Date settlementDate) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        criteria.put("settlementDate", settlementDate);

        return this.sqlSession.selectOne(this.sqlId("getThisAmount"), criteria);
    }

    //得到最后一笔单据
    public DoctorWarehouseMaterialHandle getLastDocument(Long warehouseId, Long materialId, Date settlementDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        criteria.put("settlementDate", settlementDate);
        return this.sqlSession.selectOne(this.sqlId("getLastDocument"), criteria);
    }


    //更改物料有关的信息
    public Boolean updateWarehouseMaterialHandle(DoctorWarehouseMaterialHandle doctorWarehouseMaterialHandle) {
        return  this.sqlSession.update(this.sqlId("updateWarehouseMaterialHandle"), doctorWarehouseMaterialHandle)>=1;
    }

    //根据类型和RelMaterialHandleId得到对应的数据
    public DoctorWarehouseMaterialHandle findByRelMaterialHandleId(Long relMaterialHandleId, int type) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("relMaterialHandleId", relMaterialHandleId);
        criteria.put("type", type);
        return this.sqlSession.selectOne(this.sqlId("findByRelMaterialHandleId"), criteria);
    }

    public Integer getCountByRelMaterialHandleId(Long relMaterialHandleId, int type) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("relMaterialHandleId", relMaterialHandleId);
        criteria.put("type", type);
        return this.sqlSession.selectOne(this.sqlId("getCountByRelMaterialHandleId"), criteria);
    }

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

    public List<DoctorWarehouseMaterialHandle> findByStockHandles(List<Long> stockHandleIds) {

        Map<String, Object> param = new HashMap<>();
        param.put("stockHandleIds", stockHandleIds);
        param.put("deleteFlag", WarehouseMaterialHandleDeleteFlag.NOT_DELETE.getValue());

        return this.advList(param);
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

    public void updates(List<DoctorWarehouseMaterialHandle> materialHandles) {
        materialHandles.forEach(m -> {
            this.update(m);
        });
//        this.sqlSession.update(this.sqlId("updates"), materialHandles);
    }

    /**
     * 获取会计年月内的明细单据
     *
     * @param orgId          公司id
     * @param settlementDate 会计年月
     * @return
     */
    public List<DoctorWarehouseMaterialHandle> findByOrgAndSettlementDate(Long orgId, Date settlementDate,Integer flag) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("orgId", orgId);
        criteria.put("settlementDate", settlementDate);
        criteria.put("flag", flag);

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

    //未结算：上月结存数量实时计算
    public BigDecimal findWJSQuantity(BigInteger warehouseId,Integer warehouseType,Long materialId,Integer materialType,String materialName, Date settlementDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("warehouseType", warehouseType);
        criteria.put("materialId", materialId);
        criteria.put("materialType", materialType);
        criteria.put("materialName", materialName);
        criteria.put("settlementDate", settlementDate);

        BigDecimal quantity = this.sqlSession.selectOne(this.sqlId("findWJSQuantity"), criteria);
        return quantity;
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
        criteria.put("materialId", materialHandle.getMaterialId());
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


    public DoctorWarehouseMaterialHandle findByApply(Long stockHandleId, Long applyGroupId, Long applyBarnId) {
        Map<String, Object> params = new HashMap<>();
        params.put("stockHandleId", stockHandleId);
        params.put("applyGroupId", applyGroupId);
        params.put("applyBarnId", applyBarnId);
        return this.sqlSession.selectOne(this.sqlId("findByApply"), params);
    }

    public void updateHandleDateAndSettlementDate(Calendar handleDate, Date settlementDate, Long materialHandleId,Integer type) {
        // 更新入库单据的事件日期（陈娟 2018-10-08）
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("materialHandleId", materialHandleId);
        params.put("handleDate", handleDate.getTime());
        params.put("year", handleDate.get(Calendar.YEAR));
        params.put("month", handleDate.get(Calendar.MONTH) + 1);
        params.put("settlementDate", settlementDate);

        this.sqlSession.update(this.sqlId("updateHandleDateAndSettlementDate"), params);
    }

    //得到领料出库的数量
    public BigDecimal findLibraryById(Long id, String materialName) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", id);
        map.put("materialName", materialName);
        BigDecimal quantity = this.sqlSession.selectOne(this.sqlId("findLibraryById"), map);
        return quantity;
    }


    //得到在此之前退料入库的数量和
    public BigDecimal findRetreatingById(Long relMaterialHandleId, String materialName, Long stockHandleId) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("relMaterialHandleId", relMaterialHandleId);
        map.put("materialName", materialName);
        map.put("stockHandleId", stockHandleId);
        BigDecimal quantity = this.sqlSession.selectOne(this.sqlId("findRetreatingById"), map);
        return quantity;
    }

    public DoctorWarehouseMaterialHandle findByStockHandleId(Long id) {
        return this.sqlSession.selectOne(this.sqlId("findByStockHandleId"), id);
    }

    public List<DoctorWarehouseMaterialHandle> findByStockHandleIds(Long id) {
        return this.sqlSession.selectList(this.sqlId("findByStockHandleIds"), id);
    }

    //查公司结算列表
    public List<Map> listByFarmIdTime(Map<String, Object> criteria) {
        List<Map> resultList = this.sqlSession.selectList( "listByFarmIdTime", criteria);
        return resultList;
    }

    public Map<String, Object> lastWlbdReport(
            Long farmId, String settlementDate, Integer type, Long warehouseId, Long materialId
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("settlementDate", settlementDate);
        params.put("type", type);
        params.put("warehouseId", warehouseId);
        params.put("materialId", materialId);
        return this.sqlSession.selectOne("lastWlbdReport", params);
    }

    public List<Map<String, Object>>  getMeterails(
            Long orgId,
            Long farmId, String settlementDate,
            Integer type, Long warehouseId, String materialName
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("orgId",orgId);
        params.put("farmId", farmId);
        params.put("settlementDate", settlementDate);
        params.put("type", type);
        params.put("warehouseId", warehouseId);
        params.put("materialName", materialName == null ||
          "".equals(materialName.trim()) ||
         "null".equals(materialName.trim().toLowerCase()) ? null : materialName.trim());
        return this.sqlSession.selectList("getMeterails", params);
    }

    public List<Map<String, Object>> wlbdReport(
            Long farmId, String settlementDate, Integer type, Long warehouseId, Long materialId
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("settlementDate", settlementDate);
        params.put("type", type);
        params.put("warehouseId", warehouseId);
        params.put("materialId", materialId);
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

    public List<Map<String, Object>> selectFarmsByOrgId(Long orgId) {
        return this.sqlSession.selectList("selectFarmsByOrgId", orgId);
    }

    public List<Map<String, Object>> selectCompanyReportInfo(Map<String, Object> criteria) {
        return this.sqlSession.selectList("selectCompanyReportInfo", criteria);
    }

    //<!--退料入库-->
    //<!--得到仓库类型，仓库名称，仓库管理员，所属公司-->
    public List<Map> getFarmData(Long id) {
        return this.sqlSession.selectList(this.sqlId("getFarmData"), id);
    }

    //<!--得到领料出库的物料名称-->
    public List<Map> getMaterialNameByID(Long id) {
        List<Map> getMaterialNameByID = this.sqlSession.selectList(this.sqlId("getMaterialNameByID"), id);
        return getMaterialNameByID;
    }


    //<!--根据物料名称得到 物料名称，物料编号，厂家，规格，单位，可退数量，备注-->
    public List<Map> getDataByMaterialName(Long id) {
        List<Map> getDataByMaterialName = this.sqlSession.selectList(this.sqlId("getDataByMaterialName"), id);
        return getDataByMaterialName;
    }

    //根据id判断是否有退料入库
    public Integer findCountByRelMaterialHandleId(Long id,Long farmId) {
        Map<String, Long> params = Maps.newHashMap();
        params.put("farmId", farmId);
        params.put("id", id);
        Integer count = this.sqlSession.selectOne(this.sqlId("findCountByRelMaterialHandleId"), params);
        return count;
    }

    /**
     * 得到该公司第一笔单据的会计年月，用来结算的时候做判断
     * @param orgId
     * @return
     */
    public Date findSettlementDate(Long orgId) {
        return this.sqlSession.selectOne(this.sqlId("findSettlementDate"), orgId);
    }

    /**
     * 根据framId查最早生成单据的时间
     * @param farms
     * @return
     */
    public Date findMinTimeByFarmId(List<Long> farms) {
        return this.sqlSession.selectOne(this.sqlId("findMinTimeByFarmId"),farms);
    }

    // 得到调拨入库单 （陈娟 2018-09-14）
    public DoctorWarehouseMaterialHandle findByRelMaterialHandleIdAndWarehouseId(Long relMaterialHandleId, Long warehouseId,Long materialId) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("relMaterialHandleId", relMaterialHandleId);
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        return this.sqlSession.selectOne(this.sqlId("findByRelMaterialHandleIdAndWarehouseId"), criteria);
    }

    // 盘点 （陈娟 2018-09-18）
    public DoctorWarehouseMaterialHandle getMaxInventoryDate(Long warehouseId,Long materialId,Date handleDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("materialId", materialId);
        criteria.put("handleDate", handleDate);
        return this.sqlSession.selectOne(this.sqlId("getMaxInventoryDate"), criteria);
    }

    // 根据公司和会计年月得到配方入库单据 （陈娟 2018-09-28）
    public List<DoctorWarehouseMaterialHandle> findFormulaStorage(Long orgId, Date settlementDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("orgId", orgId);
        criteria.put("settlementDate", settlementDate);
        return this.sqlSession.selectList(this.sqlId("findFormulaStorage"), criteria);
    }
    // 根据relMaterialHandleId得到配方出库单据 （陈娟 2018-09-28）
    public List<DoctorWarehouseMaterialHandle> findFormulaByRelMaterialHandleId(Long relMaterialHandleId,Integer type) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("relMaterialHandleId", relMaterialHandleId);
        criteria.put("type", type);
        return this.sqlSession.selectList(this.sqlId("findFormulaByRelMaterialHandleId"), criteria);
    }
    // 配方入库金额，单价的结算（陈娟 2018-09-28）
    public Boolean updateUnitPriceAndAmountById(Long id, BigDecimal unitPrice, BigDecimal amount) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", id);
        map.put("unitPrice", unitPrice);
        map.put("amount", amount);
        return this.sqlSession.update(this.sqlId("updateUnitPriceAndAmountById"), map)>=1;
    }

    // 修改配方出库的关联ID （陈娟 2018-09-29）
    public Boolean updateRelMaterialHandleId(Long newRelMaterialHandleId, Long oldRelMaterialHandleId) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("newRelMaterialHandleId", newRelMaterialHandleId);
        map.put("oldRelMaterialHandleId", oldRelMaterialHandleId);
        return this.sqlSession.update(this.sqlId("updateRelMaterialHandleId"), map)>=1;
    }

    // 得到出库之前的单据类型 （陈娟 2018-10-08）
    public List<Map> getBeforeType(Long warehouseId,Date settlementDate,Long materialId,Long id) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("warehouseId", warehouseId);
        map.put("settlementDate", settlementDate);
        map.put("materialId", materialId);
        map.put("id", id);
        List<Map> typeMap = this.sqlSession.selectList(this.sqlId("getBeforeType"), map);
        return typeMap;
    }

    // 得到出库之前的配方入库单据的总金额和总数量 （陈娟 2018-10-08）
    public Map<String, Object> getBeforeRecipes(Long warehouseId,Date settlementDate,Long materialId,Long id) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("warehouseId", warehouseId);
        map.put("settlementDate", settlementDate);
        map.put("materialId", materialId);
        map.put("id", id);
        return this.sqlSession.selectOne(this.sqlId("getBeforeRecipes"), map);
    }
}
