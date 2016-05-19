package io.terminus.doctor.web.core.service;

import io.terminus.doctor.user.enums.TargetSystem;
import io.terminus.doctor.user.model.TargetSystemModel;

/**
 * 陈增辉 on 16/5/18.
 */
public interface OtherSystemService {

    /**
     *将目标系统枚举转换为javabean, 以方便获取配置的值
     * @param targetSystem
     * @return
     */
    TargetSystemModel getTargetSystemModel(TargetSystem targetSystem);
}
