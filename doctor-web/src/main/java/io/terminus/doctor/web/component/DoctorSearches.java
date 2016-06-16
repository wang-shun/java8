package io.terminus.doctor.web.component;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.terminus.common.model.Paging;
import io.terminus.doctor.basic.enums.SearchType;
import io.terminus.doctor.basic.service.DoctorSearchHistoryService;
import io.terminus.doctor.common.utils.RandomUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.search.barn.BarnSearchReadService;
import io.terminus.doctor.event.search.barn.SearchedBarn;
import io.terminus.doctor.event.search.group.GroupSearchReadService;
import io.terminus.doctor.event.search.group.SearchedGroup;
import io.terminus.doctor.event.search.pig.PigSearchReadService;
import io.terminus.doctor.event.search.pig.SearchedPig;
import io.terminus.doctor.warehouse.search.material.MaterialSearchReadService;
import io.terminus.doctor.warehouse.search.material.SearchedMaterial;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
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


    @Autowired
    public DoctorSearches(PigSearchReadService pigSearchReadService,
                          GroupSearchReadService groupSearchReadService,
                          DoctorSearchHistoryService doctorSearchHistoryService,
                          BarnSearchReadService barnSearchReadService,
                          MaterialSearchReadService materialSearchReadService) {
        this.pigSearchReadService = pigSearchReadService;
        this.groupSearchReadService = groupSearchReadService;
        this.doctorSearchHistoryService = doctorSearchHistoryService;
        this.barnSearchReadService = barnSearchReadService;
        this.materialSearchReadService = materialSearchReadService;
    }

    /**
     * 母猪搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     * @return
     */
    @RequestMapping(value = "/sowpigs", method = RequestMethod.GET)
    public Paging<SearchedPig> searchSowPigs(@RequestParam(required = false) Integer pageNo,
                                             @RequestParam(required = false) Integer pageSize,
                                             @RequestParam Map<String, String> params) {
//        if (farmIdNotExist(params)) {
//            return new Paging<>(0L, Collections.emptyList());
//        }
//        createSearchWord(SearchType.SOW.getValue(), params);
//        params.put("pigType", DoctorPig.PIG_TYPE.SOW.getKey().toString());
//        return RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params));

        pageNo = MoreObjects.firstNonNull(pageNo, 1);
        pageSize = MoreObjects.firstNonNull(pageSize, 10);

        List<SearchedPig> pigs = Lists.newArrayList();
        for (int i = 1; i <= 42; i++) {
            SearchedPig pig = SearchedPig.builder()
                    .id((long) i)
                    .pigCode("K300" + i)
                    .pigType(1)
                    .pigTypeName("母猪")
                    .farmId((long) i)
                    .farmName("天下第一猪场" + i)
                    .outFarmDate(new Date())
                    .inFarmDate(new Date())
                    .status(1)
                    .statusName("待配种")
                    .currentBarnId((long) i)
                    .currentBarnName("猪舍" + i)
                    .dayAge(20)
                    .currentParity(5)
                    .build();
            pigs.add(pig);
        }
        Map<Integer, List<SearchedGroup>> map = Maps.newHashMap();
        List<List<SearchedPig>> ggs = Lists.partition(pigs, new BigDecimal(42).divide(new BigDecimal(pageSize), BigDecimal.ROUND_UP).intValue());

        return new Paging<>(42L, MoreObjects.firstNonNull(ggs.get(pageNo - 1), Collections.emptyList()));
    }

    /**
     * 公猪搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
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
        params.put("pigType", DoctorPig.PIG_TYPE.BOAR.toString());
        return RespHelper.or500(pigSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params));
    }

    /**
     * 猪群搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     * @return
     */
    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public Paging<SearchedGroup> searchGroups(@RequestParam(required = false) Integer pageNo,
                                              @RequestParam(required = false) Integer pageSize,
                                              @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }
//        createSearchWord(SearchType.GROUP.getValue(), params);
//        return RespHelper.or500(groupSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params));
        List<SearchedGroup> groups = Lists.newArrayList();

        pageNo = MoreObjects.firstNonNull(pageNo, 1);
        pageSize = MoreObjects.firstNonNull(pageSize, 8);

        for (int i = 0; i < 40; i++) {
            SearchedGroup group = new SearchedGroup();
            group.setGroupCode("保育"+i+"舍(2016-06-06)");
            group.setPigType(2);
            group.setPigTypeName("保育猪");
            group.setSex(1);
            group.setStatus(1);
            group.setFarmId(0L);
            group.setFarmName("测试猪场");
            group.setOpenAt(new Date());
            group.setCurrentBarnId((long) i);
            group.setCurrentBarnName("保育"+i+"舍");
            group.setQuantity(RandomUtil.random(1, 50));
            group.setAvgDayAge(RandomUtil.random(1, 30));
            group.setAvgWeight(RandomUtil.random(30, 50) * 2.5D);
            group.setWeight(group.getAvgWeight() * group.getQuantity());
            group.setPrice(RandomUtil.random(20, 40) * 2L);
            group.setAmount(group.getPrice() * group.getQuantity());
            groups.add(group);
        }

        Map<Integer, List<SearchedGroup>> map = Maps.newHashMap();
        List<List<SearchedGroup>> ggs = Lists.partition(groups, new BigDecimal(40).divide(new BigDecimal(pageSize), BigDecimal.ROUND_UP).intValue());
        for (int i = 0; i < ggs.size(); i++) {
            map.put(i+1, ggs.get(i));
        }

        return new Paging<>(40L, MoreObjects.firstNonNull(map.get(pageNo), Collections.emptyList()));
    }

    /**
     * 猪舍搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
     * @return
     */
    @RequestMapping(value = "/barns", method = RequestMethod.GET)
    public Paging<SearchedBarn> searchBarns(@RequestParam(required = false) Integer pageNo,
                                            @RequestParam(required = false) Integer pageSize,
                                            @RequestParam Map<String, String> params) {
        if (farmIdNotExist(params)) {
            return new Paging<>(0L, Collections.emptyList());
        }
        createSearchWord(SearchType.BARN.getValue(), params);
        return RespHelper.or500(barnSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params));
    }

    /**
     * 物料搜索方法
     *
     * @param pageNo   起始页
     * @param pageSize 页大小
     * @param params   搜索参数
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
        return RespHelper.or500(materialSearchReadService.searchWithAggs(pageNo, pageSize, "search/search.mustache", params));
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
}
