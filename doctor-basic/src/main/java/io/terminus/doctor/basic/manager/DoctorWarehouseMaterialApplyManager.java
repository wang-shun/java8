package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/21.
 */
@Component
public class DoctorWarehouseMaterialApplyManager {


    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;


    @Transactional
    public void creates(List<DoctorWarehouseMaterialApply> materialApplies) {
        materialApplies.forEach(apply ->
                doctorWarehouseMaterialApplyDao.create(apply)
        );
    }

}
