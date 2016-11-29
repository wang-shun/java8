package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.PigSearchType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorBarnWriteService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionWriteService;
import io.terminus.doctor.user.service.PrimaryUserReadService;
import io.terminus.doctor.user.service.SubRoleReadService;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
import io.terminus.doctor.web.front.event.dto.DoctorBarnDetail;
import io.terminus.doctor.web.front.event.dto.DoctorBarnSelect;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 猪舍表Controller
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-24
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/barn")
public class DoctorBarns {

    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorBarnWriteService doctorBarnWriteService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorPigReadService doctorPigReadService;
    private final DoctorPigEventReadService doctorPigEventReadService;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorFarmAuthCenter doctorFarmAuthCenter;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    private final DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService;
    private final PrimaryUserReadService primaryUserReadService;
    private final SubRoleReadService subRoleReadService;

    @Autowired
    public DoctorBarns(DoctorBarnReadService doctorBarnReadService,
                       DoctorBarnWriteService doctorBarnWriteService,
                       DoctorFarmReadService doctorFarmReadService,
                       DoctorPigReadService doctorPigReadService,
                       DoctorGroupReadService doctorGroupReadService,
                       DoctorFarmAuthCenter doctorFarmAuthCenter,
                       DoctorPigEventReadService doctorPigEventReadService,
                       DoctorUserDataPermissionWriteService doctorUserDataPermissionWriteService,
                       DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                       PrimaryUserReadService primaryUserReadService,
                       SubRoleReadService subRoleReadService) {
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorBarnWriteService = doctorBarnWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorFarmAuthCenter = doctorFarmAuthCenter;
        this.doctorPigEventReadService = doctorPigEventReadService;
        this.doctorUserDataPermissionWriteService = doctorUserDataPermissionWriteService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.primaryUserReadService = primaryUserReadService;
        this.subRoleReadService = subRoleReadService;
    }

    /**
     * 根据id查询猪舍里猪的数量
     *
     * @param barnId 主键id
     * @return 猪存栏数量
     */
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Integer countPigByBarnId(@RequestParam("barnId") Long barnId) {
        return RespHelper.or500(doctorBarnReadService.countPigByBarnId(barnId));
    }

    /**
     * 根据id查询猪舍表
     *
     * @param barnId 主键id
     * @return 猪舍表
     */
    @RequestMapping(value = "/id", method = RequestMethod.GET)
    public DoctorBarn findBarnById(@RequestParam("barnId") Long barnId) {
        return RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
    }

    /**
     * 根据farmId查询猪舍表, 根据pigIds过滤
     *
     * @param farmId 猪场id
     * @param pigIds 猪id 逗号分隔
     * @return 猪舍表列表
     */
    @RequestMapping(value = "/farmId", method = RequestMethod.GET)
    public List<DoctorBarn> findBarnsByfarmId(@RequestParam("farmId") Long farmId,
                                              @RequestParam(value = "pigIds", required = false) String pigIds) {
        return filterBarnByPigIds(RespHelper.or500(doctorBarnReadService.findBarnsByFarmId(farmId)), pigIds);
    }

    /**
     * 根据farmIds查询猪舍表, 根据pigIds过滤
     *
     * @param farmIds 猪场id
     * @param pigIds 猪id 逗号分隔
     * @return 猪舍表列表
     */
    @RequestMapping(value = "/farmIds", method = RequestMethod.GET)
    public List<DoctorBarnSelect> findBarnsByfarmIds(@RequestParam("farmIds") List<Long> farmIds,
                                                     @RequestParam(value = "pigIds", required = false) String pigIds,
                                                     @RequestParam(required = false) Long roleId) {
        List<DoctorBarn> barns = filterBarnByPigIds(RespHelper.or500(doctorBarnReadService.findBarnsByFarmIds(farmIds)), pigIds);
        List<DoctorBarnSelect> barnSelects = BeanMapper.mapList(barns, DoctorBarnSelect.class);

        if(roleId != null){
            Map<String, Object> extra = RespHelper.or500(subRoleReadService.findById(roleId)).getExtra();
            if(extra != null && extra.get("defaultBarnType") != null){
                List<Integer> barnType = (List<Integer>) extra.get("defaultBarnType");
                barnSelects.forEach(select -> {
                    if(barnType.contains(select.getPigType())){
                        select.setSelect(true);
                    }else{
                        select.setSelect(false);
                    }
                });
            }
        }

        return barnSelects;
    }

