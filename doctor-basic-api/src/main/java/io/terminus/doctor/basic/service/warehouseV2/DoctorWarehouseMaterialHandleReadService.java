package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.common.utils.ResponseUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 08:56:13
 * Created by [ your name ]
 */
public interface DoctorWarehouseMaterialHandleReadService {

    // 删除单据时判断是否有物料已盘点 （陈娟 2018-09-18）
    Response<String> deleteCheckInventory(Long id);

    // 编辑单据时判断是否有物料已盘点 （陈娟 2018-09-19）
    Response<DoctorWarehouseMaterialHandle> getMaxInventoryDate(Long warehouseId,Long materialId,Date handleDate);


    // 结算误差（陈娟 2018-8-21）
    Boolean getErrorAmount(Long warehouseId,Long materialId,Date settlementDate);

    Response<DoctorWarehouseMaterialHandle> getLastDocument(Long warehouseId, Long materialId,Date settlementDate);

    //盘盈的时候得到最近一次的采购单价
    Response<BigDecimal> getPDPrice(Long warehouseId,Long materialId,String handleDate);

    /**
     * 未结算：上月结存数量实时计算
     * @param warehouseId
     * @param settlementDate
     * @return
     */
    Response<BigDecimal> findWJSQuantity(BigInteger warehouseId,Integer warehouseType,Long materialId,Integer materialType,String materialName, Date settlementDate) ;

    /**
     * 得到领料出库的数量
     * @param id
     * @return
     */
    Response<BigDecimal> findLibraryById(Long id,String materialName);

    /**
     * 得到在此之前退料入库的数量和
     * @param relMaterialHandleId
     * @return
     */
    Response<BigDecimal> findRetreatingById(Long relMaterialHandleId,String materialName,Long stockHandleId);

    /**
     * 查询
     *
     * @param id
     * @return doctorWarehouseMaterialHandle
     */
    Response<DoctorWarehouseMaterialHandle> findById(Long id);


    /**
     * 根据单据查询操作明细
     *
     * @param stockHandleId
     * @return
     */
    Response<List<DoctorWarehouseMaterialHandle>> findByStockHandle(Long stockHandleId);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseMaterialHandle>
     */
    Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);


    /**
     * 高级分页
     * 添加对handleDate开始和结束日期的过滤；type类型的多条件过滤
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWarehouseMaterialHandle>> advPaging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);


    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return
     */
    Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, DoctorWarehouseMaterialHandle criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseMaterialHandle>
     */
    Response<List<DoctorWarehouseMaterialHandle>> list(Map<String, Object> criteria);


    /**
     * 高级列表
     * 添加对handleDate开始和结束日期的过滤；type类型的多条件过滤
     *
     * @param criteria
     * @return
     */
    Response<List<DoctorWarehouseMaterialHandle>> advList(Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return
     */
    Response<List<DoctorWarehouseMaterialHandle>> list(DoctorWarehouseMaterialHandle criteria);

    /**
     * 统计仓库纬度（出库，入库，调拨，盘点）金额
     *
     * @param data
     * @return
     */
    Response<Map<Long/*warehouseId*/, BigDecimal>> countWarehouseAmount(List<DoctorWarehouseMaterialHandle> data);


    /**
     * 统计仓库纬度（出库，入库，调拨，盘点）金额
     *
     * @param criteria
     * @param types
     * @return
     */
    Response<Map<WarehouseMaterialHandleType, Map<Long, BigDecimal>>> countWarehouseAmount(DoctorWarehouseMaterialHandle criteria, WarehouseMaterialHandleType... types);

    /**
     * 公司结算报表
     * @param criteria
     * @return
     */
    ResponseUtil<List<List<Map>>> companyReport(Map<String, Object> criteria);

    /**
     * 仓库结算报表
     * @param criteria
     * @return
     */
    ResponseUtil<List<List<Map>>> warehouseReport(Map<String,Object> criteria);

    /**
     * 仓库月度详情
     * @param
     * @return
     */
    Response<List<Map>> monthWarehouseDetail(Map<String, Object> criteria);

    /**
     * 查仓库下的物料
     * @param params
     * @return
     */
    Response<List<Map<String,Object>>> warehouseByFarmId(Map<String, Object> params);

    //<!--退料入库-->
    //<!--得到仓库类型，仓库名称，仓库管理员，所属公司-->
    Response<List<Map>> getFarmData(Long id);

    //<!--得到领料出库的物料名称-->
    Response<List<Map>> getMaterialNameByID(Long id);



    //<!--根据物料名称得到 物料名称，物料编号，厂家，规格，单位，可退数量，备注-->
    Response<List<Map>> getDataByMaterialName(Long id);

    //根据id判断是否有退料入库
    Response<Integer> findCountByRelMaterialHandleId(Long id,Long farmId);

    //
    Response<Date> findSettlementDate(Long orgId);
}