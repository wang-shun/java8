package io.terminus.doctor.basic.service.warehouseV2;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroup;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroupDetail;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
public interface DoctorWarehouseMaterialApplyReadService {

    // 仓库领用明细报表 （陈娟 2018-10-17）
    Response<Map> collarReport(Long orgId,Long farmId,String startDate,String endDate,Integer materialType,String materialName,Integer pigType,Long pigBarnId,Long pigGroupId);

    /**
     * 查询
     *
     * @param id
     * @return doctorWarehouseMaterialApply
     */
    Response<DoctorWarehouseMaterialApply> findById(Long id);

    Response<DoctorWarehouseMaterialApply> findByMaterialHandle(Long materialHandleId);

    Response<DoctorWarehouseMaterialApply> findByMaterialHandleAndFarmId(Long materialHandleId,Long farmId);

    Response<List<DoctorWarehouseMaterialApply>> findByFarmAndPigGroup(Long farmId, Long groupId);

    /**
     * 分页
     *
     * @param pageNo
     * @param pageSize
     * @param criteria
     * @return Paging<DoctorWarehouseMaterialApply>
     */
    Response<Paging<DoctorWarehouseMaterialApply>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria);

    /**
     * 列表
     *
     * @param criteria
     * @return List<DoctorWarehouseMaterialApply>
     */
    Response<List<DoctorWarehouseMaterialApply>> list(Map<String, Object> criteria);

    Response<List<DoctorWarehouseMaterialApply>> list(DoctorWarehouseMaterialApply criteria);

    Response<List<DoctorWarehouseMaterialApply>> listOrderByHandleDate(DoctorWarehouseMaterialApply criteria, Integer limit);


    Response<Map<Integer, DoctorWarehouseMaterialApply>> listEachWarehouseTypeLastApply(Long farmId);

    /**
     * 仓库月度领用记录
     *
     * @param warehouseId
     * @param applyYear   领用年
     * @param applyMonth  领用月份
     * @param skuName     可为null
     * @return
     */
    Response<List<DoctorWarehouseMaterialApply>> month(Long warehouseId, Integer applyYear, Integer applyMonth, String skuName);

    Response<Map<String,Object>> selectPigGroupApply(Long orgId,Integer farmId, String pigType, String pigName, String pigGroupName,
                                                                                         Integer skuType, String skuName, String openAtStart,String openAtEnd, String closeAtStart,String closeAtEnd) throws ParseException;

    /**
     * 猪舍领用报表
     * @param criteria
     * @return
     */
    Response<List<Map>> piggeryReport(Long orgId,String date,DoctorWarehouseMaterialApply criteria);

    /**
     * 猪舍领用详情
     * @param criteria
     * @return
     */
    Response<List<Map>> piggeryDetails(Long orgId,String date,DoctorWarehouseMaterialApply criteria);

    public Response<List<DoctorWarehouseMaterialApplyPigGroupDetail>> selectPigGroupApplyDetail(Long orgId,Long pigGroupId, Long skuId);

    public List<DoctorWarehouseMaterialApplyPigGroup> selectPigGroupApplys(Long orgId,Integer farmId, String pigType, String pigName, String pigGroupName,
                                                                           Integer skuType, String skuName, String openAtStart,String openAtEnd, String closeAtStart,String closeAtEnd);
}