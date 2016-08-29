package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
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

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.notNull;

/**
 * Desc: 基础数据写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@Service
@RpcProvider
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
            basic.setIsValid(1);
            DoctorBasic.Type type = DoctorBasic.Type.from(basic.getType());
            checkArgument(notNull(type), "basic.type.fail");
            basic.setTypeName(type.getDesc());
            doctorBasicDao.create(basic);
            publishBasicEvent(basic);
            return Response.ok(basic.getId());
        } catch (IllegalArgumentException e) {
            return Response.fail(e.getMessage());
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
            DoctorBasic basic = doctorBasicDao.findById(basicId);
            if (notNull(basic)) {
                basic.setIsValid(-1);
                doctorBasicDao.update(basic);
                publishBasicEvent(basic);
            }
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

    /**
     * 录入事件时录入客户
     *
     * @param farmId       猪场id
     * @param farmName     猪场名称
     * @param customerId   客户id(根据此字段判断是否create)
     * @param customerName 客户名称
     * @return 是否成功
     */
    @Override
    public Response<Boolean> addCustomerWhenInput(Long farmId, String farmName, Long customerId, String customerName, Long creatorId, String creatorName) {
        try {
            if (customerId != null || isEmpty(customerName)) {
                return Response.ok(true);
            }
            if (isExistCustomerName(farmId, customerName)) {
                return Response.fail("customer.name.is.duplicate");
            }

            DoctorCustomer customer = new DoctorCustomer();
            customer.setName(customerName);
            customer.setFarmId(farmId);
            customer.setFarmName(farmName);
            customer.setCreatorId(creatorId);
            customer.setCreatorName(creatorName);
            doctorCustomerDao.create(customer);
            return Response.ok(true);
        } catch (Exception e) {
            log.error("add customer when input failed, farmId:{}, customerId:{}, customerName:{}, creatorId:{}, cause:{}",
                    farmId, customerId, customerName, creatorId, Throwables.getStackTraceAsString(e));
            return Response.fail("customer.create.fail");
        }
    }

    //true 重名
    private boolean isExistCustomerName(Long farmId, String customerName) {
        List<String> customerNames = doctorCustomerDao.findByFarmId(farmId).stream().map(DoctorCustomer::getName).collect(Collectors.toList());
        return customerNames.contains(customerName);
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
