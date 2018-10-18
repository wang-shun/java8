package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseOrgSettlementDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroup;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApplyPigGroupDetail;
import io.terminus.doctor.common.enums.WareHouseType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 14:05:59
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMaterialApplyReadServiceImpl implements DoctorWarehouseMaterialApplyReadService {

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Autowired
    private DoctorWarehouseOrgSettlementDao doctorWarehouseOrgSettlementDao;

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    // 仓库领用明细报表 （陈娟 2018-10-17）
    @Override
    public Response<Map> collarReport(Long orgId, Long farmId, String startDate, String endDate, Integer materialType, String materialName, Integer pigType, Long pigBarnId, Long pigGroupId) {

        // 判断筛选条件是否有猪群
        Integer flag = 0;
        if(null != pigGroupId && !pigGroupId.equals("")){
            flag = 1;
        }
        List<Map> collarMaps = doctorWarehouseMaterialApplyDao.collarReport(flag, orgId, farmId, startDate, endDate, materialType, materialName, pigType, pigBarnId, pigGroupId);
        BigDecimal allQuantity = new BigDecimal(0);
        BigDecimal allAmount = new BigDecimal(0);
        for (Map mm:collarMaps) {
            if(flag==0){
                // 如果筛选条件没有猪群，则先得到猪舍单据，再判断是否领用到猪群（是：展示猪群；否：无）
                DoctorWarehouseMaterialApply groupApply = doctorWarehouseMaterialApplyDao.getGroupById((Long) mm.get("material_handle_id"));
                if(groupApply!=null){
                    mm.put("pig_group_id",groupApply.getPigGroupId());
                    mm.put("pig_group_name",groupApply.getPigGroupName());
                }else{
                    mm.put("pig_group_id","--");
                    mm.put("pig_group_name","--");
                }
            }

            // 判断猪群是否关闭 （陈娟 2018-10-18）
            if((!mm.get("pig_group_id").toString().equals("--")&&(!mm.get("pig_group_id").toString().equals("-1")))){
                Integer status = doctorWarehouseMaterialApplyDao.getGroupStatus((Long) mm.get("pig_group_id"));
                if(status==-1){
                    mm.put("pig_group_name",mm.get("pig_group_name").toString()+"（已关闭）");
                }
            }

            // 判断是否结算
            boolean b = doctorWarehouseOrgSettlementDao.isSettled(orgId,(Date) mm.get("settlement_date"));
            if(!b){
                mm.put("unit_price","--");
                mm.put("amount","--");
                allAmount = allAmount.add(BigDecimal.ZERO);
            }else{
                if (mm.get("amount").toString() != null) {
                    allAmount = allAmount.add(new BigDecimal(mm.get("amount").toString()));
                }else{
                    allAmount = allAmount.add(BigDecimal.ZERO);
                }
            }
            if (mm.get("quantity").toString() != null) {
                allQuantity = allQuantity.add(new BigDecimal(mm.get("quantity").toString()));
            }else{
                allQuantity = allQuantity.add(BigDecimal.ZERO);
            }
        }
        Map<String, Object> maps = new HashMap<>();
        maps.put("collarMaps", collarMaps);

        // 总金额判断是否结算
        if(allAmount.compareTo(BigDecimal.ZERO)==0){
            maps.put("allAmount", "--");
        }else{
            maps.put("allAmount", allAmount);
        }

        // 总数量判断物料类型
        if(allQuantity.compareTo(BigDecimal.ZERO)==0){
            maps.put("allQuantity", "--");
        }else{
            List<Map> typeMaps = doctorWarehouseMaterialApplyDao.getMaterialTypes(orgId, farmId, startDate, endDate, materialType, materialName, pigType, pigBarnId, pigGroupId);
            if((typeMaps.size()==1)&&(Integer.parseInt(typeMaps.get(0).get("type").toString())==1)){
                maps.put("allQuantity", allQuantity);
            }else if((typeMaps.size()==1)&&(Integer.parseInt(typeMaps.get(0).get("type").toString())==2)){
                maps.put("allQuantity", allQuantity);
            }else if((typeMaps.size()==2)&&(Integer.parseInt(typeMaps.get(0).get("type").toString())==1&&Integer.parseInt(typeMaps.get(1).get("type").toString())==2)){
                maps.put("allQuantity", allQuantity);
            }else{
                maps.put("allQuantity", "--");
            }
        }

        return Response.ok(maps);
    }

    @Override
    public Response<DoctorWarehouseMaterialApply> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseMaterialApplyDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse material apply by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.find.fail");
        }
    }

    @Override
    @ExceptionHandle("doctor.warehouse.material.apply.find.fail")
    public Response<DoctorWarehouseMaterialApply> findByMaterialHandle(Long materialHandleId) {

        return Response.ok(doctorWarehouseMaterialApplyDao.findMaterialHandle(materialHandleId));
    }

    @Override
    @ExceptionHandle("doctor.warehouse.material.apply.find.fail")
    public Response<DoctorWarehouseMaterialApply> findByMaterialHandleAndFarmId(Long materialHandleId,Long farmId) {
        return Response.ok(doctorWarehouseMaterialApplyDao.findByMaterialHandleAndFarmId(materialHandleId,farmId));
    }

    @Override
    @ExceptionHandle("doctor.warehouse.material.apply.find.fail")
    public Response<List<DoctorWarehouseMaterialApply>> findByFarmAndPigGroup(Long farmId, Long groupId) {
        List<DoctorWarehouseMaterialApply> applies = doctorWarehouseMaterialApplyDao.list(DoctorWarehouseMaterialApply.builder()
                .farmId(farmId)
                .pigGroupId(groupId)
                .build());
        return Response.ok(applies);
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialApply>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialApplyDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse material apply by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApply>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialApplyDao.advList(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApply>> list(DoctorWarehouseMaterialApply criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialApplyDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApply>> listOrderByHandleDate(DoctorWarehouseMaterialApply criteria, Integer limit) {
        try {
            return Response.ok(doctorWarehouseMaterialApplyDao.listAndOrderByHandleDate(criteria, limit));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material apply, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.apply.list.fail");
        }
    }

    @Override
    public Response<Map<Integer, DoctorWarehouseMaterialApply>> listEachWarehouseTypeLastApply(Long farmId) {

        Map<Integer, DoctorWarehouseMaterialApply> eachWarehouseTypeLastApply = new HashMap<>();
        Stream.of(WareHouseType.values()).mapToInt(WareHouseType::getKey).forEach(type -> {
            List<DoctorWarehouseMaterialApply> lastApply = doctorWarehouseMaterialApplyDao.listAndOrderByHandleDate(DoctorWarehouseMaterialApply.builder().build(), 1);
            if (null == lastApply || lastApply.isEmpty())
                eachWarehouseTypeLastApply.put(type, null);
            else
                eachWarehouseTypeLastApply.put(type, lastApply.get(0));
        });


        return Response.ok(eachWarehouseTypeLastApply);
    }

    @Override
    @ExceptionHandle("doctor.warehouse.material.apply.list.fail")
    public Response<List<DoctorWarehouseMaterialApply>> month(Long warehouseId, Integer applyYear, Integer applyMonth, String skuName) {

        return Response.ok(doctorWarehouseMaterialApplyDao.list(DoctorWarehouseMaterialApply.builder()
                .warehouseId(warehouseId)
                .materialName(StringUtils.isBlank(skuName) ? null : skuName)
                .applyYear(applyYear)
                .applyMonth(applyMonth)
                .build()));
    }

    @Override
    public Response<Map<String,Object>> selectPigGroupApply(Long orgId,Integer farmId, String pigType, String pigName, String pigGroupName,
                                                                                                Integer skuType, String skuName, String openAtStart,String openAtEnd, String closeAtStart,String closeAtEnd) throws ParseException {
        List<DoctorWarehouseMaterialApplyPigGroup> pigGroupList = doctorWarehouseMaterialApplyDao.selectPigGroupApply1(farmId, pigType, pigName, pigGroupName, skuType, skuName, openAtStart, openAtEnd, closeAtStart, closeAtEnd);


        BigDecimal allQuantity = new BigDecimal(0);
        BigDecimal allAmount = new BigDecimal(0);
        // 陈娟2018-8-23
        for (int i = 0; i < pigGroupList.size(); i++) {
            boolean b = doctorWarehouseOrgSettlementDao.isSettled(orgId, pigGroupList.get(i).getSettlementDate());
            if(!b){
                pigGroupList.get(i).setUnitPrice("--");
                pigGroupList.get(i).setAmount("--");
                allAmount=allAmount.add(BigDecimal.ZERO);
            }else{
                if (pigGroupList.get(i).getAmount() != null) {
                    allAmount = new BigDecimal( pigGroupList.get(i).getAmount()).add(allAmount);
                }
            }
            if (pigGroupList.get(i).getQuantity() != null) {
                allQuantity = new BigDecimal(pigGroupList.get(i).getQuantity()).add(allQuantity);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("pigGroupList", pigGroupList);
        map.put("allQuantity", allQuantity);
        map.put("allAmount", allAmount);
        return Response.ok(map);
    }

    @Override
    public Response<List<Map>> piggeryReport(Long orgId,String date,DoctorWarehouseMaterialApply criteria) {
        List<Map> maps = doctorWarehouseMaterialApplyDao.piggeryReport(criteria);
        try {
            //会计年月支持选择未结算过的会计年月，如果选择未结算的会计区间，则报表不显示金额和单价
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            boolean  b = doctorWarehouseOrgSettlementDao.isSettled(orgId, sdf.parse(date));
            if(!b){
                for(int i = 0;i<maps.size(); i++){
                    maps.get(i).put("sum_unit_price","--");
                    maps.get(i).put("sum_amount","--");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Response.ok(maps);
    }

    @Override
    public Response<List<Map>> piggeryDetails(Long orgId,String date,DoctorWarehouseMaterialApply criteria) {
        List<Map> map=new ArrayList<Map>();
        List<Map> maps = doctorWarehouseMaterialApplyDao.piggeryDetails(criteria);
        map.addAll(maps);
        for (Map mm: maps) {
            //判断领料出库的物料是否有退料入库
            Integer count = doctorWarehouseMaterialHandleDao.findCountByRelMaterialHandleId((Long) mm.get("id"), (Long) mm.get("farm_id"));
            if(count>0){
                criteria.setMaterialHandleId((Long) mm.get("id"));
                List<Map> maps2 = doctorWarehouseMaterialApplyDao.piggeryRetreatingDetails(criteria);
                map.addAll(maps2);
            }
        }

        try {
            //会计年月支持选择未结算过的会计年月，如果选择未结算的会计区间，则报表不显示金额和单价
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            boolean  b = doctorWarehouseOrgSettlementDao.isSettled(orgId, sdf.parse(date));
            if(!b){
                BigDecimal allQuantity = new BigDecimal(0);
                for(int i = 0;i<map.size(); i++){
                    map.get(i).put("unit_price","--");
                    map.get(i).put("amount","--");
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Response.ok(map);
    }

    @Override
    public Response<List<DoctorWarehouseMaterialApplyPigGroupDetail>> selectPigGroupApplyDetail(Long orgId,Long pigGroupId, Long skuId){
        List<DoctorWarehouseMaterialApplyPigGroupDetail> maps=new ArrayList<DoctorWarehouseMaterialApplyPigGroupDetail>();
        List<DoctorWarehouseMaterialApplyPigGroupDetail> ApplyPigGroupDetails = doctorWarehouseMaterialApplyDao.selectPigGroupApplyDetail(pigGroupId, skuId);
        maps.addAll(ApplyPigGroupDetails);
        for (DoctorWarehouseMaterialApplyPigGroupDetail gd: ApplyPigGroupDetails) {
            //判断领料出库的物料是否有退料入库
            Integer count = doctorWarehouseMaterialHandleDao.findCountByRelMaterialHandleId(gd.getMaterialHandleId(),gd.getFarmId());
            if(count>0){
                List<DoctorWarehouseMaterialApplyPigGroupDetail> doctorWarehouseMaterialApplyPigGroupDetails = doctorWarehouseMaterialApplyDao.selectPigGroupApplyRetreatingDetail(pigGroupId, skuId, gd.getMaterialHandleId());
                maps.addAll(doctorWarehouseMaterialApplyPigGroupDetails);
            }
        }

        for (int i = 0; i < maps.size(); i++) {
            try {
            //会计年月支持选择未结算过的会计年月，如果选择未结算的会计区间，则报表不显示金额和单价
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            boolean b = doctorWarehouseOrgSettlementDao.isSettled(orgId, sdf.parse(maps.get(i).getSettlementDate()));
            if(!b){
                maps.get(i).setUnitPrice("--");
                maps.get(i).setAmount("--");
            }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return Response.ok(maps);
    }

    @Override
    public List<DoctorWarehouseMaterialApplyPigGroup> selectPigGroupApplys(Long orgId,Integer farmId, String pigType, String pigName, String pigGroupName,
                                                            Integer skuType, String skuName, String openAtStart,String openAtEnd, String closeAtStart,String closeAtEnd) {

        List<DoctorWarehouseMaterialApplyPigGroup> pigGroupList = doctorWarehouseMaterialApplyDao.selectPigGroupApply1(farmId, pigType, pigName, pigGroupName, skuType, skuName, openAtStart,openAtEnd,closeAtStart,closeAtEnd);
        for (int i = 0; i < pigGroupList.size(); i++) {
            // 陈娟2018-8-23
            boolean b = doctorWarehouseOrgSettlementDao.isSettled(orgId, pigGroupList.get(i).getSettlementDate());
            if (!b) {
                pigGroupList.get(i).setUnitPrice("--");
                pigGroupList.get(i).setAmount("--");
            }
        }

        return pigGroupList;
    }
}
