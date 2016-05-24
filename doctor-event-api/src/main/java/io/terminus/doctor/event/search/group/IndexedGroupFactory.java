package io.terminus.doctor.event.search.group;

import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.search.pig.IndexedPig;

/**
 * Desc: 猪群(索引对象)创建接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public interface IndexedGroupFactory<T extends IndexedPig> {

    /**
     * 创建猪(索引对象)的方法
     *
     * @param group         猪群信息
     * @param groupTrack    猪群Track信息
     * @param others        其他一些信息
     * @return
     */
    T create(DoctorGroup group, DoctorGroupTrack groupTrack, Object... others);
}
