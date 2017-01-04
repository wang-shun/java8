package io.terminus.doctor.web.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Paging;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.basic.enums.SearchType;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.UserType;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBarnDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.search.barn.BarnSearchReadService;
import io.terminus.doctor.event.search.barn.SearchedBarn;
import io.terminus.doctor.event.search.barn.SearchedBarnDto;
import io.terminus.doctor.event.search.group.SearchedGroup;
import io.terminus.doctor.event.search.pig.SearchedPig;
import io.terminus.doctor.event.search.query.GroupPaging;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.msg.dto.DoctorMessageUserDto;
import io.terminus.doctor.msg.service.DoctorMessageUserReadService;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
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

    private final BarnSearchReadService barnSearchReadService;

    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    private final DoctorGroupReadService doctorGroupReadService;

    private final DoctorMessageUserReadService doctorMessageUserReadService;

    private final DoctorPigReadService doctorPigReadService;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    //保育，育肥，后备
    private static final List<PigType> JUST_GROUPS = Lists.newArrayList(PigType.NURSERY_PIGLET, PigType.FATTEN_PIG, PigType.RESERVE);

    //配种，妊娠，种公猪
    private static final List<PigType> JUST_PIGS = Lists.newArrayList(PigType.MATE_SOW, PigType.PREG_SOW, PigType.BOAR);

    @Autowired
    public DoctorSearches(DoctorBarnReadService doctorBarnReadService,
                          BarnSearchReadService barnSearchReadService,
                          DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                          DoctorGroupReadService doctorGroupReadService,
                          DoctorMessageUserReadService doctorMessageUserReadService,
                          DoctorPigReadService doctorPigReadService) {
        this.doctorBarnReadService = doctorBarnReadService;
        this.barnSearchReadService = barnSearchReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorMessageUserReadService = doctorMessageUserReadService;
        this.doctorPigReadService = doctorPigReadService;
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
    public Paging<SearchedPig> searchSowPigs(@RequestParam(required = false) Integer pageNo,
                                             @RequestParam(required = false) Integer pageSize,
                                             @RequestParam Map<String, String> params) {
        return pagePigs(pageNo, pageSize, params, DoctorPig.PIG_TYPE.SOW);
    }

    private Paging<SearchedPig> pagePigs(Integer pageNo, Integer pageSize, Map<String, String> params, DoctorPig.PIG_TYPE pigType){
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
                DoctorPig.PIG_TYPE pig_type = DoctorPig.PIG_TYPE.from(searchedPig.getPigType());
                if(pig_type != null){
                    searchedPig.setPigTypeName(pig_type.getDesc());
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
        return pagePigs(1, size, params, DoctorPig.PIG_TYPE.SOW).getData();
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
        return pagePigs(pageNo, pageSize, params, DoctorPig.PIG_TYPE.BOAR);
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
        return pagePigs(1, 2000, params, DoctorPig.PIG_TYPE.BOAR).getData();
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
    public GroupPaging<SearchedGroup> searchGroups(@RequestParam(required = false) Integer pageNo,
                                                   @RequestParam(required = false) Integer pageSize,
                                                   @RequestParam Map<String, String> params) {
        params = filterNullOrEmpty(params);
        if (farmIdNotExist(params)) {
            return new GroupPaging<>(0L, Collections.emptyList());
        }
        searchFromMessage(params);

        replaceKey(params, "q", "groupCode");
        replaceKey(params, "pigTypes", "pigTypeCommas");

        List<Integer> pigTypes = null;
        if(params.get("pigTypes") != null){
            pigTypes = Splitters.splitToInteger(params.get("pigTypes"), Splitters.UNDERSCORE);
            params.remove("pigTypes");
        }
        DoctorGroupSearchDto searchDto = JsonMapper.nonEmptyMapper().fromJson(JsonMapper.nonEmptyMapper().toJson(params), DoctorGroupSearchDto.class);
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
                return new GroupPaging<>(0L, Collections.emptyList());
            }else{
                searchDto.setBarnIdList(Lists.newArrayList(barnId));
            }
        }

        Paging<DoctorGroupDetail> groupDetailPaging = RespHelper.or500(doctorGroupReadService.pagingGroup(searchDto, pageNo, pageSize));
        Long groupCount = RespHelper.orServEx(doctorGroupReadService.getGroupCount(searchDto));
        return transGroupPaging(groupDetailPaging, groupCount);
    }

    private static void replaceKey(Map<String, String> params, String oldKey, String newKey) {
        if (params.containsKey(oldKey)) {
            params.put(newKey, params.get(oldKey));
        }
    }

    private GroupPaging<SearchedGroup> transGroupPaging(Paging<DoctorGroupDetail> groupDetailPaging, Long count) {
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
        return new GroupPaging<>(groupDetailPaging.getTotal(), searchedGroups, count);
    }

    /**
     * 猪舍搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     * @return
     * @see `DefaultBarnQueryBuilder#buildTerm`
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
        if (barnDto.getFarmId() == null || isEmpty(barnDto.getBarnIds())) {
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

                        barnStatus = Lists.newArrayList(SearchedBarn.createFarrowStatus(groupCount));
                        barnStatus = addPigBarnStatus(barnStatus, pigTracks);
                    }
                    else if (JUST_GROUPS.contains(pigType)) {
                        groupCount = getGroupCount(barn);
                        barnStatus = Lists.newArrayList(SearchedBarn.createGroupStatus(pigType, groupCount));
                    }
                    else if (JUST_PIGS.contains(pigType)) {
                        List<DoctorPigTrack> pigTracks = RespHelper.or500(doctorPigReadService.findActivePigTrackByCurrentBarnId(barn.getId()));
                        pigCount = pigTracks.size();
                        barnStatus = addPigBarnStatus(barnStatus, pigTracks);
                    }

                    barn.setPigCount(pigCount);
                    barn.setPigGroupCount(groupCount);
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

    //获取猪舍中猪的状态聚合
    private List<SearchedBarn.BarnStatus> addPigBarnStatus(List<SearchedBarn.BarnStatus> barnStatus, List<DoctorPigTrack> pigTracks) {
        for (Map.Entry<Integer, List<DoctorPigTrack>> m : pigTracks.stream().collect(Collectors.groupingBy(DoctorPigTrack::getStatus)).entrySet()) {
            barnStatus.add(SearchedBarn.createPigStatus(PigStatus.from(m.getKey()), m.getValue().size()));
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
                params.put("pigType", DoctorPig.PIG_TYPE.BOAR.getKey().toString());
                params.put("statuses", PigStatus.BOAR_ENTRY.toString());
            } else if (searchType.equals(SearchType.SOW.getValue())) {
                params.put("pigType", DoctorPig.PIG_TYPE.SOW.getKey().toString());
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
                DoctorPig.PIG_TYPE pigType = searchType.equals(SearchType.SOW.getValue()) ? DoctorPig.PIG_TYPE.SOW : DoctorPig.PIG_TYPE.BOAR;

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

    /**
     * 获取当前用户所拥有猪舍权限的猪舍IDS
     *
     * @return 猪舍IDS
     */
    public List<String> getUserAccessBarnIds(Map<String, String> params) {
        List<String> list = Lists.newArrayList();
        BaseUser user = UserUtil.getCurrentUser();
        DoctorUserDataPermission doctorUserDataPermission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(user.getId()));
        if (doctorUserDataPermission == null) {
            return null;
        }
        //获取猪舍ids
        String barnIds = doctorUserDataPermission.getBarnIds();
        if (StringUtils.isBlank(barnIds)) {
            return null;
        }
        String barnId = params.get("barnId");
        if (StringUtils.isNotBlank(barnId)) {
            List<String> barnIdList = Splitters.COMMA.splitToList(barnIds);
            if (!barnIdList.contains(barnId)) {
                return null;
            }
            barnIds = barnId;
            params.remove("barnId");
        }
        list.add(barnIds);
        return list;
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
