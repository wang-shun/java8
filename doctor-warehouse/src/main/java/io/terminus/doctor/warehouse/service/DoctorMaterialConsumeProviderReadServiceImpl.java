package io.terminus.doctor.warehouse.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.warehouse.dao.DoctorMaterialConsumeProviderDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialConsumeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RpcProvider
@Slf4j
public class DoctorMaterialConsumeProviderReadServiceImpl implements DoctorMaterialConsumeProviderReadService {

    private final DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao;

    @Autowired
    public DoctorMaterialConsumeProviderReadServiceImpl(DoctorMaterialConsumeProviderDao doctorMaterialConsumeProviderDao) {
        this.doctorMaterialConsumeProviderDao = doctorMaterialConsumeProviderDao;
    }

    @Override
    public Response<Paging<DoctorMaterialConsumeProvider>> page(Long warehouseId, Long materialId, Integer eventType, Integer materilaType,
                                                              Long staffId, String startAt, String endAt, Integer pageNo, Integer size) {
        try{
            DoctorMaterialConsumeProvider model = DoctorMaterialConsumeProvider.builder()
                    .wareHouseId(warehouseId).materialId(materialId).eventType(eventType).type(materilaType)
                    .staffId(staffId).build();
            Map<String, Object> map = BeanMapper.convertObjectToMap(model);
            map.put("startAt", startAt);
            map.put("endAt", endAt);
            PageInfo pageInfo = new PageInfo(pageNo, size);
            return Response.ok(doctorMaterialConsumeProviderDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), Params.filterNullOrEmpty(map)));
        }catch(Exception e){
            log.error("page DoctorMaterialConsumeProvider failed, cause :{}", Throwables.getStackTraceAsString(e));
            return Response.fail("page.DoctorMaterialConsumeProvider.fail");
        }
    }
}
