package io.terminus.doctor.event.search.pig;

import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;

/**
 * Desc: 猪(索引对象)创建接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public interface IndexedPigFactory<T extends IndexedPig> {

    /**
     * 创建猪(索引对象)的方法
     *
     * @param pig      猪信息
     * @param pigTrack 猪Track信息
     * @param others   其他一些信息
     * @return
     */
    T create(DoctorPig pig, DoctorPigTrack pigTrack, Object... others);
}
