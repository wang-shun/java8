package io.terminus.doctor.event.search.pig;

import io.terminus.common.model.Response;

import java.util.Map;

/**
 * Desc: 猪(索引对象)查询服务
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
public interface PigSearchReadService {

    /**
     * 公共搜索方法
     * @param pageNo        页码
     * @param pageSize      页大小
     * @param template      模板路径
     * @param params        查询参数
     * @return
     */
    Response<SearchedPigDto> searchWithAggs(Integer pageNo, Integer pageSize, String template, Map<String, String> params);

    /**
     * 初始化ES搜索索引
     * @param type 0.所有 1 猪 2 猪群 3 猪舍
     * @return
     */
    Response<Boolean> initIndex(Integer type);

}
