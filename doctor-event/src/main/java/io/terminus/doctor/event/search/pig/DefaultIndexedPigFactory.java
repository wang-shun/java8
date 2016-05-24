package io.terminus.doctor.event.search.pig;

import io.terminus.doctor.event.dao.DoctorPigDao;

/**
 * Desc: 默认猪(索引对象)创建工厂
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public class DefaultIndexedPigFactory extends BaseIndexedPigFactory<IndexedPig> {
    public DefaultIndexedPigFactory(DoctorPigDao doctorPigDao) {
        super(doctorPigDao);
    }
}
