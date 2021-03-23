package io.terminus.doctor.basic.service.warehouseV2;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.*;
import io.terminus.doctor.basic.manager.*;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseStockHandle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-31 14:49:19
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorWarehouseStockHandleWriteServiceImpl implements DoctorWarehouseStockHandleWriteService {

    @Autowired
    private DoctorWarehouseStockHandleDao doctorWarehouseStockHandleDao;
    @Autowired
    private DoctorWarehouseMaterialHandleDao doctorWarehouseMaterialHandleDao;
    @Autowired
    private DoctorWarehouseMaterialHandleManager doctorWarehouseMaterialHandleManager;
    @Autowired
    private DoctorWarehouseStockDao doctorWarehouseStockDao;
    @Autowired
    private WarehouseOutManager warehouseOutManager;
    @Autowired
    private WarehouseInManager warehouseInManager;
    @Autowired
    private WarehouseFormulaManager warehouseFormulaManager;
    @Autowired
    private WarehouseReturnManager warehouseReturnManager;
    @Autowired
    private WarehouseTransferManager warehouseTransferManager;
    @Autowired
    private WarehouseInventoryManager warehouseInventoryManager;
    @Autowired
    private DoctorWarehouseSkuDao doctorWarehouseSkuDao;
    @RpcConsumer
    private DoctorBasicDao doctorBasicDao;
    @Autowired
    private DoctorWarehouseStockManager doctorWarehouseStockManager;
    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;

    @Override
    public Response<Long> create(DoctorWarehouseStockHandle doctorWarehouseStockHandle) {
        try {
            doctorWarehouseStockHandleDao.create(doctorWarehouseStockHandle);
            return Response.ok(doctorWarehouseStockHandle.getId());
        } catch (Exception e) {
            log.error("failed to create doctor warehouse stock handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.handle.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorWarehouseStockHandle doctorWarehouseStockHandle) {
        try {
            return Response.ok(doctorWarehouseStockHandleDao.update(doctorWarehouseStockHandle));
        } catch (Exception e) {
            log.error("failed to update doctor warehouse stock handle, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.warehouse.stock.handle.update.fail");
        }
    }

//    @Override
//    @Transactional
//    @ExceptionHandle("doctor.warehouse.stock.handle.delete.fail")
//    public Response<String> delete(Long id) {
//        List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.findByStockHandle(id);
//        for (DoctorWarehouseMaterialHandle handle : handles) {
//            DoctorWareHouse wareHouse = new DoctorWareHouse();
//            wareHouse.setId(handle.getWarehouseId());
//            wareHouse.setWareHouseName(handle.getWarehouseName());
//            wareHouse.setFarmId(handle.getFarmId());
//            wareHouse.setType(handle.getWarehouseType());
//            int type = handle.getType();
//            if(type != 1 && type != 2 && type != 3 && type != 4 && type != 5 && type != 10 && type != 7 && type != 8 && type != 9
//                    && type != 11 && type != 12 && type != 13){
//                return Response.fail("未知类型");
//            }
//            //配方生产
//            if (type == 11) {
//                return Response.fail("配方生产入库不支持删除");
//            }
//            //调拨入库
//            if (type == 9) {
//                return Response.fail("调拨入库不支持删除");
//            }
//            //采购入库
//            if(type == 1){
//                warehouseInManager.delete(handle);
//                doctorWarehouseStockManager.out(handle.getMaterialId(),handle.getQuantity(),wareHouse);
//            }
//            //退料入库
//            if(type == 13){
//                warehouseReturnManager.delete(handle);
//                DoctorWarehouseMaterialApply apply = doctorWarehouseMaterialApplyDao.findMaterialHandle(handle.getRelMaterialHandleId());
//                apply.setRefundQuantity(apply.getRefundQuantity().subtract(handle.getQuantity()));
//                doctorWarehouseMaterialApplyDao.update(apply);
//                doctorWarehouseStockManager.out(handle.getMaterialId(),handle.getQuantity(),wareHouse);
//            }
//            //盘盈入库
//            if(type==7) {
//                warehouseInventoryManager.delete(handle);
//                doctorWarehouseStockManager.out(handle.getMaterialId(),handle.getQuantity(),wareHouse);
//            }
//            //盘亏出库
//            if(type==8){
//                warehouseInventoryManager.delete(handle);
//                doctorWarehouseStockManager.in(handle.getMaterialId(),handle.getQuantity(),wareHouse);
//            }
//            //领料出库
//            if(type == 2){
//                List<DoctorWarehouseStockHandle> a = doctorWarehouseStockHandleDao.findByRelStockHandleIds(id);
//                if(a.size() == 1){
//                    return Response.fail("此物料存在退料,不支持删除" );
//                }
//                warehouseOutManager.delete(handle);
//                doctorWarehouseStockManager.in(handle.getMaterialId(),handle.getQuantity(),wareHouse);
//            }
//            //配方出库,调拨出库
//            if (type == 12 || type == 10) {
//                DoctorWarehouseStockHandle a = null;
//                if(type == 12) {
//                    a = doctorWarehouseStockHandleDao.findByRelStockHandleId(id);//被入库的单据表
//                } else {
//                    a = doctorWarehouseStockHandleDao.findByRelStockHandleIds(id).get(0);
//                }
//                if (a != null) {
//                    DoctorWarehouseMaterialHandle b = doctorWarehouseMaterialHandleDao.findByStockHandleId(a.getId());//被入库的单据明细表
//                    DoctorWareHouse wareHouse2 = new DoctorWareHouse();
//                    wareHouse2.setId(b.getWarehouseId());
//                    wareHouse2.setWareHouseName(b.getWarehouseName());
//                    wareHouse2.setFarmId(b.getFarmId());
//                    wareHouse2.setType(b.getWarehouseType());
//                    if (type == 12) {
//                        List<DoctorWarehouseStockHandle> c = doctorWarehouseStockHandleDao.findByRelStockHandleIds(a.getId());//其他配方出库的单据
//                        for (int i = 0; i < c.size(); i++) {
//                            long g = c.get(i).getId();
//                            if (g != id) {
//                                List<DoctorWarehouseMaterialHandle> d = doctorWarehouseMaterialHandleDao.findByStockHandleIds(c.get(i).getId());//其他配方出库的单据明细
//                                for (int j = 0; j < d.size(); j++) {
//                                    warehouseFormulaManager.delete(d.get(j));//删除其他配方出库单据明细
//                                    DoctorWareHouse wareHouse1 = new DoctorWareHouse();
//                                    wareHouse1.setId(d.get(j).getWarehouseId());
//                                    wareHouse1.setWareHouseName(d.get(j).getWarehouseName());
//                                    wareHouse1.setFarmId(d.get(j).getFarmId());
//                                    wareHouse1.setType(d.get(j).getWarehouseType());
//                                    doctorWarehouseStockManager.in(d.get(j).getMaterialId(), d.get(j).getQuantity(), wareHouse1);
//                                }
//
//                                doctorWarehouseStockHandleDao.delete(c.get(i).getId());//删除其他配方出库的单据
//                            }
//                        }
//                        warehouseFormulaManager.delete(b);//删除被入库的单据明细表
//                        doctorWarehouseStockManager.out(b.getMaterialId(), b.getQuantity(), wareHouse2);
//                    }
//                    if (type == 10) {
//                        warehouseTransferManager.delete(b);//删除被入库的单据明细表
//                        doctorWarehouseStockManager.out(b.getMaterialId(), b.getQuantity(), wareHouse2);
//                    }
//                    doctorWarehouseStockHandleDao.delete(a.getId());//删除被入库的单据
//                    if (type == 12) {
//                        List<DoctorWarehouseMaterialHandle> handles1 = doctorWarehouseMaterialHandleDao.findByStockHandle(id);
//                        for(DoctorWarehouseMaterialHandle handle1 : handles1) {
//                            warehouseFormulaManager.delete(handle1);//删除出库单的单据明细
//                            doctorWarehouseStockManager.in(handle.getMaterialId(), handle.getQuantity(), wareHouse);
//                        }
//                    }
//                    if (type == 10) {
//                        warehouseTransferManager.delete(handle);//删除出库单的单据明细
//                        doctorWarehouseStockManager.in(handle.getMaterialId(), handle.getQuantity(), wareHouse);
//                    }
//                }
//            }
//        }
//            /*Map<Long*//*skuId*//*, List<DoctorWarehouseMaterialHandle>> needValidSkuHandle = handles.stream()
//                    .collect(Collectors.groupingBy(DoctorWarehouseMaterialHandle::getMaterialId));
//            for (Long skuId : needValidSkuHandle.keySet()) {
//
//                DoctorWarehouseStock stock = doctorWarehouseStockDao.findBySkuIdAndWarehouseId(needValidSkuHandle.get(skuId).get(0).getMaterialId(), needValidSkuHandle.get(skuId).get(0).getWarehouseId())
//                        .orElseThrow(() -> new InvalidException("stock.not.found", needValidSkuHandle.get(skuId).get(0).getWarehouseName(), needValidSkuHandle.get(skuId).get(0).getMaterialName()));
//                DoctorWarehouseSku sku = doctorWarehouseSkuDao.findById(needValidSkuHandle.get(skuId).get(0).getMaterialId());
//                if (null == sku)
//                    throw new InvalidException("warehouse.sku.not.found", needValidSkuHandle.get(skuId).get(0).getMaterialId());
//
//
//                List<DoctorWarehouseMaterialHandle> thisSkuHandles = needValidSkuHandle.get(skuId);
//                BigDecimal thisSkuAllOutQuantity = thisSkuHandles.stream().filter(h -> WarehouseMaterialHandleType.isBigOut(h.getType())).map(DoctorWarehouseMaterialHandle::getQuantity).reduce((a, b) -> a.add(b)).orElse(new BigDecimal(0));
//                BigDecimal thisSkuAllInQuantity = thisSkuHandles.stream().filter(h -> WarehouseMaterialHandleType.isBigIn(h.getType())).map(DoctorWarehouseMaterialHandle::getQuantity).reduce((a, b) -> a.add(b)).orElse(new BigDecimal(0));
//                log.debug("check stock is enough for delete,stock[{}],out[{}],in[{}]", stock.getQuantity(), thisSkuAllOutQuantity, thisSkuAllInQuantity);
//                if (stock.getQuantity().add(thisSkuAllOutQuantity).compareTo(thisSkuAllInQuantity) < 0) {
//                    DoctorBasic unit = doctorBasicDao.findById(Long.parseLong(sku.getUnit()));
//                    throw new InvalidException("stock.not.enough.rollback",
//                            needValidSkuHandle.get(skuId).get(0).getWarehouseName(),
//                            needValidSkuHandle.get(skuId).get(0).getMaterialName(),
//                            stock.getQuantity(),
//                            null == unit ? "" : unit.getName(), thisSkuAllInQuantity);
//                }
//            }
//
//            handles.stream().forEach(h -> {
//                doctorWarehouseMaterialHandleManager.delete(h);
//            });*/
//            boolean isdelete = doctorWarehouseStockHandleDao.delete(id);
//            if(isdelete){
//                return Response.ok("删除成功");
//            }else{
//                return Response.fail("删除单据表失败");
//            }
//        }

    // 单据删除 （陈娟 2018-09-25）
    @Override
    @Transactional
    @ExceptionHandle("doctor.warehouse.stock.handle.delete.fail")
    public Response<String> delete(Long id) {
        boolean flag = false;
        DoctorWarehouseStockHandle stockHandle = doctorWarehouseStockHandleDao.findById(id);
        DoctorWareHouse wareHouse = new DoctorWareHouse();
        wareHouse.setId(stockHandle.getWarehouseId());
        wareHouse.setWareHouseName(stockHandle.getWarehouseName());
        wareHouse.setFarmId(stockHandle.getFarmId());
        wareHouse.setType(stockHandle.getWarehouseType());
        int type = stockHandle.getHandleSubType();
        if(type != 1 && type != 2 && type != 3 && type != 4 && type != 5 && type != 10 && type != 7 && type != 8 && type != 9
                && type != 11 && type != 12 && type != 13){
            return Response.fail("未知类型");
        }
        //配方生产
        if (type == 11) {
            return Response.fail("配方生产入库不支持删除");
        }
        //调拨入库
        if (type == 9) {
            return Response.fail("调拨入库不支持删除");
        }
        //采购入库
        if(type == 1){
            List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.findByStockHandle(id);
            for (DoctorWarehouseMaterialHandle handle : handles) {
                warehouseInManager.delete(handle);// 删除单据明细
                doctorWarehouseStockManager.out(handle.getMaterialId(),handle.getQuantity(),wareHouse);// 还原库存量
            }
            doctorWarehouseStockHandleDao.delete(id);// 删除单据
            flag = true;
        }
        //退料入库
        if(type == 13){
            List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.findByStockHandle(id);
            for (DoctorWarehouseMaterialHandle handle : handles) {
                warehouseReturnManager.delete(handle);// 删除单据明细
                DoctorWarehouseMaterialApply apply = doctorWarehouseMaterialApplyDao.findMaterialHandle(handle.getRelMaterialHandleId());
                apply.setRefundQuantity(apply.getRefundQuantity().subtract(handle.getQuantity()));
                doctorWarehouseMaterialApplyDao.update(apply);// 修改领料表的退料数量
                doctorWarehouseStockManager.in(handle.getMaterialId(),handle.getQuantity(),wareHouse);// 还原库存量
            }
            doctorWarehouseStockHandleDao.delete(id);// 删除单据
            flag = true;
        }
        //盘盈入库
        if(type==7) {
            List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.findByStockHandle(id);
            for (DoctorWarehouseMaterialHandle handle : handles) {
                warehouseInventoryManager.delete(handle);// 删除单据明细
                doctorWarehouseStockManager.out(handle.getMaterialId(),handle.getQuantity(),wareHouse);// 还原库存量
            }
            doctorWarehouseStockHandleDao.delete(id);// 删除单据
            flag = true;
        }
        //盘亏出库
        if(type==8){
            List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.findByStockHandle(id);
            for (DoctorWarehouseMaterialHandle handle : handles) {
                warehouseInventoryManager.delete(handle);// 删除单据明细
                doctorWarehouseStockManager.in(handle.getMaterialId(),handle.getQuantity(),wareHouse);// 还原库存量
            }
            doctorWarehouseStockHandleDao.delete(id);// 删除单据
            flag = true;
        }
        //领料出库
        if(type == 2){
            List<DoctorWarehouseMaterialHandle> handles = doctorWarehouseMaterialHandleDao.findByStockHandle(id);
            for (DoctorWarehouseMaterialHandle handle : handles) {
                List<DoctorWarehouseStockHandle> a = doctorWarehouseStockHandleDao.findByRelStockHandleIds(id);
                if(a.size() >= 1){
                    return Response.fail("此物料存在退料,不支持删除" );
                }
                warehouseOutManager.delete(handle);// 删除单据明细
                doctorWarehouseStockManager.in(handle.getMaterialId(),handle.getQuantity(),wareHouse);// 还原库存量
            }
            doctorWarehouseStockHandleDao.delete(id);// 删除单据
            flag = true;
        }

        // 配方出库 （配方 --> 多个出库对应一个入库）
        if (type == 12) {
            DoctorWarehouseStockHandle stockHandleIn = doctorWarehouseStockHandleDao.findByRelStockHandleId(id);//被入库的单据表
            if(stockHandleIn!=null){
                DoctorWarehouseMaterialHandle MaterialHandleIn = doctorWarehouseMaterialHandleDao.findByStockHandleId(stockHandleIn.getId());//被入库的单据明细表
                if(MaterialHandleIn!=null){
                    DoctorWareHouse wareHouseIn = new DoctorWareHouse();
                    wareHouseIn.setId(MaterialHandleIn.getWarehouseId());
                    wareHouseIn.setWareHouseName(MaterialHandleIn.getWarehouseName());
                    wareHouseIn.setFarmId(MaterialHandleIn.getFarmId());
                    wareHouseIn.setType(MaterialHandleIn.getWarehouseType());
                    doctorWarehouseStockManager.out(MaterialHandleIn.getMaterialId(), MaterialHandleIn.getQuantity(), wareHouseIn);//修改入库的库存量
                    warehouseFormulaManager.delete(MaterialHandleIn);//删除被入库的单据明细表
                }
                doctorWarehouseStockHandleDao.delete(stockHandleIn.getId());//删除被入库的单据
            }

            List<DoctorWarehouseStockHandle> stockHandleOutList = doctorWarehouseStockHandleDao.findByRelStockHandleIds(stockHandleIn.getId());//其他配方出库的单据
            for (DoctorWarehouseStockHandle stockHandleOut:stockHandleOutList) {
                if(stockHandleOut!=null){
                    List<DoctorWarehouseMaterialHandle> materialHandleOutList = doctorWarehouseMaterialHandleDao.findByStockHandleIds(stockHandleOut.getId());//其他配方出库的单据明细
                    for (DoctorWarehouseMaterialHandle materialHandleOut:materialHandleOutList) {
                        DoctorWareHouse wareHouseOut = new DoctorWareHouse();
                        wareHouseOut.setId(materialHandleOut.getWarehouseId());
                        wareHouseOut.setWareHouseName(materialHandleOut.getWarehouseName());
                        wareHouseOut.setFarmId(materialHandleOut.getFarmId());
                        wareHouseOut.setType(materialHandleOut.getWarehouseType());
                        doctorWarehouseStockManager.in(materialHandleOut.getMaterialId(), materialHandleOut.getQuantity(), wareHouseOut);//修改出库的库存量
                        warehouseFormulaManager.delete(materialHandleOut);//删除其他配方出库单据明细
                    }
                    doctorWarehouseStockHandleDao.delete(stockHandleOut.getId());//删除其他配方出库的单据
                }
            }
            flag = true;
        }

        // 调拨出库 （调拨 --> 一个出库对应多个入库 ）
        if (type == 10) {
            DoctorWarehouseStockHandle stockHandleOut = doctorWarehouseStockHandleDao.findById(id);//出库单据
            if(stockHandleOut!=null){
                List<DoctorWarehouseMaterialHandle> materialHandleOutList = doctorWarehouseMaterialHandleDao.findByStockHandleIds(stockHandleOut.getId());//出库单据明细表
                for (DoctorWarehouseMaterialHandle materialHandleOut:materialHandleOutList) {
                    if(materialHandleOut!=null){
                        DoctorWareHouse wareHouseOut = new DoctorWareHouse();
                        wareHouseOut.setId(materialHandleOut.getWarehouseId());
                        wareHouseOut.setWareHouseName(materialHandleOut.getWarehouseName());
                        wareHouseOut.setFarmId(materialHandleOut.getFarmId());
                        wareHouseOut.setType(materialHandleOut.getWarehouseType());
                        doctorWarehouseStockManager.in(materialHandleOut.getMaterialId(), materialHandleOut.getQuantity(), wareHouseOut);//修改出库的库存量
                        warehouseFormulaManager.delete(materialHandleOut);//删除出库单据明细表
                    }
                }
                doctorWarehouseStockHandleDao.delete(stockHandleOut.getId());//删除出库单据
            }

            List<DoctorWarehouseStockHandle> stockHandleInList = doctorWarehouseStockHandleDao.findByRelStockHandleIds(id);//入库单据
            for (DoctorWarehouseStockHandle stockHandleIn:stockHandleInList) {
                if(stockHandleIn!=null){
                    List<DoctorWarehouseMaterialHandle> materialHandleInList = doctorWarehouseMaterialHandleDao.findByStockHandleIds(stockHandleIn.getId());//入库单据明细
                    for (DoctorWarehouseMaterialHandle materialHandleIn:materialHandleInList) {
                        DoctorWareHouse wareHouseIn = new DoctorWareHouse();
                        wareHouseIn.setId(materialHandleIn.getWarehouseId());
                        wareHouseIn.setWareHouseName(materialHandleIn.getWarehouseName());
                        wareHouseIn.setFarmId(materialHandleIn.getFarmId());
                        wareHouseIn.setType(materialHandleIn.getWarehouseType());
                        doctorWarehouseStockManager.out(materialHandleIn.getMaterialId(), materialHandleIn.getQuantity(), wareHouseIn);//修改入库的库存量
                        warehouseFormulaManager.delete(materialHandleIn);//删除入库单据明细
                    }
                    doctorWarehouseStockHandleDao.delete(stockHandleIn.getId());//删除入库的单据
                }
            }
            flag = true;
        }

        if(flag){
            return Response.ok("删除成功");
        }else{
            return Response.fail("删除单据表失败");
        }

    }

}