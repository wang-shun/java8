package io.terminus.doctor.web.front.warehouse.controller;

import com.google.common.base.Throwables;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.warehouse.dto.DoctorMaterialInWareHouseDto;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import io.terminus.doctor.warehouse.service.DoctorMaterialInWareHouseReadService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInWareHouseWriteService;
import io.terminus.doctor.warehouse.service.DoctorMaterialInfoReadService;
import io.terminus.doctor.web.front.warehouse.dto.DoctorConsumeProviderInputDto;
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
import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-25
 * Email:yaoqj@terminus.io
 * Descirbe: 对应的仓库事件信息录入
 */
@Slf4j
@Controller
@RequestMapping("/api/doctor/warehouse/event")
public class DoctorWareHouseEvents {

    private final DoctorMaterialInWareHouseWriteService doctorMaterialInWareHouseWriteService;

    private final DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService;

    private final UserReadService userReadService;

    private final DoctorBarnReadService doctorBarnReadService;

    private final DoctorMaterialInfoReadService doctorMaterialInfoReadService;

    @Autowired
    public DoctorWareHouseEvents(DoctorMaterialInWareHouseWriteService doctorMaterialInWareHouseWriteService,
                                 DoctorMaterialInWareHouseReadService doctorMaterialInWareHouseReadService,
                                 UserReadService userReadService, DoctorBarnReadService doctorBarnReadService,
                                 DoctorMaterialInfoReadService doctorMaterialInfoReadService){
        this.doctorMaterialInWareHouseWriteService = doctorMaterialInWareHouseWriteService;
        this.doctorMaterialInWareHouseReadService = doctorMaterialInWareHouseReadService;
        this.userReadService = userReadService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorMaterialInfoReadService = doctorMaterialInfoReadService;
    }

    /**
     * 获取对应的仓库的 物料信息
     * @param farmId
     * @param wareHouseId
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorMaterialInWareHouse> listDoctorMaterialInWareHouse(@RequestParam("farmId") Long farmId,
                                                                         @RequestParam("wareHouseId") Long wareHouseId){
        return RespHelper.or500(doctorMaterialInWareHouseReadService.queryDoctorMaterialInWareHouse(farmId, wareHouseId));
    }

    /**
     * 对应的仓库信息的分页查询方式
     * @param farmId
     * @param wareHouseId
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/paging", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<DoctorMaterialInWareHouseDto> pagingDoctorMaterialInWareHouse(@RequestParam("farmId") Long farmId,
                                                                                @RequestParam("wareHouseId") Long wareHouseId,
                                                                                @RequestParam("pageNo") Integer pageNo,
                                                                                @RequestParam("pageSize") Integer pageSize){
        return RespHelper.or500(doctorMaterialInWareHouseReadService.pagingDoctorMaterialInWareHouse(farmId, wareHouseId, pageNo, pageSize));
    }

    @RequestMapping(value = "/materialInWareHouse/delete", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean deleteMaterialInWareHouse(@RequestParam("materialInWareHouseId") Long materialInWareHouseId){
        Long userId;
        String userName;
        try{
            userId = UserUtil.getUserId();
            Response<User> userResponse = userReadService.findById(userId);
            checkState(userResponse.isSuccess(), "query.userInfo.error");
            userName = userResponse.getResult().getName();
        }catch (Exception e){
            log.error("get user info fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(
                doctorMaterialInWareHouseWriteService.deleteMaterialInWareHouseInfo(
                        materialInWareHouseId, userId, userName));
    }

    /**
     * 消耗物品的领用
     * @param dto
     * @return
     */
    @RequestMapping(value = "/consume", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createConsumeEvent(@RequestBody DoctorConsumeProviderInputDto dto){
        DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto = null;
        try{

            DoctorMaterialInWareHouse doctorMaterialInWareHouse = RespHelper.orServEx(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(
                    dto.getFarmId(),dto.getMaterialId(),dto.getWareHouseId()));
            checkState(!isNull(doctorMaterialInWareHouse), "input.materialInfo.error");

            String barnName = RespHelper.orServEx(doctorBarnReadService.findBarnById(dto.getBarnId())).getName();

            Response<User> userResponse = userReadService.findById(dto.getFeederId());
            String userName = RespHelper.orServEx(userResponse).getName();

            DoctorMaterialInfo doctorMaterialInfo = RespHelper.orServEx(doctorMaterialInfoReadService.queryById(dto.getMaterialId()));

            doctorMaterialConsumeProviderDto = DoctorMaterialConsumeProviderDto.builder()
                    .actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.CONSUMER.getValue())
                    .farmId(doctorMaterialInWareHouse.getFarmId()).farmName(doctorMaterialConsumeProviderDto.getFarmName())
                    .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                    .materialTypeId(doctorMaterialInWareHouse.getMaterialId()).materialName(doctorMaterialInWareHouse.getMaterialName())
                    .barnId(dto.getBarnId()).barnName(barnName).staffId(dto.getFeederId()).staffName(userName)
                    .count(dto.getCount()).consumeDays(dto.getConsumeDays())
                    .unitId(doctorMaterialInfo.getUnitId()).unitName(doctorMaterialInfo.getUnitName())
                    .build();
        }catch (Exception e){
            log.error("consume material fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.consumeMaterialInfo(doctorMaterialConsumeProviderDto));
    }

    /**
     * 录入物品数据信息
     * @param dto
     * @return
     */
    @RequestMapping(value = "/provider", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long createProviderEvent(@RequestBody DoctorConsumeProviderInputDto dto){
        DoctorMaterialConsumeProviderDto doctorMaterialConsumeProviderDto = null;
        try{

            DoctorMaterialInWareHouse doctorMaterialInWareHouse = RespHelper.orServEx(doctorMaterialInWareHouseReadService.queryByMaterialWareHouseIds(
                    dto.getFarmId(),dto.getMaterialId(),dto.getWareHouseId()));
            checkState(!isNull(doctorMaterialInWareHouse), "input.materialInfo.error");

            String barnName = RespHelper.orServEx(doctorBarnReadService.findBarnById(dto.getBarnId())).getName();

            Response<User> userResponse = userReadService.findById(dto.getFeederId());
            String userName = RespHelper.orServEx(userResponse).getName();

            DoctorMaterialInfo doctorMaterialInfo = RespHelper.orServEx(doctorMaterialInfoReadService.queryById(dto.getMaterialId()));

            doctorMaterialConsumeProviderDto = DoctorMaterialConsumeProviderDto.builder()
                    .actionType(DoctorMaterialConsumeProvider.EVENT_TYPE.PROVIDER.getValue())
                    .farmId(doctorMaterialInWareHouse.getFarmId()).farmName(doctorMaterialConsumeProviderDto.getFarmName())
                    .wareHouseId(doctorMaterialInWareHouse.getWareHouseId()).wareHouseName(doctorMaterialInWareHouse.getWareHouseName())
                    .materialTypeId(doctorMaterialInWareHouse.getMaterialId()).materialName(doctorMaterialInWareHouse.getMaterialName())
                    .barnId(dto.getBarnId()).barnName(barnName).staffId(dto.getFeederId()).staffName(userName)
                    .count(dto.getCount()).unitId(doctorMaterialInfo.getUnitId()).unitName(doctorMaterialInfo.getUnitName())
                    .build();
        }catch (Exception e){
            log.error("provider material fail, cause:{}", Throwables.getStackTraceAsString(e));
            throw new JsonResponseException(e.getMessage());
        }
        return RespHelper.or500(doctorMaterialInWareHouseWriteService.providerMaterialInfo(doctorMaterialConsumeProviderDto));
    }
}
