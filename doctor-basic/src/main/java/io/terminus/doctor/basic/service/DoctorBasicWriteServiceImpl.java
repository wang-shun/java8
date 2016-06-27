package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorChangeTypeDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.manager.DoctorBasicManager;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Desc: 基础数据写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@Service
public class DoctorBasicWriteServiceImpl implements DoctorBasicWriteService {

    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorChangeTypeDao doctorChangeTypeDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorBasicManager doctorBasicManager;
    private final DoctorBasicDao doctorBasicDao;

    @Autowired
    public DoctorBasicWriteServiceImpl(DoctorChangeReasonDao doctorChangeReasonDao,
                                       DoctorChangeTypeDao doctorChangeTypeDao,
                                       DoctorCustomerDao doctorCustomerDao,
                                       DoctorBasicManager doctorBasicManager,
                                       DoctorBasicDao doctorBasicDao) {
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorChangeTypeDao = doctorChangeTypeDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorBasicManager = doctorBasicManager;
        this.doctorBasicDao = doctorBasicDao;
    }

    @Override
    public Response<Boolean> initFarmBasic(Long farmId) {
        try {
            doctorBasicManager.initFarmBasic(farmId);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("init farm basic data failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("init.farm.basic.fail");
        }
    }

    @Override
    public Response<Long> createBasic(DoctorBasic basic) {
        try {
            doctorBasicDao.create(basic);
            return Response.ok(basic.getId());
        } catch (Exception e) {
            log.error("create basic failed, basic:{}, cause:{}", basic, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateBasic(DoctorBasic basic) {
        try {
            return Response.ok(doctorBasicDao.update(basic));
        } catch (Exception e) {
            log.error("update basic failed, basic:{}, cause:{}", basic, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteBasicById(Long basicId) {
        try {
            return Response.ok(doctorBasicDao.delete(basicId));
        } catch (Exception e) {
            log.error("delete basic failed, basicId:{}, cause:{}", basicId, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.delete.fail");
        }
    }

    @Override
    public Response<Long> createChangeReason(DoctorChangeReason changeReason) {
        try {
            doctorChangeReasonDao.create(changeReason);
            return Response.ok(changeReason.getId());
        } catch (Exception e) {
            log.error("create changeReason failed, changeReason:{}, cause:{}", changeReason, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateChangeReason(DoctorChangeReason changeReason) {
        try {
            return Response.ok(doctorChangeReasonDao.update(changeReason));
        } catch (Exception e) {
            log.error("update changeReason failed, changeReason:{}, cause:{}", changeReason, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteChangeReasonById(Long changeReasonId) {
        try {
            return Response.ok(doctorChangeReasonDao.delete(changeReasonId));
        } catch (Exception e) {
            log.error("delete changeReason failed, changeReasonId:{}, cause:{}", changeReasonId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.delete.fail");
        }
    }

    @Override
    public Response<Long> createChangeType(DoctorChangeType changeType) {
        try {
            doctorChangeTypeDao.create(changeType);
            return Response.ok(changeType.getId());
        } catch (Exception e) {
            log.error("create changeType failed, changeType:{}, cause:{}", changeType, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateChangeType(DoctorChangeType changeType) {
        try {
            return Response.ok(doctorChangeTypeDao.update(changeType));
        } catch (Exception e) {
            log.error("update changeType failed, changeType:{}, cause:{}", changeType, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteChangeTypeById(Long changeTypeId) {
        try {
            return Response.ok(doctorChangeTypeDao.delete(changeTypeId));
        } catch (Exception e) {
            log.error("delete changeType failed, changeTypeId:{}, cause:{}", changeTypeId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.delete.fail");
        }
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
