package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;

import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseSku;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-10-13 13:53:41
 * Created by [ your name ]
 */
@Repository
public class DoctorWarehouseSkuDao extends MyBatisDao<DoctorWarehouseSku> {


    public Optional<DoctorWarehouseSku> findByFarmIdAndCode(Long farmId, String code) {
        List<DoctorWarehouseSku> skus = list(DoctorWarehouseSku.builder().build());
        if (null == skus || skus.isEmpty())
            return Optional.empty();
        else
            return Optional.ofNullable(skus.get(0));
    }

}
