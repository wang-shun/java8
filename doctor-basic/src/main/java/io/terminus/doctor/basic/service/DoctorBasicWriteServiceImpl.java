package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.CoreEventDispatcher;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static io.terminus.common.utils.Arguments.notNull;

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
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorBasicDao doctorBasicDao;
    private final CoreEventDispatcher coreEventDispatcher;

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorBasicWriteServiceImpl(DoctorChangeReasonDao doctorChangeReasonDao,
                                       DoctorCustomerDao doctorCustomerDao,
                                       DoctorBasicDao doctorBasicDao,
                                       CoreEventDispatcher coreEventDispatcher) {
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorBasicDao = doctorBasicDao;
        this.coreEventDispatcher = coreEventDispatcher;
    }

    @Override
    public Response<Long> createBasic(DoctorBasic basic) {
        try {
            doctorBasicDao.create(basic);
            publishBasicEvent(basic);
            return Response.ok(basic.getId());
        } catch (Exception e) {
            log.error("create basic failed, basic:{}, cause:{}", basic, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateBasic(DoctorBasic basic) {
        try {
            doctorBasicDao.update(basic);
            publishBasicEvent(basic);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update basic failed, basic:{}, cause:{}", basic, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteBasicById(Long basicId) {
        try {
            publishBasicEvent(doctorBasicDao.findById(basicId));
            doctorBasicDao.delete(basicId);
            return Response.ok(Boolean.TRUE);
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

    //发布清理基础数据缓存的事件
    private void publishBasicEvent(DoctorBasic basic){
        if(notNull(publisher)) {
            try {
                publisher.publish(DataEvent.toBytes(DataEventType.BasicUpdate.getKey(), basic));
            } catch (Exception e) {
                log.error("publish basic zk event fail, data:{} cause:{}", basic, Throwables.getStackTraceAsString(e));
            }
        } else {
            coreEventDispatcher.publish(DataEvent.make(DataEventType.BasicUpdate.getKey(), basic));
        }
    }
}
