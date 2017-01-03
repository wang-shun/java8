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
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.search.barn.BarnSearchReadService;
import io.terminus.doctor.event.search.barn.SearchedBarn;
import io.terminus.doctor.event.search.barn.SearchedBarnDto;
import io.terminus.doctor.event.search.group.GroupSearchReadService;
import io.terminus.doctor.event.search.group.SearchedGroup;
import io.terminus.doctor.event.search.pig.PigSearchReadService;
import io.terminus.doctor.event.search.pig.SearchedPig;
import io.terminus.doctor.event.search.query.GroupPaging;
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

    private final PigSearchReadService pigSearchReadService;

    private final GroupSearchReadService groupSearchReadService;

    private final BarnSearchReadService barnSearchReadService;

    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    private final DoctorGroupReadService doctorGroupReadService;

    private final DoctorMessageUserReadService doctorMessageUserReadService;

    private final DoctorPigReadService doctorPigReadService;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();


    @Autowired
    public DoctorSearches(PigSearchReadService pigSearchReadService,
                          GroupSearchReadService groupSearchReadService,
                          BarnSearchReadService barnSearchReadService,
                          DoctorUserDataPermissionReadService doctorUserDataPermissionReadService,
                          DoctorGroupReadService doctorGroupReadService,
                          DoctorMessageUserReadService doctorMessageUserReadService,
                          DoctorPigReadService doctorPigReadService) {
        this.pigSearchReadService = pigSearchReadService;
        this.groupSearchReadService = groupSearchReadService;
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
    public SearchedBarnDto searchBarns(@RequestParam(required = false) Integer pageNo,
                                       @RequestParam(required = false) Integer pageSize,
                                       @RequestParam Map<String, String> params) {
        List<String> barnIdList = getUserAccessBarnIds(params);
        // 查询出分页后的猪舍
        if (farmIdNotExist(params) || barnIdList == null) {
            return new SearchedBarnDto(new Paging<>(0L, Collections.emptyList()), Collections.emptyList());
        }
        params.put("barnIds", barnIdList.get(0));
        Paging<SearchedBarn> barns =
                RespHelper.orServEx(barnSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getBarns();

        // 查询已存在的猪类型, 因为聚合猪类型, 所以去除
        if (params.containsKey("pigType")) {
            params.remove("pigType");
        }
        List<SearchedBarnDto.PigType> aggPigTypes =
                RespHelper.orServEx(barnSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getAggPigTypes();

        return SearchedBarnDto.builder()
                .barns(barns)
                .aggPigTypes(aggPigTypes)
                .build();
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
        List<String> barnIdList = getUserAccessBarnIds(params);

        // 查询出分页后的猪舍
        if (farmIdNotExist(params) || barnIdList == null) {
            return new Paging<>(0L, Collections.emptyList());
        }
        params.put("barnIds", barnIdList.get(0));
        return RespHelper.orServEx(barnSearchReadService.searchTypeWithAggs(pageNo, pageSize, "search/search.mustache", params));
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
        List<String> barnIdList = getUserAccessBarnIds(params);
        if (farmIdNotExist(params) || barnIdList == null) {
            return Collections.emptyList();
        }
        params.put("barnIds", barnIdList.get(0));
        // 游标法获取数据
        Integer pageNo = 1;
        Integer pageSize = 100;
        Paging<SearchedBarn> searchBarns =
                RespHelper.or500(barnSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getBarns();
        while (!searchBarns.isEmpty()) {
            pageNo++;
            Paging<SearchedBarn> tempSearchBarns =
                    RespHelper.or500(barnSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getBarns();
            if (tempSearchBarns.isEmpty()) {
                break;
            }
            searchBarns.getData().addAll(tempSearchBarns.getData());
        }
        return searchBarns.getData();
    }

    private boolean farmIdNotExist(Map<String, String> params) {
        return isEmpty(params.get("farmId"));
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Long> getAllPigIds(@RequestParam(required = false) String ids,
                                   @RequestParam Integer searchType,
                                   @RequestParam Map<String, String> params) {

        try {
            List<String> barnIdList = getUserAccessBarnIds(params);
            if (farmIdNotExist(params) || barnIdList == null) {
                return Collections.emptyList();
            }
            params.put("barnIds", barnIdList.get(0));

            // 母猪状态由前台传
            if (searchType.equals(SearchType.GROUP.getValue())) {
                params.put("status", String.valueOf(DoctorGroup.Status.CREATED.getValue()));
            } else if (searchType.equals(SearchType.BOAR.getValue())) {
                params.put("pigType", DoctorPig.PIG_TYPE.BOAR.getKey().toString());
                params.put("statuses", PigStatus.BOAR_ENTRY.toString());
            } else if (searchType.equals(SearchType.SOW.getValue())) {
                params.put("pigType", DoctorPig.PIG_TYPE.SOW.getKey().toString());
                //只查询未离场的猪
                params.put("isRemoval", String.valueOf(IsOrNot.NO.getValue()));
            }
            params.remove("ids");
            params.remove("searchType");

            List<Long> allPigOrGroupIds;
            Integer pageNo = 1;
            Integer pageSize = 100;
            if (searchType.equals(SearchType.GROUP.getValue())) {
                Paging<SearchedGroup> searchGroupPaging = RespHelper.or500(groupSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getGroups();
                while (!searchGroupPaging.isEmpty()) {
                    pageNo++;
                    Paging<SearchedGroup> tempSearchGroups =
                            RespHelper.or500(groupSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getGroups();
                    if (tempSearchGroups.isEmpty()) {
                        break;
                    }
                    searchGroupPaging.getData().addAll(tempSearchGroups.getData());
                }
                allPigOrGroupIds = searchGroupPaging.getData().stream().map(SearchedGroup::getId).collect(Collectors.toList());
            } else {
                Paging<SearchedPig> searchPigPaging =
                        RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();
                while (!searchPigPaging.isEmpty()) {
                    pageNo++;
                    Paging<SearchedPig> tempSearchPigs =
                            RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();
                    if (tempSearchPigs.isEmpty()) {
                        break;
                    }
                    searchPigPaging.getData().addAll(tempSearchPigs.getData());
                }
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
