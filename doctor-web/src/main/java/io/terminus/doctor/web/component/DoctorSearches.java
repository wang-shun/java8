package io.terminus.doctor.web.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.enums.SearchType;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigSearchType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBarnDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorSuggestPig;
import io.terminus.doctor.event.dto.GroupPigPaging;
import io.terminus.doctor.event.dto.search.SearchedBarn;
import io.terminus.doctor.event.dto.search.SearchedBarnDto;
import io.terminus.doctor.event.dto.search.SearchedGroup;
import io.terminus.doctor.event.dto.search.SearchedPig;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.event.dto.msg.DoctorMessageUserDto;
import io.terminus.doctor.event.service.DoctorMessageUserReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.notEmpty;

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

    private final DoctorBarnReadService doctorBarnReadService;

    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    private final DoctorGroupReadService doctorGroupReadService;

    private final DoctorMessageUserReadService doctorMessageUserReadService;

    private final DoctorPigReadService doctorPigReadService;

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
                          DoctorPigReadService doctorPigReadService) {
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorMessageUserReadService = doctorMessageUserReadService;
        this.doctorPigReadService = doctorPigReadService;
    }

    @RequestMapping(value = "/suggest/event", method = RequestMethod.GET)
    public List<DoctorSuggestPig> suggestPigsByEvent(@RequestParam Long farmId, @RequestParam Integer eventType, @RequestParam(required = false) String pigCode, @RequestParam Integer sex) {
        return RespHelper.or500(doctorPigEventReadService.suggestPigsByEvent(eventType, farmId, pigCode, sex));
    }
    /**
     * 母猪搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     * @return
     */
    @RequestMapping(value = "/sowpigs", method = RequestMethod.GET)
    public GroupPigPaging<SearchedPig> searchSowPigs(@RequestParam(required = false) Integer pageNo,
                                             @RequestParam(required = false) Integer pageSize,
                                             @RequestParam Map<String, String> params) {
        Paging<SearchedPig> paging = pagePigs(pageNo, pageSize, params, DoctorPig.PigSex.SOW);
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
    private Paging<SearchedPig> pagePigs(Integer pageNo, Integer pageSize, Map<String, String> params, DoctorPig.PigSex pigType){
        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }
        searchFromMessage(params);
        params.put("pigCode", params.get("q"));
        params.put("pigType", pigType.getKey().toString());
        Map<String, Object> objectMap = transMapType(params);

        BaseUser user = UserUtil.getCurrentUser();
        if(Objects.equals(user.getType(), UserType.FARM_SUB.value())){
            objectMap.put("barnIds", RespHelper.or500(doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId())).getBarnIdsList());
        }

        if(objectMap.containsKey("statuses")){
            objectMap.put("statuses", Splitters.splitToInteger(objectMap.get("statuses").toString(), Splitters.UNDERSCORE));
        }
        Paging<SearchedPig> paging = RespHelper.or500(doctorPigReadService.pagingPig(objectMap, pageNo, pageSize));
        paging.getData().forEach(searchedPig -> {
            if(searchedPig.getPigType() != null){
                DoctorPig.PigSex pigSex = DoctorPig.PigSex.from(searchedPig.getPigType());
                if(pigSex != null){
                    searchedPig.setPigTypeName(pigSex.getDesc());
                }
            }
        });
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
        Long sowCount = getSowCountWhenFarrow(searchDto);
        return new GroupPigPaging<>(paging, groupCount, sowCount);
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
        DoctorGroupSearchDto searchDto = JsonMapper.nonEmptyMapper().fromJson(JsonMapperUtil.nonEmptyMapper().toJson(params), DoctorGroupSearchDto.class);
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
    private Long getSowCountWhenFarrow(DoctorGroupSearchDto searchDto) {
        Long sowCount = 0L;
        if (notEmpty(searchDto.getBarnIdList())) {
            for (Long barnId : searchDto.getBarnIdList()) {
                DoctorBarn barn = RespHelper.or500(doctorBarnReadService.findBarnById(barnId));
                if (barn != null && Objects.equals(barn.getPigType(), PigType.DELIVER_SOW.getValue())) {
                    List<DoctorPigTrack> pigTracks = RespHelper.or(doctorPigReadService
                            .findActivePigTrackByCurrentBarnId(barnId), Collections.emptyList());
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

        Paging<DoctorBarn> barns = RespHelper.or500(doctorBarnReadService.pagingBarn(barnDto, pageNo, pageSize));
        return new Paging<>(barns.getTotal(), getSearchedBarn(barns.getData()));
    }

    //拼接下猪舍需要的字段
    private List<SearchedBarn> getSearchedBarn(List<DoctorBarn> barns) {
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
}
