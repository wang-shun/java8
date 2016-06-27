package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.cache.DoctorBasicCacher;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorChangeTypeDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isEmpty;
import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 基础数据读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@Service
public class DoctorBasicReadServiceImpl implements DoctorBasicReadService {

    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorChangeTypeDao doctorChangeTypeDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorBasicDao doctorBasicDao;
    private final DoctorBasicCacher doctorBasicCacher;

    @Autowired
    public DoctorBasicReadServiceImpl(DoctorChangeReasonDao doctorChangeReasonDao,
                                      DoctorChangeTypeDao doctorChangeTypeDao,
                                      DoctorCustomerDao doctorCustomerDao,
                                      DoctorBasicDao doctorBasicDao,
                                      DoctorBasicCacher doctorBasicCacher) {
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorChangeTypeDao = doctorChangeTypeDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorBasicDao = doctorBasicDao;
        this.doctorBasicCacher = doctorBasicCacher;
    }

    @Override
    public Response<DoctorBasic> findBasicById(Long basicId) {
        try {
            return Response.ok(doctorBasicDao.findById(basicId));
        } catch (Exception e) {
            log.error("find basic by id failed, basicId:{}, cause:{}", basicId, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBasic>> findBasicByTypeAndSrm(Integer type, String srm) {
        try {
            List<DoctorBasic> basics = doctorBasicDao.findByType(type);
            if (isEmpty(srm)) {
                return Response.ok(basics);
            }
            return Response.ok(basics.stream()
                    .filter(basic -> notEmpty(basic.getSrm()) && basic.getSrm().toLowerCase().contains(srm.toLowerCase()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("find basic by type and srm failed, type:{}, srm:{}, cause:{}", type, srm, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBasic>> findBasicByTypeAndSrmWithCache(Integer type, String srm) {
        try {
            List<DoctorBasic> basics = doctorBasicCacher.getBasicCache().getUnchecked(type);
            if (isEmpty(srm)) {
                return Response.ok(basics);
            }
            return Response.ok(basics.stream()
                    .filter(basic -> notEmpty(basic.getSrm()) && basic.getSrm().toLowerCase().contains(srm.toLowerCase()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("find basic by type and srm failed, type:{}, srm:{}, cause:{}", type, srm, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.find.fail");
        }
    }

    @Override
    public Response<DoctorChangeReason> findChangeReasonById(Long changeReasonId) {
        try {
            return Response.ok(doctorChangeReasonDao.findById(changeReasonId));
        } catch (Exception e) {
            log.error("find changeReason by id failed, changeReasonId:{}, cause:{}", changeReasonId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.find.fail");
        }
    }

    @Override
    public Response<List<DoctorChangeReason>> findChangeReasonByChangeTypeId(Long changeTypeId) {
        try {
            return Response.ok(doctorChangeReasonDao.findByChangeTypeId(changeTypeId));
        } catch (Exception e) {
            log.error("find changeReason by id failed, changeTypeId:{}, cause:{}", changeTypeId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.find.fail");
        }
    }

    @Override
    public Response<DoctorChangeType> findChangeTypeById(Long changeTypeId) {
        try {
            return Response.ok(doctorChangeTypeDao.findById(changeTypeId));
        } catch (Exception e) {
            log.error("find changeType by id failed, changeTypeId:{}, cause:{}", changeTypeId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.find.fail");
        }
    }

    @Override
    public Response<List<DoctorChangeType>> findChangeTypesByFarmId(Long farmId) {
        try {
            return Response.ok(doctorChangeTypeDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find changeType by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeType.find.fail");
        }
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
