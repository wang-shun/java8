package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.service.DoctorWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorWareHouseWriteService;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseCreateDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseUpdateDto;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.service.UserReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 猪场信息获取查询
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/warehouse/query")
public class DoctorWareHouseQuery {

    private final DoctorWareHouseReadService doctorWareHouseReadService;

    private final DoctorWareHouseWriteService doctorWareHouseWriteService;

    private final DoctorFarmReadService doctorFarmReadService;

    private final UserReadService userReadService;

    @Autowired
    public DoctorWareHouseQuery(DoctorWareHouseReadService doctorWareHouseReadService,
                                DoctorWareHouseWriteService doctorWareHouseWriteService,
                                DoctorFarmReadService doctorFarmReadService, UserReadService userReadService){
        this.doctorWareHouseReadService = doctorWareHouseReadService;
        this.doctorWareHouseWriteService = doctorWareHouseWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.userReadService = userReadService;
    }

    @RequestMapping(value = "/listWareHouseType", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorFarmWareHouseType> pagingDoctorWareHouseType(@RequestParam("farmId") Long farmId){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorFarmWareHouseType(farmId));
    }

    @RequestMapping(value = "/pagingDoctorWareHouseDto", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorWareHouseDto> pagingDoctorWareHouseDto(@RequestParam("farmId") Long farmId,
                                                               @RequestParam(value = "type", required = false) Integer type,
                                                               @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                               @RequestParam(value = "pageSize", required = false) Integer pageSize){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorWarehouseDto(farmId, type, pageNo, pageSize));
    }

    /**
     * 通过WareHouseId 获取仓库详细信息内容
     * @param warehouseId
     * @return
     */
    @RequestMapping(value = "/queryDtoByWareHouseId", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public DoctorWareHouseDto queryDtoByWareHouseId(@RequestParam("warehouseId") Long warehouseId){
        return RespHelper.or500(doctorWareHouseReadService.queryDoctorWareHouseById(warehouseId));
    }

    @RequestMapping(value = "/updateWareHouse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateWareHouse(@RequestBody DoctorWareHouseUpdateDto doctorWareHouseUpdateDto){
        DoctorWareHouse doctorWareHouse = null;
        try{
            // get user reader info
            Response<User> userResponse = userReadService.findById(UserUtil.getUserId());
            checkState(userResponse.isSuccess(), "read.userInfo.fail");
            User user = userResponse.getResult();

            doctorWareHouse = DoctorWareHouse.builder()
                    .address(doctorWareHouseUpdateDto.getAddress())
                    .managerId(doctorWareHouseUpdateDto.getManagerId()).managerName(doctorWareHouseUpdateDto.getWarehouseName())
                    .wareHouseName(doctorWareHouseUpdateDto.getWarehouseName())
                    .updatorName(user.getName()).build();
        }catch (Exception e){
            log.error("update warehouse fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException("update.warehouse.fail");
        }

        return RespHelper.or500(doctorWareHouseWriteService.updateWareHouse(doctorWareHouse));
    }

    @RequestMapping(value = "/createWareHouse", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createWareHouseInfo(@RequestBody DoctorWareHouseCreateDto doctorWareHouseCreateDto){
        DoctorWareHouse doctorWareHouse = null;
        try{
            // get farm info
            Response<DoctorFarm> farmResponse = doctorFarmReadService.findFarmById(doctorWareHouseCreateDto.getFarmId());
            checkState(farmResponse.isSuccess(), "read.farmInfo.fail");
            DoctorFarm doctorFarm = farmResponse.getResult();

            // get user reader info
            Response<User> userResponse = userReadService.findById(doctorWareHouseCreateDto.getManagerId());
            checkState(userResponse.isSuccess(), "read.userInfo.fail");
            User user = userResponse.getResult();

            Response<User> currentUserResponse = userReadService.findById(UserUtil.getUserId());
            User currentUser = currentUserResponse.getResult();

            doctorWareHouse = DoctorWareHouse.builder()
                    .wareHouseName(doctorWareHouseCreateDto.getWareHouseName())
                    .farmId(doctorWareHouseCreateDto.getFarmId()).farmName(doctorFarm.getName())
                    .managerId(doctorWareHouseCreateDto.getManagerId()).managerName(user.getName())
                    .address(doctorWareHouseCreateDto.getAddress()).type(doctorWareHouseCreateDto.getType())
                    .creatorId(currentUser.getId()).creatorName(currentUser.getName())
                    .build();
        }catch (Exception e){
            log.error("create ware house info fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorWareHouseWriteService.createWareHouse(doctorWareHouse));
    }
}
