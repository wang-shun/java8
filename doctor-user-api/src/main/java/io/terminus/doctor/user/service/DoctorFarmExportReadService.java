package io.terminus.doctor.user.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.user.model.DoctorFarmExport;

import java.util.List;

/**
 * Created by xjn on 17/9/6.
 */
public interface DoctorFarmExportReadService {

    Response<List<DoctorFarmExport>> findFarmExportRecord(String farmName);
}
