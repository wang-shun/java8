package io.terminus.doctor.event.search.barn;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;

import java.util.Map;

/**
 * Desc: 猪舍ElasticSearch搜索读接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public interface BarnSearchReadService {

    /**
     * 聚合分页搜索猪舍
     *
     * @param pageNo        页码
     * @param pageSize      页大小
     * @param template      模板路径
     * @param params        查询参数
     * @return 分页后的猪舍搜索时结果
     */
    Response<Paging<SearchedBarn>> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params);
}
