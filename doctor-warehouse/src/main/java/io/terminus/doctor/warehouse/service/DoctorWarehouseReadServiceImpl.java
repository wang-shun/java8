package io.terminus.doctor.warehouse.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.model.DoctorFarmWareHouseType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe: 仓库信息的读入操作
 */
@Service
@Slf4j
public class DoctorWarehouseReadServiceImpl implements DoctorWarehouseReadService{
    @Override
    public Response<List<DoctorFarmWareHouseType>> queryDoctorFarmWareHouseType(@NotNull(message = "input.farmId.empty") String farmId) {
        return null;
    }
}
