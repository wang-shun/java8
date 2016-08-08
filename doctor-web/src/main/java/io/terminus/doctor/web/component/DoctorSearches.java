package io.terminus.doctor.web.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Lists;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.basic.enums.SearchType;
import io.terminus.doctor.basic.search.material.MaterialSearchReadService;
import io.terminus.doctor.basic.search.material.SearchedMaterial;
import io.terminus.doctor.basic.service.DoctorSearchHistoryService;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.search.barn.BarnSearchReadService;
import io.terminus.doctor.event.search.barn.SearchedBarn;
import io.terminus.doctor.event.search.barn.SearchedBarnDto;
import io.terminus.doctor.event.search.group.GroupSearchReadService;
import io.terminus.doctor.event.search.group.SearchedGroup;
import io.terminus.doctor.event.search.group.SearchedGroupDto;
import io.terminus.doctor.event.search.pig.PigSearchReadService;
import io.terminus.doctor.event.search.pig.SearchedPig;
import io.terminus.doctor.event.search.pig.SearchedPigDto;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    private final DoctorSearchHistoryService doctorSearchHistoryService;

    private final PigSearchReadService pigSearchReadService;

    private final GroupSearchReadService groupSearchReadService;

    private final BarnSearchReadService barnSearchReadService;

    private final MaterialSearchReadService materialSearchReadService;

    private final DoctorPigEventReadService doctorPigEventReadService;

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();


    @Autowired
    public DoctorSearches(PigSearchReadService pigSearchReadService,
                          GroupSearchReadService groupSearchReadService,
                          DoctorSearchHistoryService doctorSearchHistoryService,
                          BarnSearchReadService barnSearchReadService,
                          MaterialSearchReadService materialSearchReadService,
                          DoctorPigEventReadService doctorPigEventReadService) {
        this.pigSearchReadService = pigSearchReadService;
        this.groupSearchReadService = groupSearchReadService;
        this.doctorSearchHistoryService = doctorSearchHistoryService;
        this.barnSearchReadService = barnSearchReadService;
        this.materialSearchReadService = materialSearchReadService;
        this.doctorPigEventReadService = doctorPigEventReadService;
    }

    /**
     * 母猪搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultPigQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/sowpigs", method = RequestMethod.GET)
    public Paging<SearchedPig> searchSowPigs(@RequestParam(required = false) Integer pageNo,
                                             @RequestParam(required = false) Integer pageSize,
                                             @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }
        createSearchWord(SearchType.SOW.getValue(), params);
        params.put("pigType", DoctorPig.PIG_TYPE.SOW.getKey().toString());
        return RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();
    }

    /**
     * 获取母猪聚合后的状态 数据
     * @param pageNo    起始页
     * @param pageSize  页大小
     * @param params    搜索参数
     *                  搜索参数可以参照:
     *                  @see `DefaultPigQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/sowpigs/status", method = RequestMethod.GET)
    public SearchedPigDto  searchSowStatus(@RequestParam(required = false) Integer pageNo,
                                           @RequestParam(required = false) Integer pageSize,
                                           @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return new SearchedPigDto(new Paging<>(0L, Collections.emptyList()), Collections.emptyList());
        }
        createSearchWord(SearchType.SOW.getValue(), params);
        params.put("pigType", DoctorPig.PIG_TYPE.SOW.getKey().toString());

        // 获取分页后的猪数据
        Paging<SearchedPig> pigs = RespHelper.or500(
                pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();

        // 因为要聚合猪状态, 所以去除状态属性
        if (params.containsKey("status")) {
            params.remove("status");
        }

        List<SearchedPigDto.SowStatus> sowStatuses = RespHelper.or500(
                pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getSowStatuses();

        return SearchedPigDto.builder()
                .pigs(pigs)
                .sowStatuses(sowStatuses)
                .build();
    }

    /**
     * 搜索母猪的suggest
     * @param size      数量
     * @param params    查询参数
     * @return
     */
    @RequestMapping(value = "/sowpigs/suggest", method = RequestMethod.GET)
    public List<SearchedPig> searchSowsSuggest(@RequestParam(required = false) Integer size,
                                               @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return Collections.emptyList();
        }
        params.put("pigType", DoctorPig.PIG_TYPE.SOW.getKey().toString());
        if (size == null) {
            size = 8; // 默认获取 8 条
        }

        Integer pageNo = 1; // 默认取第一页的数据
        Integer pageSize = size;
        Paging<SearchedPig> searchBoars =
                RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();

        return searchBoars.getData();
    }


    /**
     * 公猪搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultPigQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/boarpigs", method = RequestMethod.GET)
    public Paging<SearchedPig> searchBoarPigs(@RequestParam(required = false) Integer pageNo,
                                              @RequestParam(required = false) Integer pageSize,
                                              @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }
        createSearchWord(SearchType.BOAR.getValue(), params);
        params.put("pigType", DoctorPig.PIG_TYPE.BOAR.getKey().toString());
        return RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();
    }

    /**
     * 所有公猪搜索方法
     *
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultPigQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/boarpigs/all", method = RequestMethod.GET)
    public List<SearchedPig> searchAllBoarPigs(@RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return Collections.emptyList();
        }
        params.put("pigType", DoctorPig.PIG_TYPE.BOAR.getKey().toString());

        // 游标法获取数据
        Integer pageNo = 1;
        Integer pageSize = 100;
        Paging<SearchedPig> searchBoars =
                RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();
        while (!searchBoars.isEmpty()) {
            pageNo ++;
            Paging<SearchedPig> tempSearchBoars =
                    RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();
            if (tempSearchBoars.isEmpty()) {
                break;
            }
            searchBoars.getData().addAll(tempSearchBoars.getData());
        }
        return searchBoars.getData();
    }

    /**
     * 猪群搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultGroupQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public Paging<SearchedGroup> searchGroups(@RequestParam(required = false) Integer pageNo,
                                              @RequestParam(required = false) Integer pageSize,
                                              @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }
        createSearchWord(SearchType.GROUP.getValue(), params);
        return RespHelper.or500(groupSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getGroups();
    }

    /**
     * 获取猪群聚合后的状态 数据
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultGroupQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/groups/status", method = RequestMethod.GET)
    public SearchedGroupDto searchGroupStatus(@RequestParam(required = false) Integer pageNo,
                                              @RequestParam(required = false) Integer pageSize,
                                              @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return new SearchedGroupDto(new Paging<>(0L, Collections.emptyList()), Collections.emptyList());
        }
        createSearchWord(SearchType.GROUP.getValue(), params);

        // 获取分页后的猪群数据
        createSearchWord(SearchType.GROUP.getValue(), params);
        Paging<SearchedGroup> groups = RespHelper.or500(
                groupSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getGroups();

        // 查询已存在的猪类型, 因为聚合猪类型, 所以去除
        if (params.containsKey("pigType")) {
            params.remove("pigType");
        }
        List<SearchedBarnDto.PigType> aggPigTypes = RespHelper.or500(
                groupSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getAggPigTypes();

        return SearchedGroupDto.builder()
                .groups(groups)
                .aggPigTypes(aggPigTypes)
                .build();
    }

    /**
     * 猪舍搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultBarnQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/barns", method = RequestMethod.GET)
    public SearchedBarnDto searchBarns(@RequestParam(required = false) Integer pageNo,
                                       @RequestParam(required = false) Integer pageSize,
                                       @RequestParam Map<String, String> params) {
        // 查询出分页后的猪舍
        if (farmIdNotExist(params)) {
            return new SearchedBarnDto(new Paging<>(0L, Collections.emptyList()), Collections.emptyList());
        }
        createSearchWord(SearchType.BARN.getValue(), params);
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
     * 获取所有的猪舍信息
     * @param params 搜索参数
     *               搜索参数可以参照:
     *               @see `DefaultBarnQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/barns/all", method = RequestMethod.GET)
    public List<SearchedBarn> searchAllBarns(@RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return Collections.emptyList();
        }
        // 游标法获取数据
        Integer pageNo = 1;
        Integer pageSize = 100;
        Paging<SearchedBarn> searchBarns =
                RespHelper.or500(barnSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getBarns();
        while (!searchBarns.isEmpty()) {
            pageNo ++;
            Paging<SearchedBarn> tempSearchBarns =
                    RespHelper.or500(barnSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getBarns();
            if (tempSearchBarns.isEmpty()) {
                break;
            }
            searchBarns.getData().addAll(tempSearchBarns.getData());
        }
        return searchBarns.getData();
    }

    /**
     * 物料搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultMaterialQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/materials", method = RequestMethod.GET)
    public Paging<SearchedMaterial> searchMaterials(@RequestParam(required = false) Integer pageNo,
                                                    @RequestParam(required = false) Integer pageSize,
                                                    @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }
        createSearchWord(SearchType.MATERIAL.getValue(), params);
        return RespHelper.or500(materialSearchReadService.searchWithAggs(pageNo, pageSize, "search/masearch.mustache", params));
    }

    /**
     * 获取所有的物料
     *
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultMaterialQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/materials/all", method = RequestMethod.GET)
    public List<SearchedMaterial> searchAllMaterials(@RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return Collections.emptyList();
        }
        // 游标法获取数据
        Integer pageNo = 1;
        Integer pageSize = 100;
        Paging<SearchedMaterial> searchMaterials =
                RespHelper.or500(materialSearchReadService.searchWithAggs(pageNo, pageSize, "search/masearch.mustache", params));
        while (!searchMaterials.isEmpty()) {
            pageNo ++;
            Paging<SearchedMaterial> tempSearchMaterials =
                    RespHelper.or500(materialSearchReadService.searchWithAggs(pageNo, pageSize, "search/masearch.mustache", params));
            if (tempSearchMaterials.isEmpty()) {
                break;
            }
            searchMaterials.getData().addAll(tempSearchMaterials.getData());
        }
        return searchMaterials.getData();
    }

    /**
     * 获取所有的物料
     * @param params   搜索参数
     *                 搜索参数可以参照:
     *                 @see `DefaultMaterialQueryBuilder#buildTerm`
     * @return
     */
    @RequestMapping(value = "/materials/suggest", method = RequestMethod.GET)
    public List<SearchedMaterial> searchSuggestMaterials(@RequestParam(required = false) Integer size,
                                                         @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return Collections.emptyList();
        }
        if (size == null) {
            size = 8; // 默认获取 8 条
        }
        Integer pageNo = 1; // 默认取第一页的数据
        Integer pageSize = size;
        return RespHelper.or500(materialSearchReadService.searchWithAggs(pageNo, pageSize, "search/masearch.mustache", params)).getData();
    }

    /**
     * 创建搜索词
     *
     * @param searchType 搜索类型
     * @param params     搜索参数
     * @see io.terminus.doctor.basic.enums.SearchType
     */
    private void createSearchWord(int searchType, Map<String, String> params) {
        // 获取搜索词
        String q = params.get("q");
        if (StringUtils.isNotBlank(q)) {
            doctorSearchHistoryService.createSearchHistory(UserUtil.getUserId(), searchType, q);
        }
    }

    private boolean farmIdNotExist(Map<String, String> params) {
        return isEmpty(params.get("farmId"));
    }

    @RequestMapping(value = "/query/events/all", method = RequestMethod.GET)
    public List<Integer> getEventsIdByAllPigs (@RequestParam String ids,
                                    @RequestParam Integer searchType,
                                    @RequestParam(required = false) Integer pigType,
                                    @RequestParam Map<String, String> params) {

        try {
            List<Long> excludePigIds = OBJECT_MAPPER.readValue(ids, JacksonType.LIST_OF_LONG);

            if (farmIdNotExist(params)) {
                return Collections.emptyList();
            }
            createSearchWord(searchType, params);
            if (pigType != null) {
                params.put("pigType", pigType.toString());
            }
            if (searchType.equals(SearchType.GROUP.getValue())) {
                params.put("status", String.valueOf(DoctorGroup.Status.CREATED.getValue()));
            } else if (searchType.equals(SearchType.SOW.getValue())) {
                params.put("statuses", "1_3_4_5_6_7_8_9_10");
            } else if (searchType.equals(SearchType.BOAR.getValue())) {
                params.put("statuses", PigStatus.BOAR_ENTRY.toString());
            }
            params.remove("ids");
            params.remove("searchType");

            List<Long> allPigOrGroupIds;
            Integer pageNo = 1;
            Integer pageSize = 100;
            if (searchType.equals(SearchType.GROUP.getValue())) {
                Paging<SearchedGroup> searchGroupPaging = RespHelper.or500(groupSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getGroups();
                while (!searchGroupPaging.isEmpty()) {
                    pageNo ++;
                    Paging<SearchedGroup> tempSearchGroups =
                            RespHelper.or500(groupSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getGroups();
                    if (tempSearchGroups.isEmpty()) {
                        break;
                    }
                    searchGroupPaging.getData().addAll(tempSearchGroups.getData());
                }
                allPigOrGroupIds = FluentIterable.from(searchGroupPaging.getData()).transform(new Function<SearchedGroup, Long>() {
                    @Nullable
                    @Override
                    public Long apply(@Nullable SearchedGroup searchedGroup) {
                        return searchedGroup.getId();
                    }
                }).toList();
            } else {
                Paging<SearchedPig> searchPigPaging =
                        RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();
                while (!searchPigPaging.isEmpty()) {
                    pageNo ++;
                    Paging<SearchedPig> tempSearchPigs =
                            RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params)).getPigs();
                    if (tempSearchPigs.isEmpty()) {
                        break;
                    }
                    searchPigPaging.getData().addAll(tempSearchPigs.getData());
                }
                allPigOrGroupIds = FluentIterable.from(searchPigPaging.getData()).transform(new Function<SearchedPig, Long>() {
                    @Nullable
                    @Override
                    public Long apply(@Nullable SearchedPig searchedPig) {
                        return searchedPig.getId();
                    }
                }).toList();
            }

            List<Long> result = new ArrayList<>();
            for (Long id : allPigOrGroupIds) {
                if (!excludePigIds.contains(id)) {
                    result.add(id);
                }
            }

            return RespHelper.or500(doctorPigEventReadService.queryPigEvents(result));
        } catch (Exception e) {
            throw new JsonResponseException(500, e.getMessage());
        }
    }
}
