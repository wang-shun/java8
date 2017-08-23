package io.terminus.doctor.web.front.new_warehouse;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorWareHouseCriteria;
import io.terminus.doctor.basic.enums.WarehouseMaterialHandleType;
import io.terminus.doctor.basic.model.DoctorWareHouse;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehouseMaterialHandle;
import io.terminus.doctor.basic.model.warehouse.DoctorWarehousePurchase;
import io.terminus.doctor.basic.service.*;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.web.front.new_warehouse.dto.WarehouseDto;
import io.terminus.doctor.web.front.new_warehouse.vo.FarmWarehouseVo;
import io.terminus.doctor.web.front.new_warehouse.vo.WarehouseVo;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by sunbo@terminus.io on 2017/8/8.
 */
@RestController
@RequestMapping("api/doctor/warehouse")
public class WarehouseController {


    @Autowired
    private DoctorFarmReadService doctorFarmReadService;

    @Autowired
    private UserReadService<User> userReadService;

    @Autowired
    private DoctorWareHouseWriteService doctorWareHouseWriteService;


    @Autowired
    private DoctorUserProfileReadService doctorUserProfileReadService;
    @RpcConsumer
    private NewDoctorWarehouseReaderService doctorWarehouseReaderService;


    @RpcConsumer
    private DoctorWarehouseMaterialHandleReadService doctorWarehouseMaterialHandleReadService;
    @RpcConsumer
    private DoctorWarehousePurchaseReadService doctorWarehousePurchaseReadService;

    @RpcConsumer
    private NewDoctorWarehouseWriterService newDoctorWarehouseWriterService;

    @RpcConsumer
    private DoctorWarehouseMaterialApplyReadService doctorWarehouseMaterialApplyReadService;

    @RequestMapping(method = RequestMethod.POST)
    public void create(@RequestBody @Valid WarehouseDto warehouseDto, Errors errors) {

        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        Response<DoctorFarm> farmResponse = doctorFarmReadService.findFarmById(warehouseDto.getFarmId());
        checkState(farmResponse.isSuccess(), "read.farmInfo.fail");
        DoctorFarm doctorFarm = farmResponse.getResult();

        if (doctorFarm == null)
            throw new JsonResponseException("farm.not.found");

        UserProfile userProfile = RespHelper.orServEx(doctorUserProfileReadService.findProfileByUserId(warehouseDto.getManagerId()));

        Response<User> currentUserResponse = userReadService.findById(UserUtil.getUserId());
        User currentUser = currentUserResponse.getResult();
        if (null == currentUser)
            throw new JsonResponseException("user.not.login");

        DoctorWareHouse doctorWareHouse = DoctorWareHouse.builder()
                .wareHouseName(warehouseDto.getName())
                .farmId(warehouseDto.getFarmId()).farmName(doctorFarm.getName())
                .managerId(warehouseDto.getManagerId()).managerName(userProfile.getRealName())
                .address(warehouseDto.getAddress()).type(warehouseDto.getType())
                .creatorId(currentUser.getId()).creatorName(currentUser.getName())
                .build();
        doctorWareHouseWriteService.createWareHouse(doctorWareHouse);
    }


    @RequestMapping(method = RequestMethod.GET)
    public Paging<DoctorWareHouse> query(@Valid DoctorWareHouseCriteria criteria) {

        return doctorWarehouseReaderService.paging(criteria).getResult();
    }

    @RequestMapping(method = RequestMethod.GET, value = "type/{type}")
    public List<WarehouseVo> query(@PathVariable Integer type, @RequestParam Long farmId) {
        DoctorWareHouse criteria = new DoctorWareHouse();
        criteria.setType(type);
        criteria.setFarmId(farmId);
        Response<List<DoctorWareHouse>> warehouseResponse = doctorWarehouseReaderService.list(criteria);
        if (!warehouseResponse.isSuccess())
            throw new JsonResponseException(warehouseResponse.getError());
        List<WarehouseVo> vos = new ArrayList<>(warehouseResponse.getResult().size());
        warehouseResponse.getResult().forEach(wareHouse -> {
            WarehouseVo vo = new WarehouseVo();
            vo.setId(wareHouse.getId());
            vo.setName(wareHouse.getWareHouseName());
            vo.setType(wareHouse.getType());
            vo.setManagerName(wareHouse.getManagerName());
            vo.setManagerId(wareHouse.getManagerId());
            vos.add(vo);
        });
        return vos;
    }

