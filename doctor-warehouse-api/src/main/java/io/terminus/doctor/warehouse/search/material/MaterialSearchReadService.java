package io.terminus.doctor.warehouse.search.material;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;

import java.util.Map;

/**
 * Desc: 物料ElasticSearch搜索写接口
 * Mail: chk@terminus.io
 * author: IceMimosa
 * Date: 16/6/16
 */
public interface MaterialSearchReadService {

    /**
     * 聚合分页搜索物料
     *
     * @param pageNo        页码
     * @param pageSize      页大小
     * @param template      模板路径
     * @param params        查询参数
     * @return 分页后的物料搜索时结果
     */
    Response<Paging<SearchedMaterial>> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params);
}