    /**
     * 根据farmId和状态查询猪舍表
     *
     * @param farmId  猪场id
     * @param pigType 猪舍类别
     * @param pigIds  猪id 逗号分隔
     * @return 猪舍表列表
     * @see PigType
     */
    @RequestMapping(value = "/pigType", method = RequestMethod.GET)
    public List<DoctorBarn> findBarnsByfarmIdAndType(@RequestParam("farmId") Long farmId,
                                                     @RequestParam("pigType") Integer pigType,
                                                     @RequestParam(value = "pigIds", required = false) String pigIds) {
        return filterBarnByPigIds(RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, pigType, null, null)), pigIds);
    }

    //根据猪id过滤猪舍: 取出猪的猪舍type, 过滤一把
    private List<DoctorBarn> filterBarnByPigIds(List<DoctorBarn> barns, String pigIds) {
        if (barns == null || isEmpty(pigIds)) {
            return MoreObjects.firstNonNull(barns, Lists.<DoctorBarn>newArrayList());
        }

        List<Integer> barnTypes = Splitters.splitToLong(pigIds, Splitters.COMMA).stream()
                .map(pigId -> RespHelper.or500(doctorPigReadService.findBarnByPigId(pigId)).getPigType())
                .collect(Collectors.toList());
        return barns.stream().filter(barn -> barnTypes.contains(barn.getPigType())).collect(Collectors.toList());
    }

    /**
     * 根据farmId和状态查询猪舍表
     *
     * @param farmId   猪场id
     * @param pigTypes 猪舍类别 逗号分隔
     * @param pigIds   根据猪id过滤
     * @return 猪舍表列表
     * @see PigType
     */
    @RequestMapping(value = "/pigTypes", method = RequestMethod.GET)
    public List<DoctorBarn> findBarnsByfarmIdAndType(@RequestParam("farmId") Long farmId,
                                                     @RequestParam(value = "pigTypes", required = false) String pigTypes,
                                                     @RequestParam(value = "pigIds", required = false) String pigIds) {
        List<Integer> types = Lists.newArrayList();
        if (notEmpty(pigTypes)) {
            types = Splitters.splitToInteger(pigTypes, Splitters.COMMA);
        }
        return filterBarnByPigIds(RespHelper.or500(doctorBarnReadService.findBarnsByFarmIdAndPigTypes(farmId, types)), pigIds);
    }


    /**
     * 猪舍批量转舍时, 根据猪id, 求一下可转猪舍的交集
     * @param farmId    猪场id
     * @param eventType 事件类型
     * @see io.terminus.doctor.event.enums.PigEvent
     * @param pigIds    猪ids 逗号分隔
     * @return 猪舍list
     */
    @RequestMapping(value = "/pigTypes/trans", method = RequestMethod.GET)
    public List<DoctorBarn> findBarnsByfarmIdAndTypeWhenBatchTransBarn(@RequestParam("farmId") Long farmId,
                                                                       @RequestParam("eventType") Integer eventType,
                                                                       @RequestParam("pigIds") String pigIds) {
        List<Integer> barnTypes;
        if (Objects.equals(eventType, PigEvent.CHG_LOCATION.getKey())) {
            barnTypes = getTransBarnTypes(pigIds);
        } else if (Objects.equals(eventType, PigEvent.TO_MATING.getKey())) {
            barnTypes = PigType.MATING_TYPES;
        } else if (Objects.equals(eventType, PigEvent.TO_FARROWING.getKey())) {
            barnTypes = PigType.FARROW_TYPES;
        } else {
            //转舍类型: 普通转舍, 去配种, 去分娩, 其他报错
            throw new JsonResponseException("not.trans.barn.type");
        }
        return notEmpty(barnTypes) ? RespHelper.or500(doctorBarnReadService.findBarnsByFarmIdAndPigTypes(farmId, barnTypes))
                : Collections.emptyList();
    }

    //普通转舍 转入猪舍类型
    private List<Integer> getTransBarnTypes(String pigIds) {
        List<Integer> barnTypes = Lists.newArrayList(PigType.ALL_TYPES);
        //遍历求猪舍类型交集
        for (Long pigId : Splitters.splitToLong(pigIds, Splitters.COMMA)) {
            DoctorBarn pigBarn = RespHelper.or500(doctorPigReadService.findBarnByPigId(pigId));
            if (PigType.MATING_TYPES.contains(pigBarn.getPigType())) {
                barnTypes.retainAll(PigType.MATING_TYPES);
            }
            else if (PigType.FARROW_TYPES.contains(pigBarn.getPigType())) {
                barnTypes.retainAll(PigType.FARROW_TYPES);
            }
            else if (PigType.HOUBEI_TYPES.contains(pigBarn.getPigType())) {
                barnTypes.retainAll(PigType.HOUBEI_TYPES);
            }
            else {
                barnTypes.retainAll(Lists.newArrayList(pigBarn.getPigType()));
            }
        }
        return barnTypes;
    }

    /**
     * 创建或更新DoctorBarn
     *
     * @return 是否成功
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createOrUpdateBarn(@RequestBody DoctorBarn barn) {
        checkNotNull(barn, "barn.not.null");

        //权限中心校验权限
        BaseUser user = doctorFarmAuthCenter.checkFarmAuth(barn.getFarmId());

        Long barnId;

        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(barn.getFarmId()));
        barn.setOrgId(farm.getOrgId());
        barn.setOrgName(farm.getOrgName());
        barn.setFarmName(farm.getName());

        if (barn.getId() == null) {
            barn.setStatus(DoctorBarn.Status.USING.getValue());     //初始猪舍状态: 在用
            barn.setCanOpenGroup(DoctorBarn.CanOpenGroup.YES.getValue());  //初始是否可建群: 可建群
            barnId = RespHelper.or500(doctorBarnWriteService.createBarn(barn));

            //更新 数据权限
            this.addBarnId2DataPermission(barnId, user.getId());
            if(Objects.equals(user.getType(), UserType.FARM_SUB.value())){
                this.addBarnId2DataPermission(barnId, RespHelper.or500(primaryUserReadService.findSubByUserId(user.getId())).getParentUserId());
            }
        } else {
            barnId = barn.getId();
            DoctorBarn oldBarn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
            //是否容许修改猪舍名字
            if (StringUtils.isNotBlank(barn.getName()) && !barn.getName().equals(oldBarn.getName())) {
                Long groupEvent = RespHelper.or500(doctorGroupReadService.countByBarnId(barnId));
                Long pigEvent = RespHelper.or500(doctorPigEventReadService.countByBarnId(barnId));
                if (groupEvent + pigEvent > 0L) {
                    throw new JsonResponseException("barn.has.event.forbid.update.name");
                }
            }
            //判断猪舍是否能够停用
            if (Objects.equals(barn.getStatus(), DoctorBarn.Status.NOUSE.getValue())){
                if (RespHelper.or500(doctorBarnReadService.countPigByBarnId(barn.getId())) > 0){
                    throw new JsonResponseException("barn.forbid.fail");
                }
            }
            //判断猪舍是否允许修改类型
            if(barn.getPigType() != null && !Objects.equals(barn.getPigType(), oldBarn.getPigType())){
                if(!RespHelper.or500(doctorPigReadService.findActivePigTrackByCurrentBarnId(barnId)).isEmpty()){
                    throw new JsonResponseException("barn.type.forbid.update");
                }
            }
            RespHelper.or500(doctorBarnWriteService.updateBarn(barn));
        }
        return barnId;
    }

    private void addBarnId2DataPermission(Long barnId, Long userId){
        DoctorUserDataPermission permission = RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
        List<Long> barnIds = permission.getBarnIdsList();
        if(barnIds == null){
            barnIds = Lists.newArrayList();
        }
        barnIds.add(barnId);
        permission.setBarnIds(Joiner.on(",").join(barnIds));
        RespHelper.or500(doctorUserDataPermissionWriteService.updateDataPermission(permission));
    }

    /**
     * 更新猪舍状态
     *
     * @return 是否成功
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    public Boolean updateBarnStatus(@RequestParam("barnId") Long barnId,
                                    @RequestParam("status") Integer status) {
        DoctorBarn barn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));

        //权限中心校验权限
        doctorFarmAuthCenter.checkFarmAuth(barn.getFarmId());

        return RespHelper.or500(doctorBarnWriteService.updateBarnStatus(barnId, status));
    }

    /**
     * 查询猪舍详情
     *
     * @param barnId 主键id
     * @return 猪舍表
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public DoctorBarnDetail findBarnDetailByBarnId(@RequestParam("barnId") Long barnId,
                                                   @RequestParam(value = "status", required = false) Integer status,
                                                   @RequestParam(value = "pageNo", required = false) Integer pageNo,
                                                   @RequestParam(value = "size", required = false) Integer size) {
        DoctorBarn barn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
        DoctorBarnDetail barnDetail = new DoctorBarnDetail();

        Paging<DoctorGroupDetail> groupPaging = getGroupPaging(barn, status, pageNo, size);
        Paging<DoctorPigInfoDto> pigPaging = RespHelper.or500(doctorPigReadService.pagingDoctorInfoDtoByPigTrack(DoctorPigTrack.builder()
                .status(status)
                .currentBarnId(barnId)
                .pigType(DoctorPig.PIG_TYPE.SOW.getKey())
                .farmId(barn.getFarmId()).build(), pageNo, size));

        //如果母猪和猪群都存在, 全部返回, 由前台选择取哪里的数据
        if (groupPaging.getTotal() != 0 && pigPaging.getTotal() != 0) {
            barnDetail.setType(PigSearchType.SOW_GROUP.getValue());

            //猪群
            barnDetail.setGroupPaging(groupPaging);
            barnDetail.setGroupType(barn.getPigType());

            //母猪
            barnDetail.setPigPaging(pigPaging);
            barnDetail.setStatuses(RespHelper.or500(doctorPigReadService.findPigStatusByBarnId(barnId)));
            return barnDetail;
        }

        //猪群舍(实际情况: 分娩母猪舍里也有猪群)
        if (PigType.isGroup(barn.getPigType())) {
            barnDetail.setType(PigSearchType.GROUP.getValue());

            barnDetail.setGroupType(barn.getPigType());
            barnDetail.setGroupPaging(groupPaging);
            return barnDetail;
        }

        //公猪舍
        if (PigType.isBoar(barn.getPigType())) {
            barnDetail.setType(PigSearchType.BOAR.getValue());
            barnDetail.setPigPaging(RespHelper.or500(doctorPigReadService.pagingDoctorInfoDtoByPigTrack(DoctorPigTrack.builder()
                    .status(status)
                    .currentBarnId(barnId)
                    .pigType(DoctorPig.PIG_TYPE.BOAR.getKey())
                    .farmId(barn.getFarmId()).build(), pageNo, size)));
            barnDetail.setStatuses(RespHelper.or500(doctorPigReadService.findPigStatusByBarnId(barnId)));
            return barnDetail;
        }

        //母猪舍
        if (PigType.isSow(barn.getPigType())) {
            barnDetail.setType(PigSearchType.SOW.getValue());
            barnDetail.setPigPaging(pigPaging);
            barnDetail.setStatuses(RespHelper.or500(doctorPigReadService.findPigStatusByBarnId(barnId)));
            return barnDetail;
        }
        return barnDetail;
    }

    private Paging<DoctorGroupDetail> getGroupPaging(DoctorBarn barn, Integer status, Integer pageNo, Integer size) {
        DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
        searchDto.setFarmId(barn.getFarmId());
        searchDto.setCurrentBarnId(barn.getId());
        searchDto.setPigType(status);   //这里的状态就是猪群的猪类
        return RespHelper.or500(doctorGroupReadService.pagingGroup(searchDto, pageNo, size));
    }


    /**
     * 查询可以转入的猪舍
     *
     * @param farmId  转入的猪场
     * @param groupId 当前猪群id
     * @return 可以转入的猪舍
     */
    @RequestMapping(value = "/findAvailableBarns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorBarn> findAvailableBarns(@RequestParam("farmId") Long farmId,
                                               @RequestParam("groupId") Long groupId) {
        return RespHelper.orServEx(doctorBarnReadService.findAvailableBarns(farmId, groupId));
    }

    @RequestMapping(value = "/updateBarnName", method = RequestMethod.POST)
    public Boolean updateBarnName(@RequestParam Long barnId, @RequestParam String barnName){
        Long groupEvent = RespHelper.or500(doctorGroupReadService.countByBarnId(barnId));
        Long pigEvent = RespHelper.or500(doctorPigEventReadService.countByBarnId(barnId));
        if(Objects.equals(groupEvent, 0L) && Objects.equals(pigEvent, 0L)){
            DoctorBarn barn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
            if(barn == null){
                throw new JsonResponseException("barn.not.found");
            }
            if(barnName == null || barnName.trim().isEmpty()){
                throw new JsonResponseException("barn.name.not.null");
            }
            barn.setName(barnName);
            return RespHelper.or500(doctorBarnWriteService.updateBarn(barn));
        }else{
            throw new JsonResponseException("barn.has.event.forbid.update.name");
        }
    }

}