package io.terminus.doctor.event.search.barn;

import io.terminus.common.model.Response;

/**
 * Desc: dump猪舍数据接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public interface BarnSearchDumpService {

    /**
     * 全量dump
     *
     * @param before dump此时间之前的数据, 如果为空dump当前时间之前的数据, 格式: yyyy-MM-dd HH:mm:ss
     * @return 是否成功
     */
    Response<Boolean> fullDump(String before);

    /**
     * 增量dump
     *
     * @param interval 间隔时间(分钟)
     * @return 是否成功
     */
    Response<Boolean> deltaDump(Integer interval);
}
