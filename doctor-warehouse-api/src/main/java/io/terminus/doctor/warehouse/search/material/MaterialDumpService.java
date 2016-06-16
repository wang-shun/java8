package io.terminus.doctor.warehouse.search.material;

import io.terminus.common.model.Response;

/**
 * Desc: 物料(索引对象)建立索引服务
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/16
 */
public interface MaterialDumpService {

    /**
     * 全量dump
     * @return
     */
    Response<Boolean> fullDump(String before);

    /**
     * 增量dump
     * @param interval  间隔时间(分钟)
     * @return
     */
    Response<Boolean> deltaDump(Integer interval);
}
