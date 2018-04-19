package io.terminus.doctor.basic.dao;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.dto.warehouseV2.AmountAndQuantityDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleDeleteFlag;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
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


//    @Deprecated
//    public List<DoctorWarehouseMaterialHandle> findAfter(Long warehouseId, Long materialHandleId, Date handleDate) {
//        Map<String, Object> criteria = Maps.newHashMap();
//        criteria.put("warehouseId", warehouseId);
//        criteria.put("materialHandleId", materialHandleId);
//        criteria.put("handleDate", handleDate);
//
//        return this.sqlSession.selectList(this.sqlId("findAfter"), criteria);
//    }

    public List<DoctorWarehouseMaterialHandle> findAfter(Long warehouseId, Long skuId, Date handleDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("skuId", skuId);
        criteria.put("handleDate", handleDate);
        return this.sqlSession.selectList(this.sqlId("findAfterByDate"), criteria);
    }

    /**
     * 根据单据明细统计历史某一个节点的库存量
     *
     * @return
     */
    public BigDecimal getHistoryStock(Long warehouseId, Long skuId, Date handleDate) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("skuId", skuId);
        criteria.put("handleDate", handleDate);

        return this.sqlSession.selectOne(this.sqlId("countHistoryStock"), criteria);
    }


    public List<DoctorWarehouseMaterialHandle> findByAccountingDate(Long warehouseId, Integer year, Integer month) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("year", year);
        criteria.put("month", month);

        return this.sqlSession.selectList(this.sqlId("findByAccountingDate"), criteria);
    }

    public List<DoctorWarehouseMaterialHandle> findByOrgAndSettlementDate(Long orgId, Date settlementDate) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("orgId", orgId);
        criteria.put("settlementDate", settlementDate);

        return this.sqlSession.selectList(this.sqlId("findByOrgAndSettlementDate"), criteria);
    }

    @Deprecated
    public void reverseSettlement(Long farmId, Integer year, Integer month) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("farmId", farmId);
        criteria.put("year", year);
        criteria.put("month", month);

        this.sqlSession.update(this.sqlId("reverseSettlement"), criteria);
    }

    public void reverseSettlement(Long orgId, Date settlementDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("orgId", orgId);
        criteria.put("settlementDate", settlementDate);

        this.sqlSession.update(this.sqlId("reverseSettlementByOrg"), criteria);
    }

    /**
     * 获取本会计年月之前的库存量和金额
     *
     * @param warehouseId
     * @param settlementDate
     * @return
     */
    public AmountAndQuantityDto findBalanceByAccountingDate(Long warehouseId, Date settlementDate) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("warehouseId", warehouseId);
        criteria.put("settlementDate", settlementDate);

        Map<String, BigDecimal> result = this.sqlSession.selectOne(this.sqlId("findBalanceByAccountingDate"), criteria);
        return new AmountAndQuantityDto((result.get("amount")), result.get("quantity"));
    }


    /**
     * 获取公司下各个仓库在该会计年月之前的库存余量余额
     *
     * @param orgId
     * @param settlementDate
     * @return
     */
    public Map<Long, AmountAndQuantityDto> findEachWarehouseBalanceBySettlementDate(Long orgId, Date settlementDate) {

        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("orgId", orgId);
        criteria.put("settlementDate", settlementDate);

        List<Map<String, Object>> results = this.sqlSession.selectList(this.sqlId("findEachWarehouseBalanceByAccountingDate"), criteria);

        Map<Long/*warehouseId*/, AmountAndQuantityDto> balances = new HashMap<>();

        results.forEach(m -> {
            balances.put((Long) m.get("warehouseId"), new AmountAndQuantityDto(((BigDecimal) m.get("amount")), (BigDecimal) m.get("quantity")));
        });

        return balances;
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

    //得到领料出库的数量
    public BigDecimal findLibraryById(Long id) {
        BigDecimal quantity = this.sqlSession.selectOne(this.sqlId("findLibraryById"), id);
        return quantity;
    }


    //得到在此之前退料入库的数量和
    public BigDecimal findRetreatingById(DoctorWarehouseMaterialHandle materialHandle) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("otherTransferHandleId", materialHandle.getRelMaterialHandleId());
        criteria.put("handleDate", materialHandle.getHandleDate());
        BigDecimal quantity = this.sqlSession.selectOne(this.sqlId("findRetreatingById"), criteria);
        return quantity;
    }
    public Integer findByRelMaterialHandleId(Long materialId, int type) {
        Map<String, Object> criteria = Maps.newHashMap();
        criteria.put("id", materialId);
        criteria.put("type", type);
        return this.sqlSession.selectOne(this.sqlId("findSameMaterialId"), criteria);
    }

}
