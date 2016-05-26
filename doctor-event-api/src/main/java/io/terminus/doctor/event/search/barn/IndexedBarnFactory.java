package io.terminus.doctor.event.search.barn;

import io.terminus.doctor.event.model.DoctorBarn;

/**
 * Desc: 猪舍索引对象创建接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public interface IndexedBarnFactory<T extends IndexedBarn> {

    /**
     * 创建猪舍索引
     *
     * @param barn 猪舍
     * @param objects 其他信息
     * @return 猪舍索引或其子类
     */
    T create(DoctorBarn barn, Object... objects);
}
