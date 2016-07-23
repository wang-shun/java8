package io.terminus.doctor.event.search.barn;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Desc: 猪舍索引工厂抽象类, 实现了工厂接口
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
public abstract class BaseIndexedBarnFactory<T extends IndexedBarn> implements IndexedBarnFactory<T> {

    protected final Class<T> clazz;

    protected DoctorBarnReadService doctorBarnReadService;

    @SuppressWarnings("all")
    public BaseIndexedBarnFactory(DoctorBarnReadService doctorBarnReadService) {
        this.doctorBarnReadService = doctorBarnReadService;
        final Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            clazz = ((Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0]);
        } else {
            clazz = ((Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass())
                    .getActualTypeArguments()[0]);
        }
    }

    @Override
    public T create(DoctorBarn barn, Object... objects) {
        if (barn == null) {
            return null;
        }

        T indexedBarn = BeanMapper.map(barn, clazz);
        PigType pigType = PigType.from(indexedBarn.getPigType());
        indexedBarn.setPigTypeName(pigType == null ? "" : ( pigType.getDesc() + "[" + pigType.getType() + "]"));
        indexedBarn.setNameSearch(barn.getName().toLowerCase());
        indexedBarn.setStorage(RespHelper.orServEx(doctorBarnReadService.countPigByBarnId(indexedBarn.getId())));
        return indexedBarn;
    }
}
