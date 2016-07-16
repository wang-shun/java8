package io.terminus.doctor.warehouse.search.material;

import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Desc: 物料(索引对象)创建工厂
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
public abstract class BaseIndexedMaterialFactory<T extends IndexedMaterial> implements IndexedMaterialFactory<T> {

    protected final Class<T> clazz;

    @SuppressWarnings("all")
    public BaseIndexedMaterialFactory() {
        final Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            clazz = ((Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0]);
        } else {
            clazz = ((Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass())
                    .getActualTypeArguments()[0]);
        }
    }

    @Override
    public T create(DoctorMaterialInfo material, Object... others) {
        if (material == null) {
            return null;
        }
        T indexedMaterial = BeanMapper.map(material, clazz);
        WareHouseType type = WareHouseType.from(material.getType());
        indexedMaterial.setTypeName(type != null ? type.getDesc() : "");
        return indexedMaterial;
    }
}
