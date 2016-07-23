package io.terminus.doctor.web.front.event.controller;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.PigSearchType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorBarnWriteService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.web.front.auth.DoctorFarmAuthCenter;
import io.terminus.doctor.web.front.event.dto.DoctorBarnDetail;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorFarmAuthCenter doctorFarmAuthCenter;

    @Autowired
    public DoctorBarns(DoctorBarnReadService doctorBarnReadService,
                       DoctorBarnWriteService doctorBarnWriteService,
                       DoctorFarmReadService doctorFarmReadService,
                       DoctorPigReadService doctorPigReadService,
                       DoctorGroupReadService doctorGroupReadService,
                       DoctorFarmAuthCenter doctorFarmAuthCenter) {
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorBarnWriteService = doctorBarnWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorFarmAuthCenter = doctorFarmAuthCenter;
    }

    /**
     * 根据id查询猪舍里猪的数量
     * @param barnId 主键id
     * @return 猪存栏数量
     */
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Integer countPigByBarnId(@RequestParam("barnId") Long barnId) {
        return RespHelper.or500(doctorBarnReadService.countPigByBarnId(barnId));
    }

    /**
     * 根据id查询猪舍表
     * @param barnId 主键id
     * @return 猪舍表
     */
    @RequestMapping(value = "/id", method = RequestMethod.GET)
    public DoctorBarn findBarnById(@RequestParam("barnId") Long barnId) {
        return RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
    }

    /**
     * 根据farmId查询猪舍表, 根据pigIds过滤
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
     * 根据farmId和状态查询猪舍表
     * @param farmId 猪场id
     * @param pigType 猪舍类别
     * @param pigIds 猪id 逗号分隔
     * @see PigType
     * @return 猪舍表列表
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
            return MoreObjects.firstNonNull(barns, Lists.newArrayList());
        }

        List<Integer> barnTypes = Splitters.splitToLong(pigIds, Splitters.COMMA).stream()
                .map(pigId -> RespHelper.or500(doctorPigReadService.findBarnByPigId(pigId)).getPigType())
                .collect(Collectors.toList());
        return barns.stream().filter(barn -> barnTypes.contains(barn.getPigType())).collect(Collectors.toList());
    }

    /**
     * 根据farmId和状态查询猪舍表
     * @param farmId 猪场id
     * @param pigTypes 猪舍类别 逗号分隔
     * @see PigType
     * @return 猪舍表列表
     */
    @RequestMapping(value = "/pigTypes", method = RequestMethod.GET)
    public List<DoctorBarn> findBarnsByfarmIdAndType(@RequestParam("farmId") Long farmId,
                                                     @RequestParam(value = "pigTypes", required = false) String pigTypes) {
        List<Integer> types = Lists.newArrayList();
        if (notEmpty(pigTypes)) {
            types = Splitters.splitToInteger(pigTypes, Splitters.COMMA);
        }
        return RespHelper.or500(doctorBarnReadService.findBarnsByFarmIdAndPigTypes(farmId, types));
    }

    /**
     * 创建或更新DoctorBarn
     * @return 是否成功
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public Long createOrUpdateBarn(@RequestBody DoctorBarn barn) {
        checkNotNull(barn, "barn.not.null");

        //权限中心校验权限
        doctorFarmAuthCenter.checkFarmAuth(barn.getFarmId());

        Long barnId;

        DoctorFarm farm = RespHelper.or500(doctorFarmReadService.findFarmById(barn.getFarmId()));
        barn.setOrgId(farm.getOrgId());
        barn.setOrgName(farm.getOrgName());
        barn.setFarmName(farm.getName());
        barn.setStaffId(UserUtil.getUserId());
        barn.setStaffName(UserUtil.getCurrentUser().getName());

        if (barn.getId() == null) {
            barn.setStatus(DoctorBarn.Status.NOUSE.getValue());     //初始猪舍状态: 未用
            barn.setCanOpenGroup(DoctorBarn.CanOpenGroup.YES.getValue());  //初始是否可建群: 可建群
            barnId = RespHelper.or500(doctorBarnWriteService.createBarn(barn));
        } else {
            RespHelper.or500(doctorBarnWriteService.updateBarn(barn));
            barnId = barn.getId();
        }
        return barnId;
    }

    /**
     * 更新猪舍状态
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
            barnDetail.setPigPaging(RespHelper.or500(doctorPigReadService.pagingDoctorInfoDtoByPigTrack(DoctorPigTrack.builder()
                    .status(status)
                    .currentBarnId(barnId)
                    .pigType(DoctorPig.PIG_TYPE.SOW.getKey())
                    .farmId(barn.getFarmId()).build(), pageNo, size)));
            barnDetail.setStatuses(RespHelper.or500(doctorPigReadService.findPigStatusByBarnId(barnId)));
            return barnDetail;
        }

        //猪群舍(实际情况: 分娩母猪舍里也有猪群)
        if (PigType.isGroup(barn.getPigType())) {
            barnDetail.setType(PigSearchType.GROUP.getValue());
            barnDetail.setStatuses(Sets.newHashSet(barn.getPigType())); //一类猪舍只能放一类猪群

            DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
            searchDto.setFarmId(barn.getFarmId());
            searchDto.setCurrentBarnId(barnId);
            searchDto.setPigType(status);   //这里的状态就是猪群的猪类
            barnDetail.setGroupPaging(RespHelper.or500(doctorGroupReadService.pagingGroup(searchDto, pageNo, size)));
            return barnDetail;
        }
        return barnDetail;
    }
}