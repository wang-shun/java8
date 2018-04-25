package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialHandleDao;
import io.terminus.doctor.basic.dao.DoctorWarehouseStockMonthlyDao;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.common.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Override
    public Response<BigDecimal> findLibraryById(Long id) {
        return Response.ok(doctorWarehouseMaterialHandleDao.findLibraryById(id));
    }

    @Override
    public Response<BigDecimal> findRetreatingById(DoctorWarehouseMaterialHandle materialHandle) {
        return Response.ok(doctorWarehouseMaterialHandleDao.findRetreatingById(materialHandle));
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
        try {
            criteria = this.getMonth(criteria);
            int count =(int)criteria.get("count");
            int startMonth =(int)criteria.get("startMonth");
            int startYear =(int)criteria.get("startYear");

            int size = 0;
            List<Map<String,Object>> farms = Lists.newArrayList();

            for(;count>=0;count--,startMonth++){
                if(startMonth>12) {
                    startMonth = 1;
                    startYear += 1;
                }
                criteria.put("handleMonth",startMonth);
                criteria.put("handleYear",startYear);

                List<Map> lists = doctorWarehouseMaterialHandleDao.listByFarmIdTime(criteria);



                if(lists!=null&&lists.size()>0) {
                    boolean settled = doctorWarehouseSettlementService.isSettled((Long) lists.get(0).get("orgId"), (Date) lists.get(0).get("settlementDate"));
                    HashMap<Object, Object> infoMap = Maps.newHashMap();

                    Date settleDate = null;
                    BigDecimal allInAmount = new BigDecimal(0);
                    BigDecimal allOutAmount = new BigDecimal(0);
                    BigDecimal allBalanceAmount = new BigDecimal(0);
                    for(int x=0;x<lists.size();x++){
                        if(settleDate==null)
                            settleDate = (Date)lists.get(x).get("handleDate");
                        if(settleDate!=null&&lists.get(x).get("handleDate")!=null) {
                            if (settleDate.getTime() < ((Date) lists.get(x).get("handleDate")).getTime())
                                settleDate = (Date) lists.get(x).get("handleDate");
                        }
                        if(lists.get(x).get("inAmount")!=null)
                           allInAmount = allInAmount.add(new BigDecimal(lists.get(x).get("inAmount").toString()));

                        if(lists.get(x).get("outAmount")!=null)
                          allOutAmount = allOutAmount.add(new BigDecimal(lists.get(x).get("outAmount").toString()));

                        if(lists.get(x).get("balanceAmount")!=null)
                          allBalanceAmount = allBalanceAmount.add(new BigDecimal(lists.get(x).get("balanceAmount").toString()));

                    }
                    infoMap.put("month",startMonth);
                    infoMap.put("handleDate",settleDate);
                    infoMap.put("settled", settled);
                    infoMap.put("allInAmount",allInAmount);
                    infoMap.put("allOutAmount",allOutAmount);
                    infoMap.put("allBalanceAmount",allBalanceAmount);

                    if(lists.size()>size){
                        farms.clear();
                        for(int x=0;x<(lists.size());x++) {
                            Map map = Maps.newHashMap();
                            map.put("id", lists.get(x).get("farmId"));
                            map.put("name", lists.get(x).get("farmName"));
                            farms.add(map);
                        }
                        size = lists.size();
                    }
                    lists.add(infoMap);
                    resultList.add(lists);
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
        List<List<Map>> resultList = Lists.newArrayList();
        try {
            criteria = this.getMonth(criteria);
            int count =(int)criteria.get("count");
            int startMonth =(int)criteria.get("startMonth");
            int startYear =(int)criteria.get("startYear");

            int size = 0;
            List<Map<String,Object>> farms = Lists.newArrayList();

            for(;count>=0;count--,startMonth++) {
                if (startMonth > 12) {
                    startMonth = 1;
                    startYear += 1;
                }
                criteria.put("handleMonth",startMonth);
                criteria.put("handleYear",startYear);

                List<Map> lists = doctorWarehouseStockMonthlyDao.listByHouseIdTime(criteria);

                if(lists!=null&&lists.size()>0) {

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
                    infoMap.put("month",startMonth);
                    infoMap.put("settled", settled);
                    infoMap.put("allInAmount",allInAmount);
                    infoMap.put("allOutAmount",allOutAmount);
                    infoMap.put("allBalanceAmount",allBalanceAmount);

                    if(lists.size()>size){
                        farms.clear();
                        for(int x=0;x<(lists.size());x++) {
                            Map map = Maps.newHashMap();
                            map.put("id", lists.get(x).get("farmId"));
                            map.put("name", lists.get(x).get("farmName"));
                            map.put("date",lists.get(x).get("settlementDate"));
                            farms.add(map);
                        }
                        size = lists.size();
                    }

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
            List<Map> resultList = doctorWarehouseStockMonthlyDao.monthWarehouseDetail(criteria);
            resultList.add(this.countInfo(resultList));
            return Response.ok(resultList);
        }catch (Exception e) {
            log.error("failed to list doctor warehouse material handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.material.handle.list.fail");
        }
    }

    private Map<String, Object> getMonth(Map<String, Object> criteria){
        Date settlementDateStart = (Date)criteria.get("settlementDateStart");
        Date settlementDateEnd = (Date)criteria.get("settlementDateEnd");

        Calendar c = Calendar.getInstance();
        c.setTime(settlementDateStart);
        int startYear = c.get(Calendar.YEAR);
        int startMonth = c.get(Calendar.MONTH);

        c.setTime(settlementDateEnd);
        int endYear = c.get(Calendar.YEAR);
        int endMonth = c.get(Calendar.MONTH);

        int count = endMonth-startMonth;
        if(endYear>startYear){
            count+=12;
        }
        criteria.put("count",count);
        criteria.put("startYear",startYear);
        criteria.put("startMonth",startMonth);
        return criteria;
    }

    private Map<String, Object> countInfo(List<Map> resultList){
        BigDecimal allLastQuantity = new BigDecimal(0);
        BigDecimal allLastAmount = new BigDecimal(0);
        BigDecimal allInAmount = new BigDecimal(0);
        BigDecimal allInQuantity = new BigDecimal(0);
        BigDecimal allOutAmount = new BigDecimal(0);
        BigDecimal allOutQuantity = new BigDecimal(0);
        BigDecimal allBalanceQuantity = new BigDecimal(0);
        BigDecimal allBalanceAmount = new BigDecimal(0);
        for(Map map : resultList){
            allLastQuantity = allLastQuantity.add(new BigDecimal(map.get("lastQuantity").toString()));
            allLastAmount = allLastAmount.add(new BigDecimal(map.get("lastAmount").toString()));
            allInAmount = allInAmount.add(new BigDecimal(map.get("inAmount").toString()));
            allInQuantity = allInQuantity.add(new BigDecimal(map.get("inQuantity").toString()));
            allOutAmount = allOutAmount.add(new BigDecimal(map.get("outAmount").toString()));
            allOutQuantity = allOutQuantity.add(new BigDecimal(map.get("outQuantity").toString()));
            allBalanceQuantity = allBalanceQuantity.add(new BigDecimal(map.get("balanceQuantity").toString()));
            allBalanceAmount = allBalanceAmount.add(new BigDecimal(map.get("balanceAmount").toString()));
        }
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("allLastQuantity",allLastQuantity);
        map.put("allLastAmount",allLastAmount);
        map.put("allInAmount",allInAmount);
        map.put("allInQuantity",allInQuantity);
        map.put("allOutAmount",allOutAmount);
        map.put("allOutQuantity",allOutQuantity);
        map.put("allBalanceQuantity",allBalanceQuantity);
        map.put("allBalanceAmount",allBalanceAmount);
        return map;
    }

}
