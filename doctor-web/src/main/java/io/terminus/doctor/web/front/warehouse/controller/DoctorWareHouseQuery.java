package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.service.DoctorBasicMaterialReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserProfileReadService;
import io.terminus.doctor.warehouse.dto.DoctorWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorWareHouse;
import io.terminus.doctor.warehouse.service.DoctorMaterialConsumeProviderReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorWareHouseWriteService;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseCreateDto;
import io.terminus.doctor.web.front.warehouse.dto.DoctorWareHouseUpdateDto;
import io.terminus.pampas.common.UserUtil;
import io.terminus.parana.user.model.User;
import io.terminus.parana.user.model.UserProfile;
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
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 仓库信息获取查询
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/warehouse/query")
public class DoctorWareHouseQuery {

    private final DoctorWareHouseReadService doctorWareHouseReadService;

    private final DoctorWareHouseWriteService doctorWareHouseWriteService;

    private final DoctorFarmReadService doctorFarmReadService;

    private final UserReadService<User> userReadService;

    private final DoctorUserProfileReadService doctorUserProfileReadService;

    private final DoctorBasicMaterialReadService doctorBasicMaterialReadService;

    private final DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService;
    @RpcConsumer
    private DoctorMaterialConsumeProviderReadService doctorMaterialConsumeProviderReadService;

    @Autowired
    public DoctorWareHouseQuery(DoctorWareHouseReadService doctorWareHouseReadService,
                                DoctorWareHouseWriteService doctorWareHouseWriteService,
                                DoctorFarmReadService doctorFarmReadService, UserReadService<User> userReadService,
                                DoctorUserProfileReadService doctorUserProfileReadService,
                                DoctorBasicMaterialReadService doctorBasicMaterialReadService,
                                DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService){
        this.doctorWareHouseReadService = doctorWareHouseReadService;
        this.doctorWareHouseWriteService = doctorWareHouseWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.userReadService = userReadService;
        this.doctorUserProfileReadService = doctorUserProfileReadService;
        this.doctorBasicMaterialReadService = doctorBasicMaterialReadService;
        this.doctorMaterialInWareHouseReadService = doctorMaterialInWareHouseReadService;
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

            doctorWareHouse = DoctorWareHouse.builder().id(doctorWareHouseUpdateDto.getDoctorWareHouseId())
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
            UserProfile userProfile = RespHelper.orServEx(
                    doctorUserProfileReadService.findProfileByUserIds(
                            Lists.newArrayList(doctorWareHouseCreateDto.getManagerId()))).get(0);

            Response<User> currentUserResponse = userReadService.findById(UserUtil.getUserId());
            User currentUser = currentUserResponse.getResult();

            doctorWareHouse = DoctorWareHouse.builder()
                    .wareHouseName(doctorWareHouseCreateDto.getWareHouseName())
                    .farmId(doctorWareHouseCreateDto.getFarmId()).farmName(doctorFarm.getName())
                    .managerId(doctorWareHouseCreateDto.getManagerId()).managerName(userProfile.getRealName())
                    .address(doctorWareHouseCreateDto.getAddress()).type(doctorWareHouseCreateDto.getType())
                    .creatorId(currentUser.getId()).creatorName(currentUser.getName())
                    .build();
        }catch (Exception e){
            log.error("create ware house info fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorWareHouseWriteService.createWareHouse(doctorWareHouse));
    }

    /**
     * 查询指定仓库中不存在的基础物料
     * @param type 基础物料类型
     * @see io.terminus.doctor.common.enums.WareHouseType
     * @param srm 输入码
     * @param wareHouseId 仓库id
     * @param exIds 排除掉的ids
     * @return
     */
    @RequestMapping(value = "/findBasicMaterialNotInWareHouse", method = RequestMethod.GET)
    @ResponseBody
    public List<DoctorBasicMaterial> findBasicMaterialNotInWareHouse(@RequestParam(value = "type", required = false) Integer type,
                                                                     @RequestParam(value = "srm", required = false) String srm,
                                                                     @RequestParam(value = "exIds", required = false) String exIds,
                                                                     @RequestParam Long wareHouseId){
        DoctorWareHouse wareHouse = RespHelper.or500(doctorWareHouseReadService.findById(wareHouseId));
        //已存在的物料id
        List<Long> exist = RespHelper.or500(doctorMaterialInWareHouseReadService.queryDoctorMaterialInWareHouse(wareHouse.getFarmId(), wareHouseId))
                .stream()
                .map(DoctorMaterialInWareHouse::getMaterialId)
                .collect(Collectors.toList());
        return RespHelper.or500(doctorBasicMaterialReadService.findBasicMaterialByTypeFilterBySrm(type, srm, exIds))
                .stream()
                .filter(basicMaterial -> !exist.contains(basicMaterial.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 分页查询仓库历史出入记录
     * @param warehouseId 仓库id
     * @param materialId 物料id
     * @param eventType 事件类型
     *                  @see DoctorMaterialConsumeProvider.EVENT_TYPE
     * @param materilaType 物料(仓库)类型
     *                     @see io.terminus.doctor.common.enums.WareHouseType
     * @param staffId 事件员工id
     * @param startAt 开始日期范围
     * @param endAt 结束日期范围
     * @param pageNo 第几页
     * @param size 每页数量
     * @return
     */
    @RequestMapping(value = "/pageConsumeProvideHistory", method = RequestMethod.GET)
    @ResponseBody
    public Paging<DoctorMaterialConsumeProvider> pageConsumeProvideHistory(@RequestParam(name = "warehouseId", required = false) Long warehouseId,
                                                                          @RequestParam(required = false) Long materialId,
                                                                          @RequestParam(required = false) Integer eventType,
                                                                          @RequestParam(required = false) Integer materilaType,
                                                                          @RequestParam(required = false) Long staffId,
                                                                          @RequestParam(required = false) String startAt,
                                                                          @RequestParam(required = false) String endAt,
                                                                          @RequestParam(required = false) Integer pageNo,
                                                                          @RequestParam(required = false) Integer size){
        return RespHelper.or500(doctorMaterialConsumeProviderReadService.page(warehouseId, materialId, eventType, materilaType, staffId, startAt, endAt, pageNo, size));
    }
}
