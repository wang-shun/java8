package io.terminus.doctor.event.search.group;

import io.terminus.common.model.Response;

/**
 * Desc: 猪群(索引对象)建立索引服务
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public interface GroupDumpService {

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
