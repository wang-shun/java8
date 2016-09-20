package io.terminus.doctor.warehouse.handler;

import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;

import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: 不同的物料类型， 数据表的处理方式
 */
public interface IHandler {

    /**
     * 校验处理
     * @param dto
     * @param context
     * @return
     */
    Boolean ifHandle(DoctorMaterialConsumeProviderDto dto, Map<String,Object> context);

    /**
     * 修改对应的数据表信息
     * @param dto
     * @param context
     * @throws RuntimeException
     */
    void handle(DoctorMaterialConsumeProviderDto dto, Map<String,Object> context) throws RuntimeException;

    boolean canRollback(Long eventId);

    void rollback(Long eventId);
}