    @RequestMapping(method = RequestMethod.GET, value = "farm/{farmId}")
    public List<FarmWarehouseVo> query(@PathVariable Long farmId) {

        Response<List<DoctorWareHouse>> warehouseResponse = doctorWarehouseReaderService.findByFarmId(farmId);
        if (!warehouseResponse.isSuccess())
            throw new JsonResponseException(warehouseResponse.getError());

        List<FarmWarehouseVo> vos = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        warehouseResponse.getResult().forEach(wareHouse -> {

            FarmWarehouseVo vo = new FarmWarehouseVo();
            vo.setId(wareHouse.getId());
            vo.setName(wareHouse.getWareHouseName());
            vo.setType(wareHouse.getType());
            vo.setManagerName(wareHouse.getManagerName());

            DoctorWarehouseMaterialHandle handleCriteria = new DoctorWarehouseMaterialHandle();
            handleCriteria.setWarehouseId(wareHouse.getId());
            handleCriteria.setHandleYear(now.get(Calendar.YEAR));
            handleCriteria.setHandleMonth(now.get(Calendar.MONTH) + 1);
            Response<List<DoctorWarehouseMaterialHandle>> thisMonthHandlesResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
            if (!thisMonthHandlesResponse.isSuccess())
                throw new JsonResponseException(thisMonthHandlesResponse.getError());

            thisMonthHandlesResponse.getResult().forEach(handle -> {
                long money = handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue();
                if (handle.getType() == WarehouseMaterialHandleType.IN.getValue()) {
                    vo.setInAmount(vo.getInAmount() + money);
                    vo.setInQuantity(vo.getInQuantity().add(handle.getQuantity()));
                } else if (handle.getType() == WarehouseMaterialHandleType.OUT.getValue()) {
                    vo.setOutAmount(vo.getOutAmount() + money);
                    vo.setOutQuantity(vo.getOutQuantity().add(handle.getQuantity()));
                } else if (handle.getType() == WarehouseMaterialHandleType.TRANSFER.getValue()) {
                    vo.setTransferOutAmount(vo.getTransferOutAmount() + money);
                    vo.setTransferOutQuantity(vo.getTransferOutQuantity().add(handle.getQuantity()));
                }
            });
            //调拨，调入
            handleCriteria = new DoctorWarehouseMaterialHandle();
            handleCriteria.setTargetWarehouseId(wareHouse.getId());
            handleCriteria.setHandleYear(now.get(Calendar.YEAR));
            handleCriteria.setHandleMonth(now.get(Calendar.MONTH) + 1);
            handleCriteria.setType(WarehouseMaterialHandleType.TRANSFER.getValue());
            thisMonthHandlesResponse = doctorWarehouseMaterialHandleReadService.list(handleCriteria);
            if (!thisMonthHandlesResponse.isSuccess())
                throw new JsonResponseException(thisMonthHandlesResponse.getError());
            thisMonthHandlesResponse.getResult().forEach(handle -> {
                vo.setTransferInAmount(vo.getTransferInAmount() + handle.getQuantity().multiply(new BigDecimal(handle.getUnitPrice())).longValue());
                vo.setTransferInQuantity(vo.getTransferInQuantity().add(handle.getQuantity()));
            });


            //余额和余量
            DoctorWarehousePurchase purchaseCriteria = new DoctorWarehousePurchase();
            purchaseCriteria.setHandleFinishFlag(1);
            purchaseCriteria.setWarehouseId(wareHouse.getId());
            Response<List<DoctorWarehousePurchase>> warehousePurchasesResponse = doctorWarehousePurchaseReadService.list(purchaseCriteria);
            if (!warehousePurchasesResponse.isSuccess())
                throw new JsonResponseException(warehousePurchasesResponse.getError());
            BigDecimal totalQuantity = new BigDecimal(0);
            long totalMoney = 0L;
            for (DoctorWarehousePurchase purchase : warehousePurchasesResponse.getResult()) {
                BigDecimal leftQuantity = purchase.getQuantity().subtract(purchase.getHandleQuantity());
                totalQuantity = totalQuantity.add(leftQuantity);
                totalMoney += leftQuantity.multiply(new BigDecimal(purchase.getUnitPrice())).longValue();
            }
            vo.setBalanceQuantity(totalQuantity);
            vo.setBalanceAmount(totalMoney);
            vos.add(vo);

        });
        return vos;
    }


