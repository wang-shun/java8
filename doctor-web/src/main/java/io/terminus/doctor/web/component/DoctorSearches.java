package io.terminus.doctor.web.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.Arguments;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.enums.SearchType;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigSearchType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dto.*;
import io.terminus.doctor.event.dto.msg.DoctorMessageUserDto;
import io.terminus.doctor.event.dto.search.*;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.KongHuaiPregCheckResult;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.*;
import io.terminus.doctor.event.service.*;
import io.terminus.doctor.user.model.DoctorFarmInformation;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.web.core.export.Exporter;
import io.terminus.doctor.web.front.event.dto.DoctorSowManagerDto;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.notEmpty;
import static java.util.stream.Collectors.toList;

/**
 * Desc: 猪场软件主搜
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/25
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/search")
public class DoctorSearches {

    @Autowired
    private Exporter exporter;

    private final DoctorBarnReadService doctorBarnReadService;

    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    private final DoctorGroupReadService doctorGroupReadService;

    private final DoctorMessageUserReadService doctorMessageUserReadService;

    private final DoctorPigReadService doctorPigReadService;

    private final DoctorFarmReadService doctorFarmReadService;

    @RpcConsumer
    private DoctorPigEventReadService doctorPigEventReadService;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    //保育，育肥，后备
    private static final List<PigType> JUST_GROUPS = Lists.newArrayList(PigType.NURSERY_PIGLET, PigType.FATTEN_PIG, PigType.RESERVE);

    //配种，妊娠，种公猪
    private static final List<PigType> JUST_PIGS = Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW, PigType.BOAR);

    @Autowired
    public DoctorSearches(DoctorBarnReadService doctorBarnReadService,
                          DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                          DoctorGroupReadService doctorGroupReadService,
                          DoctorMessageUserReadService doctorMessageUserReadService,
                          DoctorPigReadService doctorPigReadService,
                          DoctorFarmReadService doctorFarmReadService) {
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorMessageUserReadService = doctorMessageUserReadService;
        this.doctorPigReadService = doctorPigReadService;
        this.doctorFarmReadService = doctorFarmReadService;
    }

    /**
     * 获取猪场存栏状况
     * @param farmId 猪场id
     * @return 猪场存栏
     */
    @RequestMapping(value = "/getFarmLiveStock", method = RequestMethod.GET)
    public DoctorLiveStockDto getFarmLiveStock(@RequestParam Long farmId) {
        DoctorPigCountDto pigCountDto = RespHelper.or500(doctorPigReadService.getPigCount(farmId));
        DoctorGroupCountDto groupCountDto = RespHelper.or500(doctorGroupReadService.findGroupCount(farmId));
        return new DoctorLiveStockDto(pigCountDto, groupCountDto);
    }

    @RequestMapping(value = "/suggest/event", method = RequestMethod.GET)
    public List<DoctorSuggestPig> suggestPigsByEvent(@RequestParam Long farmId,
                                                     @RequestParam Integer eventType,
                                                     @RequestParam(required = false) String pigCode,
                                                     @RequestParam Integer sex) {
        return RespHelper.or500(doctorPigEventReadService.suggestPigsByEvent(eventType, farmId, pigCode, sex));
    }
    /**
     * 母猪搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *
     * @return
     *
     * 增加按事件类型,事件日期筛选 by lbw
     */
    @RequestMapping(value = "/sowpigs", method = RequestMethod.GET)
    public GroupPigPaging<SearchedPig> searchSowPigs(@RequestParam(required = false) Integer pageNo,
                                             @RequestParam(required = false) Integer pageSize,
                                             @RequestParam Map<String, String> params) throws ParseException {
        Paging<SearchedPig> paging = pageSowPigs(pageNo, pageSize, params);
        Long groupCount = getGroupCountWhenFarrow(params);
        return new GroupPigPaging<>(paging, groupCount, paging.getTotal());
    }

    //获取猪群里猪的数量
    private Long getGroupCountWhenFarrow(Map<String, String> params) {
        //如果没传barnId，直接返回0
        if (!params.containsKey("barnId") || isEmpty(params.get("barnId"))) {
            return 0L;
        }

        DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
        searchDto.setFarmId(Long.valueOf(params.get("farmId")));
        searchDto.setCurrentBarnId(Long.valueOf(params.get("barnId")));

        BaseUser user = UserUtil.getCurrentUser();
        if(Objects.equals(user.getType(), UserType.FARM_SUB.value())){
            searchDto.setBarnIdList(RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId())).getBarnIdsList());
        }
        return RespHelper.or(doctorGroupReadService.getGroupCount(searchDto), 0L);
    }

    //猪分页
    //2017/3/14 增加事件筛选 type, event_at
    private Paging<SearchedPig> pagePigs(Integer pageNo, Integer pageSize, Map<String, String> params, DoctorPig.PigSex pigType){
        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }

        searchFromMessage(params);

        params.put("pigCode", params.get("q"));
        params.put("pigType", pigType.getKey().toString());
        Map<String, Object> objectMap = transMapType(params);
        //先根据事件去查
        boolean searchEvent = !Strings.isNullOrEmpty(params.get("types"))
                || !Strings.isNullOrEmpty(params.get("beginDate"))
                || !Strings.isNullOrEmpty(params.get("endDate"));
        if (searchEvent) {
            Map<String, Object> eventCriteria = Maps.newHashMap();
            if (!Strings.isNullOrEmpty(params.get("types"))){
                eventCriteria.put("types", Splitters.splitToInteger(params.get("types"), Splitters.COMMA));
            }
            eventCriteria.put("beginDate", params.get("beginDate"));
            eventCriteria.put("endDate", params.get("endDate"));
            eventCriteria.put("farmId", params.get("farmId"));
            eventCriteria = Params.filterNullOrEmpty(eventCriteria);
            List<Long> pigIds = RespHelper.or(doctorPigEventReadService.findPigIdsBy(eventCriteria), null);
            if (CollectionUtils.isEmpty(pigIds)) {
                return new Paging<SearchedPig>();
            }
            objectMap.put("pigIds", pigIds);
        }

        BaseUser user = UserUtil.getCurrentUser();
        if(Objects.equals(user.getType(), UserType.FARM_SUB.value())){
            objectMap.put("barnIds", RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId())).getBarnIdsList());
        }

        if(objectMap.containsKey("statuses")){
            objectMap.put("statuses", Splitters.splitToInteger(objectMap.get("statuses").toString(), Splitters.UNDERSCORE));
        }

        Paging<SearchedPig> paging;
        if(objectMap.containsKey("statuses")
                && ((List)objectMap.get("statuses")).contains(PigStatus.CHG_FARM.getKey())){
            log.error("pagePigs:"+1+":"+objectMap.toString());
            paging = RespHelper.or500(doctorPigReadService.pagingChgFarmPig(objectMap, pageNo, pageSize));
        } else {
            log.error("pagePigs:"+2+":"+objectMap.toString());
            paging = RespHelper.or500(doctorPigReadService.pagingPig(objectMap, pageNo, pageSize));
        }
        paging.getData().forEach(searchedPig -> {
            if(searchedPig.getPigType() != null){
                DoctorPig.PigSex pigSex = DoctorPig.PigSex.from(searchedPig.getPigType());
                if(pigSex != null){
                    searchedPig.setPigTypeName(pigSex.getDesc());
                }
            }
            Integer status = searchedPig.getStatus();
            Date eventAt;
            if (Objects.equals(status, PigStatus.CHG_FARM.getKey())) {
                try {
                    DoctorChgFarmInfo doctorChgFarmInfo = RespHelper.or500(doctorPigReadService.findByFarmIdAndPigId(searchedPig.getFarmId(), searchedPig.getId()));
                    DoctorPigEvent chgFarm = RespHelper.or500(doctorPigEventReadService.findById(doctorChgFarmInfo.getEventId()));
                    eventAt = chgFarm.getEventAt();
                }catch(Exception e){
                    log.error(e.getMessage());
                    eventAt = new Date();
                }
            } else {
                KongHuaiPregCheckResult result = KongHuaiPregCheckResult.from(searchedPig.getStatus());
                if (result != null) {
                    status = PigStatus.KongHuai.getKey();
                }

                if (Objects.equals(status, PigStatus.Pregnancy.getKey())) {
                    status = PigStatus.Mate.getKey();
                }
                eventAt = RespHelper.or500(doctorPigEventReadService.findEventAtLeadToStatus(searchedPig.getId()
                        , status));
            }
            Integer statusDay = DateUtil.getDeltaDays(eventAt, new Date());
            searchedPig.setStatusDay(statusDay);
        });
        log.error("pagePigs:size"+paging.getData().size());
        return paging;
    }

    /**
     * 搜索母猪的suggest
     *
     * @param size   数量
     * @param params 查询参数
     * @return
     */
    @RequestMapping(value = "/sowpigs/suggest", method = RequestMethod.GET)
    public List<SearchedPig> searchSowsSuggest(@RequestParam(required = false) Integer size,
                                               @RequestParam Map<String, String> params) {
        return pagePigs(1, size, params, DoctorPig.PigSex.SOW).getData();
    }


    /**
     * 公猪搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     * @return
     */
    @RequestMapping(value = "/boarpigs", method = RequestMethod.GET)
    public Paging<SearchedPig> searchBoarPigs(@RequestParam(required = false) Integer pageNo,
                                              @RequestParam(required = false) Integer pageSize,
                                              @RequestParam Map<String, String> params) {
        return pagePigs(pageNo, pageSize, params, DoctorPig.PigSex.BOAR);
    }

    /**
     * 所有公猪搜索方法(最多2000头公猪)
     *
     * @param params 搜索参数
     *               搜索参数可以参照:
     * @return
     */
    @RequestMapping(value = "/boarpigs/all", method = RequestMethod.GET)
    public List<SearchedPig> searchAllBoarPigs(@RequestParam Map<String, String> params) {
        return pagePigs(1, 2000, params, DoctorPig.PigSex.BOAR).getData();
    }

    /**
     * 所有猪群搜索方法
     */
    @RequestMapping(value = "/groups/all", method = RequestMethod.GET)
    public List<SearchedGroup> searchGroupsAll(@RequestParam Map<String, String> params) {
        DoctorGroupSearchDto searchDto = getGroupSearchDto(params);
        if (searchDto == null) {
            return Collections.emptyList();
        }
        return pagingGroup(1, Integer.MAX_VALUE, searchDto).getData();
    }


    /**
     * 猪群搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     * @return
     * @see `DefaultGroupQueryBuilder#buildTerm`
     */
    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public GroupPigPaging<SearchedGroup> searchGroups(@RequestParam(required = false) Integer pageNo,
                                                      @RequestParam(required = false) Integer pageSize,
                                                      @RequestParam Map<String, String> params) {

        DoctorGroupSearchDto searchDto = getGroupSearchDto(params);
        if (searchDto == null) {
            return new GroupPigPaging<>(0L, Collections.emptyList());
        }

        Paging<SearchedGroup> paging = pagingGroup(pageNo, pageSize, searchDto);
        Long groupCount = RespHelper.orServEx(doctorGroupReadService.getGroupCount(searchDto));
        Long sowCount = getSowCountWhenFarrow(searchDto,Long.valueOf(params.get("farmId")));
        return new GroupPigPaging<>(paging, groupCount, sowCount);
    }

    @RequestMapping(value = "/barn-info", method = RequestMethod.GET)
    public SearchedGroup getBarnInfo(@RequestParam Long barnId){
        SearchedGroup searchDto = new SearchedGroup();
        DoctorBarn doctorBarn = RespHelper.orServEx(doctorBarnReadService.findBarnById(barnId));
        searchDto.setFarmId(doctorBarn.getFarmId());
        searchDto.setFarmName(doctorBarn.getFarmName());
        searchDto.setPigType(doctorBarn.getPigType());
        searchDto.setPigTypeName(PigType.from(doctorBarn.getPigType()).getDesc());
        searchDto.setCurrentBarnId(doctorBarn.getId());
        searchDto.setCurrentBarnName(doctorBarn.getName());
        List<DoctorGroup> groups = RespHelper.orServEx(doctorGroupReadService.findGroupByCurrentBarnId(barnId));
        groups.forEach(doctorGroup -> {
            DoctorGroupDetail groupDetail = RespHelper.orServEx(doctorGroupReadService.findGroupDetailByGroupId(doctorGroup.getId()));
            if(!Arguments.isNull(groupDetail) && !Arguments.isNull(groupDetail.getGroupTrack())){
                searchDto.setQuantity(MoreObjects.firstNonNull(searchDto.getQuantity(), 0) + MoreObjects.firstNonNull(groupDetail.getGroupTrack().getQuantity(), 0));
            }
        });
        return searchDto;
    }

    //猪群分页
    private Paging<SearchedGroup> pagingGroup(Integer pageNo, Integer pageSize, DoctorGroupSearchDto searchDto) {
        if (searchDto == null || searchDto.getFarmId() == null) {
            return new GroupPigPaging<>(0L, Collections.emptyList());
        }
        Paging<DoctorGroupDetail> groupDetailPaging = RespHelper.or500(doctorGroupReadService.pagingGroup(searchDto, pageNo, pageSize));
        List<SearchedGroup> searchedGroups = groupDetailPaging.getData().stream()
                .map(gd -> {
                    SearchedGroup group = BeanMapper.map(gd.getGroup(), SearchedGroup.class);
                    PigType pigType = PigType.from(group.getPigType());
                    group.setPigTypeName(pigType == null ? "" : pigType.getDesc());
                    group.setSex(gd.getGroupTrack().getSex());
                    group.setQuantity(gd.getGroupTrack().getQuantity());
                    group.setAvgDayAge(gd.getGroupTrack().getAvgDayAge());
                    return group;
                })
                .collect(Collectors.toList());
        return new Paging<>(groupDetailPaging.getTotal(), searchedGroups);
    }

    //猪群查询条件转换
    private DoctorGroupSearchDto getGroupSearchDto(Map<String, String> params) {
        params = filterNullOrEmpty(params);
        searchFromMessage(params);

        replaceKey(params, "q", "groupCode");
        replaceKey(params, "pigTypes", "pigTypeCommas");

        List<Integer> pigTypes = null;
        if(params.get("pigTypes") != null){
            pigTypes = Splitters.splitToInteger(params.get("pigTypes"), Splitters.UNDERSCORE);
            params.remove("pigTypes");
        }
        DoctorGroupSearchDto searchDto = JsonMapper.nonEmptyMapper().fromJson(ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(params), DoctorGroupSearchDto.class);
        searchDto.setPigTypes(pigTypes);

        BaseUser user = UserUtil.getCurrentUser();
        List<Long> permission = RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId())).getBarnIdsList();
        if(StringUtils.isBlank(params.get("barnId"))){
            if(Objects.equals(user.getType(), UserType.FARM_SUB.value())){
                searchDto.setBarnIdList(permission);
            }
        }else{
            Long barnId = Long.valueOf(params.get("barnId"));
            if(Objects.equals(user.getType(), UserType.FARM_SUB.value()) && !permission.contains(barnId)){
                return null;
            }else{
                searchDto.setBarnIdList(Lists.newArrayList(barnId));
            }
        }
        return searchDto;
    }

    private static void replaceKey(Map<String, String> params, String oldKey, String newKey) {
        if (params.containsKey(oldKey)) {
            params.put(newKey, params.get(oldKey));
        }
    }

    //如果是产房，获取一下产房里的母猪数
    private Long getSowCountWhenFarrow(DoctorGroupSearchDto searchDto,Long farmId) {
        Long sowCount = 0L;
        if (notEmpty(searchDto.getBarnIdList())) {
            for (Long barnId : searchDto.getBarnIdList()) {
                DoctorBarn barn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
                if (barn != null && Objects.equals(barn.getPigType(), PigType.DELIVER_SOW.getValue())) {
                    List<DoctorPigTrack> pigTracks = RespHelper.or(doctorPigReadService
                            .findActivePigTrackByCurrentBarnIds(barnId,farmId), Collections.emptyList());
                    sowCount += pigTracks.size();
                }
            }
            return sowCount;
        }

        //如果查产房类型，直接返回所有产房里的母猪数
        if (notEmpty(searchDto.getPigTypes()) && searchDto.getPigTypes().contains(PigType.DELIVER_SOW.getValue())) {
            return RespHelper.or(doctorPigReadService.getPigCountByBarnPigTypes(searchDto.getFarmId(),
                            Lists.newArrayList(PigType.DELIVER_SOW.getValue())), 0L);
        }
        return sowCount;
    }

    /**
     * 猪舍搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     * @return
     */
    @RequestMapping(value = "/barns", method = RequestMethod.GET)
    public SearchedBarnDto searchBarn(@RequestParam(required = false) Integer pageNo,
                                      @RequestParam(required = false) Integer pageSize,
                                      @RequestParam Map<String, String> params) {
        return SearchedBarnDto.builder().barns(this.searchBarnsPC(pageNo, pageSize, params)).build();
    }


    /**
     * 猪舍搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     * @return
     */
    @RequestMapping(value = "/wsn/barns", method = RequestMethod.GET)
    public SearchedBarnDto searchWsnBarn(@RequestParam(required = false) Integer pageNo,
                                         @RequestParam(required = false) Integer pageSize,
                                         @RequestParam Map<String, String> params) {
        DoctorBarnDto barnDto = new DoctorBarnDto();
        if (Params.containsNotEmpty(params, "barnIds")){
            List<Long> barnIds = Splitters.splitToLong(params.get("barnIds").toString(), Splitters.COMMA);
            barnDto.setBarnIds(barnIds);
        }

        if (Params.containsNotEmpty(params, "q")) {
            barnDto.setName(params.get("q"));
        }
        if (Params.containsNotEmpty(params, "farmId")) {
            barnDto.setFarmId(Long.valueOf(params.get("farmId")));
        }
        if (Params.containsNotEmpty(params, "pigType")) {
            barnDto.setPigType(Integer.valueOf(params.get("pigType")));
        }
        if (Params.containsNotEmpty(params, "pigTypes")) {
            barnDto.setPigTypes(Splitters.splitToInteger(params.get("pigTypes"), Splitters.COMMA));
        }
        if (Params.containsNotEmpty(params, "status")) {
            barnDto.setStatus(Integer.valueOf(params.get("status")));
        }
        if (barnDto.getFarmId() == null) {
            return new SearchedBarnDto();
        }

        Paging<DoctorBarn> barns = RespHelper.or500(doctorBarnReadService.pagingBarn(barnDto, pageNo, pageSize));
        Paging<SearchedBarn> result = new Paging<>(barns.getTotal(), getSearchedBarn(barns.getData()));
        return SearchedBarnDto.builder().barns(result).build();
    }

    /**
     * PC端猪舍搜索方法(每个猪舍里要有根据状态聚合的数据)
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     * @return 分页结果
     * @see `DefaultBarnQueryBuilder#buildTerm`
     */
    @RequestMapping(value = "/barns/pc", method = RequestMethod.GET)
    public Paging<SearchedBarn> searchBarnsPC(@RequestParam(required = false) Integer pageNo,
                                              @RequestParam(required = false) Integer pageSize,
                                              @RequestParam Map<String, String> params) {
        DoctorBarnDto barnDto = getBarnSearchMap(params);
        if (barnDto.getFarmId() == null) {
            return new Paging<>(0L, Collections.emptyList());
        }
        if (barnDto.getStatus() == null){
            barnDto.setStatus(1);
        }

        Paging<DoctorBarn> barns = RespHelper.or500(doctorBarnReadService.pagingBarn(barnDto, pageNo, pageSize));
        return new Paging<>(barns.getTotal(), getSearchedBarn(barns.getData()));
    }

    //拼接下猪舍需要的字段
    public List<SearchedBarn> getSearchedBarn(List<DoctorBarn> barns) {
        return BeanMapper.mapList(barns, SearchedBarn.class).stream()
                .map(barn -> {
                    PigType pigType = PigType.from(barn.getPigType());
                    barn.setPigTypeName(pigType == null ? "" : pigType.getDesc());

                    int pigCount = 0;
                    int groupCount = 0;
                    List<SearchedBarn.BarnStatus> barnStatus = Lists.newArrayList();

                    //猪舍里每种猪的聚合
                    if (Objects.equals(pigType, PigType.DELIVER_SOW)) {
                        List<DoctorPigTrack> pigTracks = RespHelper.or500(doctorPigReadService.findActivePigTrackByCurrentBarnId(barn.getId()));
                        pigCount = pigTracks.size();
                        groupCount = getGroupCount(barn);
                        barnStatus = addPigBarnStatus(barnStatus, pigTracks);
                        barn.setPigWeanCount(getGroupWeanCount(barn));
                        barn.setType(PigSearchType.SOW_GROUP.getValue());
                    }
                    else if (JUST_GROUPS.contains(pigType)) {
                        groupCount = getGroupCount(barn);
                        barn.setType(PigSearchType.GROUP.getValue());
                    }
                    else if (JUST_PIGS.contains(pigType)) {
                        List<DoctorPigTrack> pigTracks = RespHelper.or500(doctorPigReadService.findActivePigTrackByCurrentBarnId(barn.getId()));
                        pigCount = pigTracks.size();
                        barnStatus = addPigBarnStatus(barnStatus, pigTracks);
                        barn.setType(Objects.equals(pigType, PigType.BOAR) ? PigSearchType.BOAR.getValue() : PigSearchType.SOW.getValue());
                    }

                    barn.setPigCount(pigCount);
                    barn.setPigGroupCount(groupCount);
                    barn.setStorage(barn.getPigCount() + barn.getPigGroupCount());
                    barn.setBarnStatuses(barnStatus);
                    return barn;
                })
                .collect(Collectors.toList());
    }

    //获取猪群数量
    private int getGroupCount(SearchedBarn barn) {
        DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
        searchDto.setFarmId(barn.getFarmId());
        searchDto.setCurrentBarnId(barn.getId());
        searchDto.setStatus(DoctorGroup.Status.CREATED.getValue());
        return RespHelper.or500(doctorGroupReadService.getGroupCount(searchDto)).intValue();
    }

    //获取猪群断奶仔猪数
    private int getGroupWeanCount(SearchedBarn barn) {
        DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
        searchDto.setFarmId(barn.getFarmId());
        searchDto.setCurrentBarnId(barn.getId());
        searchDto.setStatus(DoctorGroup.Status.CREATED.getValue());
        return RespHelper.or500(doctorGroupReadService.getWeanCount(searchDto)).intValue();
    }

    //获取猪舍中猪的状态聚合
    private List<SearchedBarn.BarnStatus> addPigBarnStatus(List<SearchedBarn.BarnStatus> barnStatus, List<DoctorPigTrack> pigTracks) {
        for (Map.Entry<Integer, List<DoctorPigTrack>> m : pigTracks.stream().collect(Collectors.groupingBy(DoctorPigTrack::getStatus)).entrySet()) {
            //公猪进场的只显示总数就可以，所以不加进去了
            if (!Objects.equals(PigStatus.BOAR_ENTRY.getKey(), m.getKey()) && m.getValue().size() > 0) {
                barnStatus.add(SearchedBarn.createPigStatus(PigStatus.from(m.getKey()), m.getValue().size()));
            }
        }
        return barnStatus;
    }

    //转换猪舍搜索条件
    private DoctorBarnDto getBarnSearchMap(Map<String, String> params) {
        DoctorBarnDto barnDto = new DoctorBarnDto();

        //主账号不用校验，直接拥有全部猪舍权限
        BaseUser user = UserUtil.getCurrentUser();
        if(Objects.equals(user.getType(), UserType.FARM_SUB.value())){
            barnDto.setBarnIds(RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId())).getBarnIdsList());
        }

        if (Params.containsNotEmpty(params, "q")) {
            barnDto.setName(params.get("q"));
        }
        if (Params.containsNotEmpty(params, "farmId")) {
            barnDto.setFarmId(Long.valueOf(params.get("farmId")));
        }
        if (Params.containsNotEmpty(params, "pigType")) {
            barnDto.setPigType(Integer.valueOf(params.get("pigType")));
        }
        if (Params.containsNotEmpty(params, "pigTypes")) {
            barnDto.setPigTypes(Splitters.splitToInteger(params.get("pigTypes"), Splitters.COMMA));
        }
        if (Params.containsNotEmpty(params, "status")) {
            barnDto.setStatus(Integer.valueOf(params.get("status")));
        }
        return barnDto;
    }

    /**
     * 获取所有的猪舍信息
     *
     * @param params 搜索参数
     *               搜索参数可以参照:
     * @return
     * @see `DefaultBarnQueryBuilder#buildTerm`
     */
    @RequestMapping(value = "/barns/all", method = RequestMethod.GET)
    public List<SearchedBarn> searchAllBarns(@RequestParam Map<String, String> params) {
        return this.searchBarnsPC(1, Integer.MAX_VALUE, params).getData();
    }

    private boolean farmIdNotExist(Map<String, String> params) {
        return isEmpty(params.get("farmId"));
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Long> getAllPigIds(@RequestParam(required = false) String ids,
                                   @RequestParam Integer searchType,
                                   @RequestParam Map<String, String> params) {

        try {
            // 母猪状态由前台传
            if (searchType.equals(SearchType.GROUP.getValue())) {
                params.put("status", String.valueOf(DoctorGroup.Status.CREATED.getValue()));
            } else if (searchType.equals(SearchType.BOAR.getValue())) {
                params.put("pigType", DoctorPig.PigSex.BOAR.getKey().toString());
                params.put("statuses", PigStatus.BOAR_ENTRY.toString());
            } else if (searchType.equals(SearchType.SOW.getValue())) {
                params.put("pigType", DoctorPig.PigSex.SOW.getKey().toString());
                params.put("isRemoval", String.valueOf(IsOrNot.NO.getValue())); //只查询未离场的猪
            }
            params.remove("ids");
            params.remove("searchType");

            List<Long> allPigOrGroupIds;
            Integer pageNo = 1;
            Integer pageSize = Integer.MAX_VALUE;
            
            if (searchType.equals(SearchType.GROUP.getValue())) {
                Paging<SearchedGroup> searchGroupPaging = this.searchGroups(pageNo, pageSize, params);
                allPigOrGroupIds = searchGroupPaging.getData().stream().map(SearchedGroup::getId).collect(Collectors.toList());
            } else {
                DoctorPig.PigSex pigType = searchType.equals(SearchType.SOW.getValue()) ? DoctorPig.PigSex.SOW : DoctorPig.PigSex.BOAR;

                Paging<SearchedPig> searchPigPaging = pagePigs(pageNo, pageSize, params, pigType);
                allPigOrGroupIds = searchPigPaging.getData().stream().map(SearchedPig::getId).collect(Collectors.toList());
            }

            if (ids != null) {
                List<Long> excludePigIds = OBJECT_MAPPER.readValue(ids, JacksonType.LIST_OF_LONG);
                return allPigOrGroupIds.stream().filter(id -> !excludePigIds.contains(id)).collect(Collectors.toList());
            } else {
                return allPigOrGroupIds;
            }
        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
    }

    public void searchFromMessage(Map<String, String> params) {
        if (Objects.equals(params.get("searchFrom"), "MESSAGE")) {
            DoctorMessageUserDto doctorMessageUserDto = new DoctorMessageUserDto();
            doctorMessageUserDto.setTemplateId(Long.parseLong(params.get("templateId")));
            doctorMessageUserDto.setFarmId(Long.parseLong(params.get("farmId")));
            doctorMessageUserDto.setUserId(UserUtil.getCurrentUser().getId());
            List<Long> idList = RespHelper.or500(doctorMessageUserReadService.findBusinessListByCriteria(doctorMessageUserDto));
            String ids = idList.toString().trim().substring(1, idList.toString().toCharArray().length - 1);
            params.put("ids", ids);
        }
    }

    private Map<String, String> filterNullOrEmpty(Map<String, String> criteria) {
        return Maps.filterEntries(criteria, entry -> {
            String v = entry.getValue();
            return !Strings.isNullOrEmpty(v);
        });
    }

    private Map<String, Object> transMapType(Map<String, String> map){
        if(map == null){
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        for(Map.Entry<String, String> entry : map.entrySet()){
            String value = entry.getValue();
            if(StringUtils.isNotBlank(value)){
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    @RequestMapping(value = "/sowManagerExport", method = RequestMethod.GET)
    public void sowManagerExport(@RequestParam Map<String, String> eventCriteria, HttpServletRequest request, HttpServletResponse response) {
        //导出母猪管理
        exporter.export("sow-manager-export", eventCriteria, 1, 500, this::pagingSowManagerExport, request, response);
    }

    public Paging<DoctorSowManagerDto> pagingSowManagerExport(Map<String, String> pigEventCriteria) {
        Integer pageNo = Integer.valueOf(pigEventCriteria.get("pageNo"));
        Integer pageSize = Integer.valueOf(pigEventCriteria.get("size"));
        Paging<SearchedPig> paging = pagePigs(pageNo, pageSize, pigEventCriteria, DoctorPig.PigSex.SOW);
        List<DoctorSowManagerDto> list = paging.getData().stream().map(doctorPigEventDetail -> {
            DoctorSowManagerDto dto = new DoctorSowManagerDto();
            dto.setId(doctorPigEventDetail.getId());
            dto.setPigCode(doctorPigEventDetail.getPigCode());
            dto.setBreedName(doctorPigEventDetail.getBreedName());
            dto.setCurrentParity(doctorPigEventDetail.getCurrentParity());
            dto.setPigWeight(doctorPigEventDetail.getPigWeight());
            dto.setRfid(doctorPigEventDetail.getRfid());
            dto.setStatus(doctorPigEventDetail.getStatus());
            dto.setStatusDay(doctorPigEventDetail.getStatusDay());
            dto.setCurrentBarnId(doctorPigEventDetail.getCurrentBarnId());
            dto.setCurrentBarnName(doctorPigEventDetail.getCurrentBarnName());
            dto.setStatusName(doctorPigEventDetail.getStatusName());
            return dto;
        }).collect(toList());
        return new Paging<>(paging.getTotal(), list);
    }


    // -------------- 新增代码-----------------------
    @RequestMapping(value = "/notTransitionsSow", method = RequestMethod.GET)
    public List<Long> notTransitionsSow(@RequestParam Long farmId,@RequestParam Long barnId,
                                        @RequestParam String status,@RequestParam String pigCode,
                                        @RequestParam String rfid,@RequestParam Integer isRemoval){
        Map<String,Object> valueMap = new HashMap<>();
        valueMap.put("statuses", Splitters.splitToInteger(status, Splitters.UNDERSCORE));
        if("2".equals(status)){ isRemoval = 1; }
        return RespHelper.or500(doctorPigReadService.findNotTransitionsSow(farmId,barnId,valueMap,pigCode,rfid,isRemoval));
    }

    @RequestMapping(value = "/haveTransitionsSow", method = RequestMethod.GET)
    public List<Long> haveTransitionsSow(@RequestParam Long farmId,@RequestParam Long barnId,
                                         @RequestParam String pigCode,@RequestParam String rfid){
        return RespHelper.or500(doctorPigReadService.findHaveTransitionsSow(farmId,barnId,pigCode,rfid));
    }

    @RequestMapping(value = "/eventTransitionsSow", method = RequestMethod.GET)
    public List<Long> eventTransitionsSow(@RequestParam Long farmId,@RequestParam Long barnId,
                                          @RequestParam String status,@RequestParam String pigCode,
                                          @RequestParam String rfid,@RequestParam String types,
                                          @RequestParam Integer isRemoval,@RequestParam String beginDate,
                                          @RequestParam String endDate) throws ParseException {
        Map<String,Object> valueMap = new HashMap<>();
        if(status!=null && !"".equals(status)) {
            valueMap.put("statuses", Splitters.splitToInteger(status, Splitters.UNDERSCORE));
        }
        if("2".equals(status)){ isRemoval = 1; }
        List<Long> notList = RespHelper.or500(doctorPigReadService.findNotTransitionsSow(farmId,barnId,valueMap,pigCode,rfid,isRemoval));
        if( "13".equals(status) ) {
            List<Long> haveList = RespHelper.or500(doctorPigReadService.findHaveTransitionsSow(farmId, barnId, pigCode, rfid));
            notList.addAll(haveList);
        }
        Map<String, Object> eventCriteria = Maps.newHashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (!Strings.isNullOrEmpty(types)){
            eventCriteria.put("types", Splitters.splitToInteger(types, Splitters.COMMA));
        }
        if(!beginDate.isEmpty()){
            eventCriteria.put("beginDate",sdf.parse(beginDate));
        }
        if(!endDate.isEmpty()){
            eventCriteria.put("endDate", sdf.parse(endDate));
        }
        eventCriteria.put("farmId", farmId);
        eventCriteria.put("notList",notList);
        eventCriteria = Params.filterNullOrEmpty(eventCriteria);
        return RespHelper.or500(doctorPigEventReadService.findPigIdsByEvent(eventCriteria));
    }

    private Paging<SearchedPig> pageSowPigs (Integer pageNo, Integer pageSize,Map<String, String> params) throws ParseException {

        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }
        searchFromMessage(params);
        Map<String,Object> valueMap = new HashMap<>();

        Long farmId = null;
        Long barnId = null;
        Integer isRemoval = null;
        String pigCode = null;
        String rfid = null;
        if(!Strings.isNullOrEmpty(params.get("farmId"))){
            farmId = Long.parseLong(params.get("farmId"));
        }
        if(!Strings.isNullOrEmpty(params.get("barnId"))){
            barnId = Long.parseLong(params.get("barnId"));
        }
        if(!Strings.isNullOrEmpty(params.get("statuses"))){
            valueMap.put("statuses", Splitters.splitToInteger(params.get("statuses"), Splitters.UNDERSCORE));
        }
        if(!Strings.isNullOrEmpty(params.get("q"))){
            pigCode = params.get("q");
        }
        if(!Strings.isNullOrEmpty(params.get("isRemoval"))){
            isRemoval = Integer.parseInt(params.get("isRemoval"));
        }
        if(!Strings.isNullOrEmpty(params.get("rfid"))){
            rfid = params.get("rfid");
        }

        String leave = String.valueOf(valueMap.get("statuses"));
        if(leave!=null && "[2]".equals(leave)){
            isRemoval = 1;
        }

        List<Long> notList;
        if( "13".equals(params.get("statuses")) ){
            notList = RespHelper.or500(doctorPigReadService.findHaveTransitionsSow(farmId,barnId,pigCode,rfid));
        }else{
            notList = RespHelper.or500(doctorPigReadService.findNotTransitionsSow(farmId,barnId,valueMap,pigCode,rfid,isRemoval));
        }
        if(notList.size()<=0){
            return new Paging<>(0L, Collections.emptyList());
        }

        List<Long> eventList;
        boolean searchEvent = !Strings.isNullOrEmpty(params.get("types"))
                || !Strings.isNullOrEmpty(params.get("beginDate"))
                || !Strings.isNullOrEmpty(params.get("endDate"));
        if(searchEvent){
            Map<String, Object> eventCriteria = Maps.newHashMap();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (!Strings.isNullOrEmpty(params.get("types"))){
                eventCriteria.put("types", Splitters.splitToInteger(params.get("types"), Splitters.COMMA));
            }
            String beginDate = params.get("beginDate");
            String endDate = params.get("endDate");
            if(!beginDate.isEmpty()){
                eventCriteria.put("beginDate",sdf.parse(beginDate));
            }
            if(!endDate.isEmpty()){
                eventCriteria.put("endDate", sdf.parse(endDate));
            }
            eventCriteria.put("farmId", farmId);
            eventCriteria.put("notList",notList);
            eventCriteria = Params.filterNullOrEmpty(eventCriteria);
            eventList = RespHelper.or500(doctorPigEventReadService.findPigIdsByEvent(eventCriteria));
        }else{
            eventList = notList;
        }

        Map<String,Object> objectMap = new HashMap<>();
        Paging<SearchedPig> paging = null;

        if(eventList.size()>0){
            objectMap.put("pigIds",eventList);
            if("[2]".equals(leave)){
                paging = RespHelper.or500(doctorPigReadService.pagesSowPigById(objectMap, pageNo, pageSize));
            }else {
                paging = RespHelper.or500(doctorPigReadService.pagingPig(objectMap, pageNo, pageSize));
            }

            Long finalFarmId = farmId;
            paging.getData().forEach(searchedPig -> {
                Integer status = searchedPig.getStatus();
                Date eventAt;

                if ( "13".equals(params.get("statuses")) ) {
                    eventAt = RespHelper.or500(doctorPigEventReadService.findFarmSowEventAt(searchedPig.getId(), finalFarmId));

                }else{
                    KongHuaiPregCheckResult result = KongHuaiPregCheckResult.from(searchedPig.getStatus());
                    if (result != null) {
                        status = PigStatus.KongHuai.getKey();
                    }

                    if (Objects.equals(status, PigStatus.Pregnancy.getKey())) {
                        status = PigStatus.Mate.getKey();
                    }

                    if (status == 4 || status == 7){
                        eventAt = RespHelper.or500(doctorPigEventReadService.findMateEventToPigId(searchedPig.getId()));
                    } else {
                        eventAt = RespHelper.or500(doctorPigEventReadService.findEventAtLeadToStatus(searchedPig.getId(), status));
                    }
                }

                Integer statusDay = DateUtil.getDeltaDays(eventAt, new Date()) + 1;
                searchedPig.setStatusDay(statusDay);
                if ( "13".equals(params.get("statuses")) ) {
                    searchedPig.setStatus(13);
                    searchedPig.setStatusName("已转场");
                    searchedPig.setFarmId(Long.parseLong(params.get("farmId")));
                }
            });
        }

        log.error("pagePigs:size"+paging.getData().size());
        return paging;
    }

    @RequestMapping(value = "/findSubordinatePig",produces="application/json;charset=UTF-8", method = RequestMethod.GET)
    public String findSubordinatePig(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date){
        List<DoctorFarmInformation> farmInformation = doctorFarmReadService.findSubordinatePig(date);
        return ToJsonMapper.JSON_NON_EMPTY_MAPPER.toJson(farmInformation);
    }

}
