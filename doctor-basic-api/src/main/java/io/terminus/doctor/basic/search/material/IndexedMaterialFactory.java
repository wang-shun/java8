package io.terminus.doctor.basic.search.material;

import io.terminus.doctor.basic.model.DoctorBasicMaterial;

/**
 * Desc: 物料(索引对象)创建接口
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
public interface IndexedMaterialFactory<T extends IndexedMaterial> {

    /**
     * 创建物料(索引对象)的方法
     *
     * @param material  物料信息
     * @param others    其他一些信息
     * @return
     */
    T create(DoctorBasicMaterial material, Object... others);
}
