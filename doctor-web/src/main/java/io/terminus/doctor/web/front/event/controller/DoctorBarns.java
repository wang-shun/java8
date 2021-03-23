package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.PigSearchType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBarnCountForPigTypeDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.search.SearchedBarn;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.handler.DoctorEventSelector;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.*;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.model.PrimaryUser;
import io.terminus.doctor.user.service.*;
import io.terminus.doctor.web.component.DoctorSearches;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
import io.terminus.doctor.web.front.event.dto.DoctorBarnDetail;
import io.terminus.doctor.web.front.event.dto.DoctorBarnSelect;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.terminus.common.utils.Arguments.*;

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

    @RpcConsumer
    private DoctorGroupWriteService doctorGroupWriteService;
    @RpcConsumer
    private DoctorPigWriteService doctorPigWriteService;

    @Autowired
    private DoctorSearches doctorSearches;

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

    @RequestMapping(method = RequestMethod.GET, value = "has-child")
    public boolean hasChildPig(@RequestParam Long barnId) {

        return RespHelper.or500(doctorGroupReadService.findGroupPigQuantityByBarnId(barnId)) != 0;


//        List<DoctorPigInfoDto> pigInfoDtos = RespHelper.or500(doctorPigReadService.queryDoctorPigInfoByBarnId(barnId));
//        for (DoctorPigInfoDto pig : pigInfoDtos) {
//            if (pig.getPigType() == PigType.NURSERY_PIGLET.getValue() ||
//                    pig.getPigType() == PigType.FATTEN_PIG.getValue() ||
//                    pig.getPigType() == PigType.RESERVE.getValue())
//                return true;
//        }
//        return false;
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
     * @param farmId     猪场id
     * @param barnStatus 筛选猪舍状态
     * @param pigIds     猪id 逗号分隔
     * @return 猪舍表列表
     * @see DoctorBarn.Status
     */
    @RequestMapping(value = "/farmId", method = RequestMethod.GET)
    public List<DoctorBarn> findBarnsByfarmId(@RequestParam("farmId") Long farmId,
                                              @RequestParam(value = "status", required = false) Integer barnStatus,
                                              @RequestParam(value = "pigIds", required = false) String pigIds) {
        List<DoctorBarn> barnList = filterBarnByPigIds(RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, null,
                null, barnStatus, doctorFarmAuthCenter.getAuthBarnIds())), pigIds);
        if (Arguments.isNullOrEmpty(barnList)) {
            return Collections.emptyList();
        }
        return barnList.stream().sorted((barn1, barn2) -> PigType.compareTo(barn1.getPigType(), barn2.getPigType()))
                .collect(Collectors.toList());
    }

    /**
     * 查询猪群的猪舍表
     *
     * @param farmId     猪场id
     * @param barnStatus 筛选猪舍状态
     * @return 猪舍表列表
     * @see DoctorBarn.Status
     */
    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public List<DoctorBarn> findGroupBarnsByfarmId(@RequestParam("farmId") Long farmId,
                                                   @RequestParam(value = "status", required = false) Integer barnStatus) {
        return RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, PigType.GROUP_TYPES,
                null, barnStatus, doctorFarmAuthCenter.getAuthBarnIds()));
    }

    /**
     * 根据farmIds查询猪舍表, 根据pigIds过滤
     *
     * @param farmIds 猪场id
     * @param pigIds  猪id 逗号分隔
     * @return 猪舍表列表
     */
    @RequestMapping(value = "/farmIds", method = RequestMethod.GET)
    public List<DoctorBarnSelect> findBarnsByfarmIds(@RequestParam("farmIds") List<Long> farmIds,
                                                     @RequestParam(value = "pigIds", required = false) String pigIds,
                                                     @RequestParam(required = false) Long roleId) {
        List<DoctorBarn> barns = filterBarnByPigIds(RespHelper.or500(doctorBarnReadService.findBarnsByFarmIds(farmIds)), pigIds);
        List<DoctorBarnSelect> barnSelects = BeanMapper.mapList(barns, DoctorBarnSelect.class);

        if (roleId != null) {
            Map<String, Object> extra = RespHelper.or500(subRoleReadService.findById(roleId)).getExtra();
            if (extra != null && extra.get("defaultBarnType") != null) {
                List<Integer> barnType = (List<Integer>) extra.get("defaultBarnType");
                barnSelects.forEach(select -> select.setSelect(barnType.contains(select.getPigType())));
            }
        }

        return barnSelects;
    }

    /**
     * 根据farmid查询非空猪舍
     *
     * @param farmId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/using")
    public List<SearchedBarn> findBarnsByEnums(@RequestParam Long farmId) {
        //得到猪场下正在使用的猪舍
        List<DoctorBarn> barns = RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, null, null, DoctorBarn.Status.USING.getValue(), null));
        List<SearchedBarn> notEmptyBarns=doctorSearches.getSearchedBarn(barns).stream().filter(b -> b.getPigCount() > 0 || b.getPigGroupCount() > 0).collect(Collectors.toList());
        return notEmptyBarns;
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
                                                     @RequestParam(value = "status", required = false) Integer status,
                                                     @RequestParam(value = "pigIds", required = false) String pigIds) {
        return filterBarnByPigIds(RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, Lists.newArrayList(pigType),
                null, status, doctorFarmAuthCenter.getAuthBarnIds())), pigIds);
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
                                                     @RequestParam(value = "status", required = false) Integer status,
                                                     @RequestParam(value = "pigIds", required = false) String pigIds) {
        List<Integer> types = Lists.newArrayList();
        if (notEmpty(pigTypes)) {
            types = Splitters.splitToInteger(pigTypes, Splitters.COMMA);
        }
        return filterBarnByPigIds(RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, types,
                null, status, null)), pigIds);
    }

    /**
     * 根据farmId和当前用户查猪舍
     * 冯雨晴 2019.9.18
     * @param farmId   猪场id
     * @return 猪舍表列表
     */
    @RequestMapping(value = "/pigTypess", method = RequestMethod.GET)
    public List<Map> findBarnsByfarmIdAndTypes(@RequestParam("farmId") Long farmId,
                                               @RequestParam(value = "status", required = false) Integer status,
                                               @RequestParam(value = "pigTypes", required = false) String pigTypes
                                                    ) {
        BaseUser baseUser = UserUtil.getCurrentUser();
        if (baseUser == null) {
            throw new JsonResponseException("user.not.login");
        }
        Response<DoctorUserDataPermission> dataPermissionResponse = doctorUserDataPermissionReadService.findDataPermissionByUserId(baseUser.getId());
        if (!dataPermissionResponse.isSuccess()) {
            throw new JsonResponseException("user.not.permission");
        }
        List<Long> barnIds = dataPermissionResponse.getResult().getBarnIdsList();

        List<Integer> types = Lists.newArrayList();
        if (notEmpty(pigTypes)) {
            types = Splitters.splitToInteger(pigTypes, Splitters.COMMA);
        }
        return RespHelper.or500(doctorBarnReadService.findBarnsByEnumss(farmId,types,status,barnIds));

    }


    /**
     * 猪舍批量转舍时, 根据猪id, 求一下可转猪舍的交集
     *
     * @param farmId    猪场id
     * @param eventType 事件类型
     * @param pigIds    猪ids 逗号分隔
     * @return 猪舍list
     * @see io.terminus.doctor.event.enums.PigEvent
     */
    @RequestMapping(value = "/pigTypes/trans", method = RequestMethod.GET)
    public List<DoctorBarn> findBarnsByfarmIdAndTypeWhenBatchTransBarn(@RequestParam("farmId") Long farmId,
                                                                       @RequestParam("eventType") Integer eventType,
                                                                       @RequestParam(value = "status", required = false) Integer status,
                                                                       @RequestParam("pigIds") String pigIds) {
        List<Integer> barnTypes;
        if (Objects.equals(eventType, PigEvent.CHG_LOCATION.getKey())) {
            barnTypes = getTransBarnTypes(pigIds);
        } else {
            //转舍类型: 普通转舍, 去配种, 去分娩, 其他报错
            throw new JsonResponseException("not.trans.barn.type");
        }
        return notEmpty(barnTypes) ? RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, barnTypes,
                null, status, doctorFarmAuthCenter.getAuthBarnIds())) : Collections.emptyList();
    }

    //普通转舍 转入猪舍类型
    private List<Integer> getTransBarnTypes(String pigIds) {
        List<Integer> barnTypes = Lists.newArrayList(PigType.ALL_TYPES);
        //遍历求猪舍类型交集
        for (Long pigId : Splitters.splitToLong(pigIds, Splitters.COMMA)) {
            DoctorBarn pigBarn = RespHelper.or500(doctorPigReadService.findBarnByPigId(pigId));
            DoctorPigTrack doctorPigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
            if (Objects.equals(pigBarn.getPigType(), PigType.MATE_SOW.getValue())) {
                barnTypes.retainAll(PigType.MATING_TYPES);
            } else if (Objects.equals(pigBarn.getPigType(), PigType.PREG_SOW.getValue())) {
                if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Pregnancy.getKey())) {
                    barnTypes.retainAll(PigType.MATING_FARROW_TYPES);
                } else {
                    barnTypes.retainAll(PigType.MATING_TYPES);
                }
            } else if (PigType.FARROW_TYPES.contains(pigBarn.getPigType())) {
                if (Objects.equals(doctorPigTrack.getStatus(), PigStatus.Wean.getKey())) {
                    barnTypes.retainAll(PigType.MATING_FARROW_TYPES);
                } else {
                    barnTypes.retainAll(PigType.FARROW_TYPES);
                }
            } else if (PigType.HOUBEI_TYPES.contains(pigBarn.getPigType())) {
                barnTypes.retainAll(PigType.HOUBEI_TYPES);
            } else {
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
        if (isEmpty(barn.getName())) {
            throw new JsonResponseException("barn.name.not.empty");
        }
        DoctorBarn doctorBarn = RespHelper.or500(doctorBarnReadService.findBarnByFarmAndBarnName(barn.getFarmId(), barn.getName()));

        if (barn.getId() == null) {
            if (notNull(doctorBarn)) {
                throw new JsonResponseException("barn.name.has.existed");
            }
            barn.setStatus(DoctorBarn.Status.USING.getValue());     //初始猪舍状态: 在用
            barn.setCanOpenGroup(DoctorBarn.CanOpenGroup.YES.getValue());  //初始是否可建群: 可建群
            barnId = RespHelper.or500(doctorBarnWriteService.createBarn(barn));

            //更新 数据权限
            this.addBarnIdDataPermissionForPrimary(barnId, barn.getFarmId());
            if (Objects.equals(user.getType(), UserType.FARM_SUB.value())) {
                DoctorUserDataPermission permission = RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId()));
                this.addBarnId2DataPermission(barnId, permission);
            }
        } else {
            if (notNull(doctorBarn) && !Objects.equals(doctorBarn.getId(), barn.getId())) {
                throw new JsonResponseException("barn.name.has.existed");
            }
            barnId = barn.getId();
            DoctorBarn oldBarn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
            //判断猪舍是否能够停用
            if (Objects.equals(barn.getStatus(), DoctorBarn.Status.NOUSE.getValue())) {
                if (RespHelper.or500(doctorBarnReadService.countPigByBarnId(barn.getId())) > 0) {
                    throw new JsonResponseException("barn.forbid.fail");
                }
            }
            //判断猪舍是否允许修改类型
            if (barn.getPigType() != null && !Objects.equals(barn.getPigType(), oldBarn.getPigType())) {
                if (!RespHelper.or500(doctorPigReadService.findActivePigTrackByCurrentBarnId(barnId)).isEmpty()
                        || !RespHelper.or500(doctorGroupReadService.findGroupByCurrentBarnId(barnId)).isEmpty()) {
                    throw new JsonResponseException("barn.type.forbid.update");
                }
            }
            RespHelper.or500(doctorBarnWriteService.updateBarn(barn));

            //修改猪舍名称
            if (!Objects.equals(barn.getName(), oldBarn.getName())) {
                RespHelper.or500(doctorGroupWriteService.updateCurrentBarnName(barn.getId(), barn.getName()));
                RespHelper.or500(doctorPigWriteService.updateCurrentBarnName(barn.getId(), barn.getName()));
            }

            if (!Objects.equals(barn.getStaffId(), oldBarn.getStaffId())) {
                RespHelper.or500(doctorGroupWriteService.updateStaffName(barn.getId(), barn.getStaffId(), barn.getStaffName()));
            }
        }
        return barnId;
    }

    private void addBarnIdDataPermissionForPrimary(Long barnId, Long farmId) {
        List<PrimaryUser> allPrimary = RespHelper.or500(primaryUserReadService.findAllPrimaryUser());
        List<Long> userIds = allPrimary.stream().map(PrimaryUser::getUserId).collect(Collectors.toList());
        List<DoctorUserDataPermission> permissions = RespHelper.or500(doctorUserDataPermissionReadService.findByFarmAndPrimary(farmId, userIds));
        permissions.add(RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(10L)));
        permissions.forEach(permission -> addBarnId2DataPermission(barnId, permission));

    }

    private void addBarnId2DataPermission(Long barnId, DoctorUserDataPermission permission) {
        List<Long> barnIds = permission.getBarnIdsList();
        if (barnIds == null) {
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
                .pigType(DoctorPig.PigSex.SOW.getKey())
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
                    .pigType(DoctorPig.PigSex.BOAR.getKey())
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


    /**
     * 查询可以转种猪猪舍
     *
     * @param farmId  转入的猪场
     * @param groupId 当前猪群id
     * @return 可以转入的猪舍
     */
    @RequestMapping(value = "/findAvailablePigBarns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<DoctorBarn> findAvailablePigBarns(@RequestParam("farmId") Long farmId,
                                               @RequestParam("groupId") Long groupId) {
        return RespHelper.orServEx(doctorBarnReadService.findAvailablePigBarns(farmId, groupId));
    }



    @RequestMapping(value = "/updateBarnName", method = RequestMethod.POST)
    public Boolean updateBarnName(@RequestParam Long barnId, @RequestParam String barnName) {
        Long groupEvent = RespHelper.or500(doctorGroupReadService.countByBarnId(barnId));
        Long pigEvent = RespHelper.or500(doctorPigEventReadService.countByBarnId(barnId));
        if (Objects.equals(groupEvent, 0L) && Objects.equals(pigEvent, 0L)) {
            DoctorBarn barn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
            if (barn == null) {
                throw new JsonResponseException("barn.not.found");
            }
            if (barnName == null || barnName.trim().isEmpty()) {
                throw new JsonResponseException("barn.name.not.null");
            }
            barn.setName(barnName);
            RespHelper.or500(doctorBarnWriteService.updateBarn(barn));
            RespHelper.or500(doctorGroupWriteService.updateCurrentBarnName(barnId, barnName));
            return RespHelper.or500(doctorPigWriteService.updateCurrentBarnName(barnId, barnName));
        } else {
            throw new JsonResponseException("barn.has.event.forbid.update.name");
        }
    }

    /**
     * 统计每种猪舍类型猪舍数量
     *
     * @param params 查询条件
     * @return
     */
    @RequestMapping(value = "/countForTypes", method = RequestMethod.GET)
    public DoctorBarnCountForPigTypeDto countForTypes(@RequestParam(value = "farmId", required = false) Long farmId,
                                                      @RequestParam Map<String, Object> params) {
        if (farmId == null) {
            return new DoctorBarnCountForPigTypeDto();
        }
        doctorFarmAuthCenter.checkFarmAuth(farmId);
        BaseUser baseUser = UserUtil.getCurrentUser();
        params.put("userId",baseUser.getId());
        params.put("farmId", farmId);
        if(params.get("status")==null ||params.get("status").equals("")){
            params.put("status",1);
        }
        return RespHelper.or500(doctorBarnReadService.countForTypes(params));
    }

    /**
     * 查询可转猪舍
     *
     * @param pigIds 猪id
     * @return
     */
    @RequestMapping(value = "/selectBarns", method = RequestMethod.GET)
    public List<DoctorBarn> selectChgLocationBarn(@RequestParam String pigIds, @RequestParam Long farmId) {
        List<Long> pigIdList = Splitters.COMMA.splitToList(pigIds).stream().map(Long::parseLong).collect(Collectors.toList());
        if (Arguments.isNullOrEmpty(pigIdList)) {
            return Collections.emptyList();
        }
        List<Integer> barnType = Lists.newArrayList();
        barnType.addAll(PigType.PIG_TYPES);
        pigIdList.forEach(pigId -> {
            DoctorPigTrack pigTrack = RespHelper.or500(doctorPigReadService.findPigTrackByPigId(pigId));
            barnType.retainAll(DoctorEventSelector.selectBarn(PigStatus.from(pigTrack.getStatus()), PigType.from(pigTrack.getCurrentBarnType()))
                    .stream().map(PigType::getValue).collect(Collectors.toList()));
        });
        if (barnType.isEmpty()) {
            return Collections.emptyList();
        }
        return RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, barnType, null, DoctorBarn.Status.USING.getValue(), null));
    }

    /**
     * 获取断奶触发的转舍可转猪舍
     *
     * @param farmId 猪场id
     * @return 可转猪舍列表
     */
    @RequestMapping(value = "/selectChgBarnFromWean", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorBarn> selectChgBarnFromWean(@RequestParam Long farmId) {
        List<Integer> barnType = Lists.newArrayList(PigType.MATING_FARROW_TYPES);
        return RespHelper.or500(doctorBarnReadService.findBarnsByEnums(farmId, barnType, null, DoctorBarn.Status.USING.getValue(), null));
    }

    /**
     * 获取所有猪舍
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<DoctorBarn> findAllBarn(@RequestParam Long farmId) {
        return RespHelper.or500(doctorBarnReadService.findBarnsByFarmId(farmId));
    }
}