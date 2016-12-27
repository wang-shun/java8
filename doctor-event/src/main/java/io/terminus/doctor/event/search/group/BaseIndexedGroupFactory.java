package io.terminus.doctor.event.search.group;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Desc: 猪群(索引对象)创建工厂
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Slf4j
public abstract class BaseIndexedGroupFactory<T extends IndexedGroup> implements IndexedGroupFactory<T> {

    protected final Class<T> clazz;

    @SuppressWarnings("all")
    public BaseIndexedGroupFactory() {
        final Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            clazz = ((Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0]);
        } else {
            clazz = ((Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass())
                    .getActualTypeArguments()[0]);
        }
    }

    @Override
    public T create(DoctorGroup group, DoctorGroupTrack groupTrack, Object... others) {
        if (group == null) {
            return null;
        }
        // 1. 处理猪群信息
        T indexedGroup = BeanMapper.map(group, clazz);
        PigType pigType = PigType.from(indexedGroup.getPigType());
        indexedGroup.setPigTypeName(pigType == null ? "" : ( pigType.getDesc() + "[" + pigType.getType() + "]"));
        indexedGroup.setGroupCodeSearch(group.getGroupCode().toLowerCase());

        // 2. 处理 Track 信息
        if (groupTrack != null) {
            indexedGroup.setQuantity(groupTrack.getQuantity());
            indexedGroup.setAvgDayAge(groupTrack.getAvgDayAge());
            indexedGroup.setUpdatedAt(groupTrack.getUpdatedAt());
        }
        return indexedGroup;
    }
}
