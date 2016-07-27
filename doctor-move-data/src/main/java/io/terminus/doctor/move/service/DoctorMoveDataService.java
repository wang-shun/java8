package io.terminus.doctor.move.service;

import com.google.common.base.Throwables;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.B_Customer;
import io.terminus.doctor.move.model.View_PigLocationList;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Slf4j
@Service
public class DoctorMoveDataService {

    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorBarnDao doctorBarnDao;
    private final DoctorCustomerDao doctorCustomerDao;

    //Just for mock!
    private final DoctorFarm farm;
    private final DoctorOrg org;
    private final DoctorUser user;

    @Autowired
    public DoctorMoveDataService(DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                 DoctorBarnDao doctorBarnDao,
                                 DoctorCustomerDao doctorCustomerDao) {
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorBarnDao = doctorBarnDao;
        this.doctorCustomerDao = doctorCustomerDao;

        this.farm = new DoctorFarm();
        this.org = new DoctorOrg();
        this.user = new DoctorUser();
    }

    /**
     * 迁移Barn
     */
    @Transactional
    public Response<Boolean> moveBarn(Long moveId) {
        try {
            List<DoctorBarn> barns = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findAllData(moveId, View_PigLocationList.class, DoctorMoveTableEnum.view_PigLocationList)).stream()
                    .map(this::getBarn)
                    .collect(Collectors.toList());

            doctorBarnDao.creates(barns);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move barn failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.barn.fail");
        }
    }

    /**
     * 迁移客户
     */
    @Transactional
    public Response<Boolean> moveCustomer(Long moveId) {
        try {
            List<DoctorCustomer> customers = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findAllData(moveId, B_Customer.class, DoctorMoveTableEnum.B_Customer)).stream()
                    .map(this::getCustomer)
                    .collect(Collectors.toList());

            doctorCustomerDao.creates(customers);
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move customer failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.customer.fail");
        }
    }

    //拼接客户
    private DoctorCustomer getCustomer(B_Customer cus) {
        DoctorCustomer customer = new DoctorCustomer();
        customer.setName(cus.getCustomerName());
        customer.setFarmId(farm.getId());
        customer.setFarmName(farm.getName());
        customer.setMobile(cus.getMobilePhone());
        customer.setEmail(cus.getEMail());
        customer.setOutId(cus.getOID());
        customer.setCreatorId(user.getId());
        customer.setCreatorName(user.getName());
        return customer;
    }

    //拼接barn
    private DoctorBarn getBarn(View_PigLocationList location) {
        //转换pigtype
        PigType pigType = PigType.from(location.getTypeName());

        DoctorBarn barn = new DoctorBarn();
        barn.setName(location.getBarn());
        barn.setOrgId(org.getId());
        barn.setOrgName(org.getName());
        barn.setFarmId(farm.getId());
        barn.setFarmName(farm.getName());
        barn.setPigType(pigType == null ? 0: pigType.getValue());
        barn.setCanOpenGroup("可以".equals(location.getCanOpenGroupText()) ? 1 : -1);
        barn.setStatus("在用".equals(location.getIsStopUseText()) ? 1 : 0);
        barn.setStaffId(user.getId());
        barn.setStaffName(user.getName());
        barn.setOutId(location.getOID());
        return barn;
    }
}
