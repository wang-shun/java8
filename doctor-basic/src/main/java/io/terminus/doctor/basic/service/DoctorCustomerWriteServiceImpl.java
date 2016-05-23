package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 变动类型表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
public class DoctorCustomerWriteServiceImpl implements DoctorCustomerWriteService {

    private final DoctorCustomerDao doctorCustomerDao;

    @Autowired
    public DoctorCustomerWriteServiceImpl(DoctorCustomerDao doctorCustomerDao) {
        this.doctorCustomerDao = doctorCustomerDao;
    }

    @Override
    public Response<Long> createCustomer(DoctorCustomer customer) {
        try {
            doctorCustomerDao.create(customer);
            return Response.ok(customer.getId());
        } catch (Exception e) {
            log.error("create customer failed, customer:{}, cause:{}", customer, Throwables.getStackTraceAsString(e));
            return Response.fail("customer.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateCustomer(DoctorCustomer customer) {
        try {
            return Response.ok(doctorCustomerDao.update(customer));
        } catch (Exception e) {
            log.error("update customer failed, customer:{}, cause:{}", customer, Throwables.getStackTraceAsString(e));
            return Response.fail("customer.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteCustomerById(Long customerId) {
        try {
            return Response.ok(doctorCustomerDao.delete(customerId));
        } catch (Exception e) {
            log.error("delete customer failed, customerId:{}, cause:{}", customerId, Throwables.getStackTraceAsString(e));
            return Response.fail("customer.delete.fail");
        }
    }
}
