package io.terminus.doctor.basic.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorWareHouseCriteria;
import io.terminus.doctor.basic.model.DoctorWareHouse;

import java.util.List;


/**
 * Created by sunbo@terminus.io on 2017/8/9.
 */
public interface NewDoctorWarehouseReaderService {


    Response<Paging<DoctorWareHouse>> paging(DoctorWareHouseCriteria criteria);

    Response<List<DoctorWareHouse>> findByFarmId(Long farmId);

    Response<DoctorWareHouse> findById(Long warehouseId);

    Response<List<DoctorWareHouse>> list(DoctorWareHouse criteria);
}
