package io.terminus.doctor.basic.handler;

import io.terminus.doctor.basic.dto.DoctorMaterialConsumeProviderDto;
import io.terminus.doctor.basic.dto.EventHandlerContext;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: 不同的物料类型， 数据表的处理方式
 */
public interface IHandler {

    /**
     * 校验处理
     * @param eventType
     * @return
     */
    boolean ifHandle(DoctorMaterialConsumeProvider.EVENT_TYPE eventType);

    /**
     * 修改对应的数据表信息
     * @param dto
     * @param context
     * @throws RuntimeException
     */
    void handle(DoctorMaterialConsumeProviderDto dto, EventHandlerContext context) throws RuntimeException;

    /**
     * 回滚事件
     * @param cp
     */
    void rollback(DoctorMaterialConsumeProvider cp);
}
