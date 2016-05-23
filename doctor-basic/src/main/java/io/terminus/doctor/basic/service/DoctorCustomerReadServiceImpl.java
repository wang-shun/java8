package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Desc: 变动类型表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorCustomerReadServiceImpl implements DoctorCustomerReadService {

    private final DoctorCustomerDao doctorCustomerDao;

    @Autowired
    public DoctorCustomerReadServiceImpl(DoctorCustomerDao doctorCustomerDao) {
        this.doctorCustomerDao = doctorCustomerDao;
    }

    @Override
    public Response<DoctorCustomer> findCustomerById(Long customerId) {
        try {
            return Response.ok(doctorCustomerDao.findById(customerId));
        } catch (Exception e) {
            log.error("find customer by id failed, customerId:{}, cause:{}", customerId, Throwables.getStackTraceAsString(e));
            return Response.fail("customer.find.fail");
        }
    }

    @Override
    public Response<List<DoctorCustomer>> findCustomersByFarmId(Long farmId) {
        try {
            return Response.ok(doctorCustomerDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find customer by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("customer.find.fail");
        }
    }
}