    @RequestMapping(method = RequestMethod.GET, value = "{id}")
    public WarehouseVo find(@PathVariable Long id) {
        Response<DoctorWareHouse> wareHouseResponse = doctorWarehouseReaderService.findById(id);
        if (!wareHouseResponse.isSuccess())
            throw new JsonResponseException(wareHouseResponse.getError());
        if (null == wareHouseResponse.getResult())
            throw new JsonResponseException("warehouse.not.found");

        //最近一次领用记录
        DoctorWarehouseMaterialApply applyCriteria = new DoctorWarehouseMaterialApply();
        applyCriteria.setWarehouseId(id);
        Response<List<DoctorWarehouseMaterialApply>> applyResponse = doctorWarehouseMaterialApplyReadService.listOrderByHandleDate(applyCriteria, 1);
        if (!applyResponse.isSuccess())
            throw new JsonResponseException(applyResponse.getError());

        WarehouseVo vo = new WarehouseVo();
        vo.setId(id);
        vo.setName(wareHouseResponse.getResult().getWareHouseName());
        vo.setType(wareHouseResponse.getResult().getType());
        vo.setManagerId(wareHouseResponse.getResult().getManagerId());
        vo.setManagerName(wareHouseResponse.getResult().getManagerName());

        if (null != applyResponse && !applyResponse.getResult().isEmpty())
            vo.setLastApplyDate(applyResponse.getResult().get(0).getApplyDate());

        return vo;
    }


//    @RequestMapping(method = RequestMethod.PUT, value = "in")
//    public void in(@RequestBody @Validated(WarehouseStockDto.InWarehouseValid.class) WarehouseStockDto dto) {
//
//        dto.setType(WarehouseMaterialHandleType.IN.getValue());
//        newDoctorWarehouseWriterService.handler(dto);
//    }
//
//
//    @RequestMapping(method = RequestMethod.PUT, value = "out")
//    public void out(@RequestBody @Validated(WarehouseStockDto.OutWarehouseValid.class) WarehouseStockDto dto) {
//        dto.setType(WarehouseMaterialHandleType.OUT.getValue());
//        newDoctorWarehouseWriterService.handler(dto);
//    }
//
//    @RequestMapping(method = RequestMethod.PUT, value = "inventory")
//    public void inventory(@RequestBody @Validated(WarehouseStockDto.InventoryWarehouseValid.class) WarehouseStockDto dto) {
//        dto.setType(WarehouseMaterialHandleType.INVENTORY.getValue());
//        newDoctorWarehouseWriterService.handler(dto);
//    }
//
//    @RequestMapping(method = RequestMethod.PUT, value = "transfer")
//    public void transfer(@RequestBody @Validated(WarehouseStockDto.TransferWarehouseValid.class) WarehouseStockDto dto) {
//        dto.setType(WarehouseMaterialHandleType.TRANSFER.getValue());
//        newDoctorWarehouseWriterService.handler(dto);
//    }


}
