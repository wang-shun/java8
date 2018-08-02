package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.dto.warehouseV2.CompanyReportDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.DoctorWarehouseOrgSettlement;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-08-21 08:56:13
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseMaterialHandleReadServiceImpl implements DoctorWarehouseMaterialHandleReadService {

    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;

    @Autowired
    private DoctorWarehouseSettlementService doctorWarehouseSettlementService;

    @Autowired
    private DoctorWarehouseStockMonthlyDao doctorWarehouseStockMonthlyDao;

    @Autowired
    private DoctorWarehouseOrgSettlementDao doctorWarehouseOrgSettlementDao;

    @Autowired
    private DoctorWareHouseDao doctorWareHouseDao;

    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Override
    public Response<BigDecimal> getPDPrice(Long warehouseId, Long materialId, String handleDate){
        DoctorWarehouseMaterialHandle mh=new DoctorWarehouseMaterialHandle();
        mh.setWarehouseId(warehouseId);
        mh.setMaterialId(materialId);
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dd = sdf.parse(handleDate);
            mh.setHandleDate(DateUtils.addDays(dd,1));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DoctorWarehouseMaterialHandle previous = doctorWarehouseMaterialHandleDao.findPrevious(mh, WarehouseMaterialHandleType.IN);
        return Response.ok(previous.getUnitPrice());
    }

    @Override
    public Response<BigDecimal> findWJSQuantity(BigInteger warehouseId,Integer warehouseType,Long materialId,Integer materialType,String materialName, Date settlementDate) {
        return Response.ok(doctorWarehouseMaterialHandleDao.findWJSQuantity(warehouseId,warehouseType,materialId,materialType,materialName,settlementDate));
    }

    @Override
    public Response<BigDecimal> findLibraryById(Long id,String materialName) {
        return Response.ok(doctorWarehouseMaterialHandleDao.findLibraryById(id,materialName));
    }

    @Override
    public Response<BigDecimal> findRetreatingById(Long relMaterialHandleId,String materialName,Long stockHandleId) {
        return Response.ok(doctorWarehouseMaterialHandleDao.findRetreatingById(relMaterialHandleId,materialName,stockHandleId));
    }

    @Override
    public Response<DoctorWarehouseMaterialHandle> findById(Long id) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor warehouse material handle by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.find.fail");
        }
    }

    @Override
    @ExceptionHandle("doctor.warehouse.material.handle.find.fail")
    public Response<List<DoctorWarehouseMaterialHandle>> findByStockHandle(Long stockHandleId) {

        return Response.ok(doctorWarehouseMaterialHandleDao.findByStockHandle(stockHandleId));
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialHandleDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse material handle by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.paging.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialHandle>> paging(Integer pageNo, Integer pageSize, DoctorWarehouseMaterialHandle criteria) {

        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialHandleDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse material handle by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.paging.fail");
        }
    }

    @Override
    public Response<Paging<DoctorWarehouseMaterialHandle>> advPaging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {

        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorWarehouseMaterialHandleDao.advPaging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor warehouse material handle by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialHandle>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.list.fail");
        }
    }


    @Override
    public Response<List<DoctorWarehouseMaterialHandle>> advList(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.advList(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.list.fail");
        }
    }

    @Override
    public Response<List<DoctorWarehouseMaterialHandle>> list(DoctorWarehouseMaterialHandle criteria) {
        try {
            return Response.ok(doctorWarehouseMaterialHandleDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.list.fail");
        }
    }


    @Override
    public Response<Map<Long, BigDecimal>> countWarehouseAmount(List<DoctorWarehouseMaterialHandle> data) {
        Map<Long/*warehouseId*/, BigDecimal/*amount*/> amounts = new HashMap<>();
        for (DoctorWarehouseMaterialHandle inHandle : data) {
            log.debug("count material handle[{}],warehouse[{}],quantity[{}],unitPrice[{}]", inHandle.getId(), inHandle.getWarehouseId(), inHandle.getQuantity(), inHandle.getUnitPrice());
            if (!amounts.containsKey(inHandle.getWarehouseId())) {
                BigDecimal amount = inHandle.getQuantity().multiply(inHandle.getUnitPrice());
                log.debug("amount[{}]", amount);
                amounts.put(inHandle.getWarehouseId(), amount);
            } else {
                BigDecimal alreadyAmount = amounts.get(inHandle.getWarehouseId());
                BigDecimal amount = inHandle.getQuantity().multiply(inHandle.getUnitPrice());
                log.debug("amount[{}]", amount);
                amounts.put(inHandle.getWarehouseId(), amount.add(alreadyAmount));
            }
        }
        return Response.ok(amounts);
    }

    @Override
    public Response<Map<WarehouseMaterialHandleType, Map<Long, BigDecimal>>> countWarehouseAmount(DoctorWarehouseMaterialHandle criteria, WarehouseMaterialHandleType... types) {


        Map<WarehouseMaterialHandleType, Map<Long, BigDecimal>> eachTypeAmounts = new HashMap<>();
        for (WarehouseMaterialHandleType type : types) {
            criteria.setType(type.getValue());
            List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.list(criteria);
            log.debug("count each warehouse amount for type[{}],handleYear[{}],handleMonth[{}]", type.getValue(), criteria.getHandleYear(), criteria.getHandleMonth());
            Map<Long/*warehouseId*/, BigDecimal/*amount*/> amounts = countWarehouseAmount(handles).getResult();
            log.debug(amounts.toString());
            eachTypeAmounts.put(type, amounts);
        }

        return Response.ok(eachTypeAmounts);
    }

    @Override
    public ResponseUtil<List<List<Map>>> companyReport(Map<String, Object> criteria) {
        List<List<Map>> resultList = Lists.newArrayList();
        //查公司名下所有猪场
        List<Map<String,Object>> farms = this.doctorWarehouseMaterialHandleDao.selectFarmsByOrgId((Long)criteria.get("orgId"));
        //查猪场最早创建仓库的时间
        List<Long> ids = Lists.newArrayList();
        farms.forEach(stringObjectMap -> {
            ids.add((Long)stringObjectMap.get("id"));
        });
        Date maxTime = this.doctorWareHouseDao.findMaxTimeByFarmId(ids);
        if(maxTime==null)
            return ResponseUtil.isOk(resultList,farms);
        try {
            criteria = this.getMonth(criteria);
            int count =(int)criteria.get("count");
            int startMonth =(int)criteria.get("startMonth");
            int startYear =(int)criteria.get("startYear");

            for(;count>=0;count--,startMonth++) {
                if (startMonth > 12) {
                    startMonth = 1;
                    startYear += 1;
                }
                criteria.put("settlementDate", DateUtil.toDate(startYear + "-" + startMonth + "-01"));

                List<Map> lists = doctorWarehouseMaterialHandleDao.listByFarmIdTime(criteria);
                Date time = (Date) criteria.get("settlementDate");
//                if (time.getTime() < System.currentTimeMillis()&&time.getTime()>maxTime.getTime()) {
                if (time.getTime() < System.currentTimeMillis()&&(time.equals(DateUtil.toDate(DateUtil.getYearMonth(maxTime) + "-01"))||time.after(DateUtil.toDate(DateUtil.getYearMonth(maxTime) + "-01")))) {
                    if (lists == null || lists.size() == 0) {
                        lists = Lists.newArrayList();
                    }
                    //补全猪场
                    for (Map<String, Object> farm : farms) {
                        boolean flag = false;
                        Long orgId = (Long)criteria.get("orgId");
                        for (Map<String, Object> list : lists) {
                            orgId = (Long) list.get("orgId");
                            if ((long) list.get("farmId") == (long) farm.get("id")) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            farm.put("settlementDate", criteria.get("settlementDate"));
                            farm.put("farmId", farm.get("id"));
                            farm.put("orgId", orgId);
                            farm.put("farmName", farm.get("name"));
                            farm.put("inAmount", new BigDecimal("0"));
                            farm.put("outAmount", new BigDecimal("0"));
                            farm.put("balanceAmount", new BigDecimal("0"));
                            lists.add(farm);
                            Collections.sort(lists, new Comparator<Map>() {
                                @Override
                                public int compare(Map o1, Map o2) {
                                    return (int) ((long) o1.get("farmId") - (long) o2.get("farmId"));
                                }
                            });
                        }
                    }
//                    if(!settled)
                    boolean settled = doctorWarehouseSettlementService.isSettled((Long) lists.get(0).get("orgId"), (Date) lists.get(0).get("settlementDate"));
                    HashMap<Object, Object> infoMap = Maps.newHashMap();

                    BigDecimal allInAmount = new BigDecimal(0);
                    BigDecimal allOutAmount = new BigDecimal(0);
                    BigDecimal allBalanceAmount = new BigDecimal(0);
                    for (int x = 0; x < lists.size(); x++) {
                        if (lists.get(x).get("inAmount") != null)
                            allInAmount = allInAmount.add(new BigDecimal(lists.get(x).get("inAmount").toString()));

                        if (lists.get(x).get("outAmount") != null)
                            allOutAmount = allOutAmount.add(new BigDecimal(lists.get(x).get("outAmount").toString()));

                        if (lists.get(x).get("balanceAmount") != null)
                            allBalanceAmount = allBalanceAmount.add(new BigDecimal(lists.get(x).get("balanceAmount").toString()));

                    }

                    DoctorWarehouseOrgSettlement orgId = doctorWarehouseOrgSettlementDao.findByOrg((Long) criteria.get("orgId"));
                    Date settleDate = null;
                    if (orgId != null) {
                        settleDate = orgId.getLastSettlementDate();
                    }
                    infoMap.put("year", startYear);
                    infoMap.put("month", startMonth);
                    infoMap.put("handleDate", settleDate);
                    infoMap.put("settlementDate", criteria.get("settlementDate"));
                    infoMap.put("settled", settled);
                    infoMap.put("allInAmount", allInAmount);
                    infoMap.put("allOutAmount", allOutAmount);
                    infoMap.put("allBalanceAmount", allBalanceAmount);

                    farms.get(0).put("settled", settled);

                    lists.add(infoMap);
                    resultList.add(lists);
//                }
                }
            }
            Collections.reverse(resultList);
            return ResponseUtil.isOk(resultList,farms);
        } catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return ResponseUtil.isFail("doctor.warehouse.material.handle.list.fail");
        }

    }

    @Override
    public ResponseUtil<List<List<Map>>> warehouseReport(Map<String, Object> criteria) {
        //查猪场名下仓库
        List<Map<String,Object>> farms = doctorWareHouseDao.findMapByFarmId((Long) criteria.get("farmId"));

        List<List<Map>> resultList = Lists.newArrayList();
        try {
            criteria = this.getMonth(criteria);
            int count =(int)criteria.get("count");
            int startMonth =(int)criteria.get("startMonth");
            int startYear =(int)criteria.get("startYear");


            for(;count>=0;count--,startMonth++) {
                if (startMonth > 12) {
                    startMonth = 1;
                    startYear += 1;
                }

                criteria.put("settlementDate",DateUtil.toDate(startYear + "-" + startMonth + "-01"));

                List<Map> lists = doctorWarehouseStockMonthlyDao.listByHouseIdTime(criteria);

                if(lists!=null&&lists.size()>0) {
                    //补全仓库
                    for(Map<String,Object> house:farms){
                        boolean flag = false;
                        for(Map<String,Object> list:lists){
                            try {
                                Long warehouseId = (Long) list.get("warehouseId");
                                BigInteger id = (BigInteger) house.get("id");
                                if (id.longValue() == warehouseId)
                                    flag = true;
                            }catch(Exception e){
                                BigInteger warehouseId = (BigInteger) list.get("warehouseId");
                                BigInteger id = (BigInteger) house.get("id");
                                if (id.longValue() == warehouseId.longValue())
                                    flag = true;
                            }
                        }
                        if(!flag){
                            house.put("settlementDate",criteria.get("settlementDate"));
                            house.put("warehouseId",((BigInteger)house.get("id")).longValue());
                            house.put("warehouseName",house.get("name"));
                            house.put("inAmount",new BigDecimal("0"));
                            house.put("outAmount",new BigDecimal("0"));
                            house.put("balanceAmount",new BigDecimal("0"));
                            lists.add(house);
                            Collections.sort(lists, new Comparator<Map>() {
                                @Override
                                public int compare(Map o1, Map o2) {
                                    Long warehouseId = (Long) o1.get("warehouseId");
                                    Long warehouseId2 = (Long) o2.get("warehouseId");
                                    return warehouseId.intValue() - warehouseId2.intValue();
                                }
                            });
                        }
                    }

                    boolean settled = doctorWarehouseSettlementService.isSettled((Long) lists.get(0).get("orgId"), (Date) lists.get(0).get("settlementDate"));

                    HashMap<Object, Object> infoMap = Maps.newHashMap();

                    BigDecimal allInAmount = new BigDecimal(0);
                    BigDecimal allOutAmount = new BigDecimal(0);
                    BigDecimal allBalanceAmount = new BigDecimal(0);
                    for(int x=0;x<lists.size();x++){
                        if(lists.get(x).get("inAmount")!=null)
                            allInAmount = allInAmount.add(new BigDecimal(lists.get(x).get("inAmount").toString()));

                        if(lists.get(x).get("outAmount")!=null)
                            allOutAmount = allOutAmount.add(new BigDecimal(lists.get(x).get("outAmount").toString()));

                        if(lists.get(x).get("balanceAmount")!=null)
                            allBalanceAmount = allBalanceAmount.add(new BigDecimal(lists.get(x).get("balanceAmount").toString()));
                    }
                    infoMap.put("year",startYear);
                    infoMap.put("month",startMonth);
                    infoMap.put("settled", settled);
                    infoMap.put("allInAmount",allInAmount);
                    infoMap.put("allOutAmount",allOutAmount);
                    infoMap.put("allBalanceAmount",allBalanceAmount);


                    lists.add(infoMap);
                    resultList.add(lists);
                }

            }
            Collections.reverse(resultList);
            return ResponseUtil.isOk(resultList,farms);
        }catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return ResponseUtil.isFail("doctor.warehouse.material.handle.list.fail");
        }
    }

    @Override
    public Response<List<Map>> monthWarehouseDetail(Map<String, Object> criteria) {
        try {
            Date date=null;
            if(criteria.get("settlementDate")==null) {
                date = new Date();
                /*SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
                String dateString = formatter.format(date);*/
                criteria.put("settlementDate", date);
            }else {
                String settlementDate = (String)criteria.get("settlementDate");
//                Date date = DateUtil.formatToDate(DateUtil.YYYYMM, settlementDate);
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
                date  = sdf.parse(settlementDate);
                criteria.put("settlementDate",date);
            }
            List<Map> resultList = doctorWarehouseStockMonthlyDao.monthWarehouseDetail(criteria);
            //本月是否结算
            boolean b = doctorWarehouseSettlementService.isSettled((Long)criteria.get("orgId"), date);
            if(!b){
                //未结算：上月结存数量实时计算 findWJSQuantity
                //取上个月
                Date newDate = null;
                if (date != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.add(Calendar.MONTH, -1);
                    newDate = calendar.getTime();
                }
                //上月是否结算
                boolean bb = doctorWarehouseSettlementService.isSettled((Long)criteria.get("orgId"), newDate);
                if(!bb){
                    //假如上月未结算，仓库月报期初数量显示，金额不显示，本月入库数量金额都显示，本月出库数量显示 金额不显示 ，期末数量显示 金额不显示
                    for(Map mm:resultList){
                        BigDecimal quantity = doctorWarehouseMaterialHandleDao.findWJSQuantity(BigInteger.valueOf((Long) mm.get("warehouseId")), null, (Long) mm.get("materialId"), null, null, date);
                        mm.put("lastQuantity",quantity);
                        BigDecimal balanceQuantity=quantity.add((BigDecimal)mm.get("inQuantity")).subtract((BigDecimal)mm.get("outQuantity"));
                        mm.put("balanceQuantity",balanceQuantity);
                        mm.put("lastAmount","--");
                        mm.put("balanceAmount","--");
                        mm.put("outAmount","--");
                    }
                }else{
                    //假如上月已结算，仓库月报期初数量、金额有数据，本月入库数量、金额有数据，本月出库数量显示，金额不显示，期末数量显示，金额不显示；
                    for(Map mm:resultList){
                        BigDecimal quantity = doctorWarehouseMaterialHandleDao.findWJSQuantity(BigInteger.valueOf((Long) mm.get("warehouseId")), null, (Long) mm.get("materialId"), null, null, date);
                        mm.put("lastQuantity",quantity);
                        BigDecimal balanceQuantity=quantity.add((BigDecimal)mm.get("inQuantity")).subtract((BigDecimal)mm.get("outQuantity"));
                        mm.put("balanceQuantity",balanceQuantity);
                        mm.put("balanceAmount","--");
                        mm.put("outAmount","--");
                    }
                }

            }
            DoctorWareHouse warehouseId = doctorWareHouseDao.findById((Long) criteria.get("warehouseId"));
            Map<String, Object> all = this.countInfo(resultList, (Long) criteria.get("orgId"), date,warehouseId.getType());
            resultList.add(all);
            return Response.ok(resultList);
        }catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.list.fail");
        }
    }

    @Override
    public Response<List<Map<String,Object>>> warehouseByFarmId(Map<String, Object> criteria) {
        return Response.ok(doctorWareHouseDao.findMapByFarmId((Long) criteria.get("farmId")));
    }

    @Override
    public Response<List<Map>> getFarmData(Long id) {
        return Response.ok(doctorWarehouseMaterialHandleDao.getFarmData(id));
    }

    @Override
    public Response<List<Map>> getMaterialNameByID(Long id) {
        return Response.ok(doctorWarehouseMaterialHandleDao.getMaterialNameByID(id));
    }

    @Override
    public Response<List<Map>> getDataByMaterialName(Long id) {
//        List<Map> dataByMaterialName = doctorWarehouseMaterialHandleDao.getDataByMaterialName(id);
        return Response.ok(doctorWarehouseMaterialHandleDao.getDataByMaterialName(id));
    }

    private Map<String, Object> getMonth(Map<String, Object> criteria){
        Date settlementDateStart = (Date)criteria.get("settlementDateStart");
        Date settlementDateEnd = (Date)criteria.get("settlementDateEnd");

        Calendar c = Calendar.getInstance();
        c.setTime(settlementDateStart);
        int startYear = c.get(Calendar.YEAR);
        int startMonth = c.get(Calendar.MONTH)+1;

        c.setTime(settlementDateEnd);
        int endYear = c.get(Calendar.YEAR);
        int endMonth = c.get(Calendar.MONTH)+1;

        int count = endMonth-startMonth;
        if(endYear>startYear){
            count+=12;
        }
        criteria.put("count",count);
        criteria.put("startYear",startYear);
        criteria.put("startMonth",startMonth);
        return criteria;
    }

    private Map<String, Object> countInfo(List<Map> resultList,Long orgId,Date date,Integer warehouseType){
        boolean b = doctorWarehouseSettlementService.isSettled(orgId, date);
        BigDecimal allLastQuantity = new BigDecimal(0);
        BigDecimal allLastAmount = new BigDecimal(0);
        BigDecimal allInAmount = new BigDecimal(0);
        BigDecimal allInQuantity = new BigDecimal(0);
        BigDecimal allOutAmount = new BigDecimal(0);
        BigDecimal allOutQuantity = new BigDecimal(0);
        BigDecimal allBalanceQuantity = new BigDecimal(0);
        BigDecimal allBalanceAmount = new BigDecimal(0);
        for(Map map : resultList){
            if(warehouseType==1||warehouseType==2){
                allLastQuantity = allLastQuantity.add(new BigDecimal(map.get("lastQuantity").toString()));
                allInQuantity = allInQuantity.add(new BigDecimal(map.get("inQuantity").toString()));
                allOutQuantity = allOutQuantity.add(new BigDecimal(map.get("outQuantity").toString()));
                allBalanceQuantity = allBalanceQuantity.add(new BigDecimal(map.get("balanceQuantity").toString()));
            }

            if(b){
                allLastAmount = allLastAmount.add(new BigDecimal(map.get("lastAmount").toString()));
                allInAmount = allInAmount.add(new BigDecimal(map.get("inAmount").toString()));
                allOutAmount = allOutAmount.add(new BigDecimal(map.get("outAmount").toString()));
                allBalanceAmount = allBalanceAmount.add(new BigDecimal(map.get("balanceAmount").toString()));
            }
        }
        HashMap<String, Object> map = Maps.newHashMap();
        if(warehouseType==1||warehouseType==2) {
            map.put("allLastQuantity", allLastQuantity);
            map.put("allInQuantity", allInQuantity);
            map.put("allOutQuantity", allOutQuantity);
            map.put("allBalanceQuantity", allBalanceQuantity);
        }else{
            map.put("allLastQuantity", "--");
            map.put("allInQuantity", "--");
            map.put("allOutQuantity", "--");
            map.put("allBalanceQuantity", "--");
        }

        if(!b){
            map.put("allLastAmount","--");
            map.put("allInAmount","--");
            map.put("allOutAmount","--");
            map.put("allBalanceAmount","--");
        }else{
            map.put("allLastAmount",allLastAmount);
            map.put("allInAmount",allInAmount);
            map.put("allOutAmount",allOutAmount);
            map.put("allBalanceAmount",allBalanceAmount);
        }
        return map;
    }

    //根据id判断是否有退料入库
    @Override
    public Response<Integer> findCountByRelMaterialHandleId(Long id,Long farmId) {
        return  Response.ok(doctorWarehouseMaterialHandleDao.findCountByRelMaterialHandleId(id,farmId));
    }

    //得到该公司第一笔单据的会计年月，用来结算的时候做判断
    @Override
    public Response<Date> findSettlementDate(Long orgId) {
        return Response.ok(doctorWarehouseMaterialHandleDao.findSettlementDate(orgId));
    }
}
