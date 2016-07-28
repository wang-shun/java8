package io.terminus.doctor.move.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.B_ChangeReason;
import io.terminus.doctor.move.model.B_Customer;
import io.terminus.doctor.move.model.TB_FieldValue;
import io.terminus.doctor.move.model.View_PigLocationList;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 迁移数据, 注意如果一个数据源有多个猪场的情况!!!
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Slf4j
@Service
public class DoctorMoveDataService implements CommandLineRunner {

    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorBarnDao doctorBarnDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorBasicDao doctorBasicDao;

    @Autowired
    public DoctorMoveDataService(DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                 DoctorBarnDao doctorBarnDao,
                                 DoctorCustomerDao doctorCustomerDao,
                                 DoctorChangeReasonDao doctorChangeReasonDao,
                                 DoctorBasicDao doctorBasicDao) {
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorBarnDao = doctorBarnDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorBasicDao = doctorBasicDao;
    }

    /**
     * 迁移基础数据
     */
    @Transactional
    public Response<Boolean> moveBasic(Long moveId) {
        try {
            //基础数据按照类型名称分组
            Map<String, List<DoctorBasic>> basicsMap = doctorBasicDao.listAll().stream().collect(Collectors.groupingBy(DoctorBasic::getTypeName));
            Map<String, List<TB_FieldValue>> fieldsMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, TB_FieldValue.class, "TB_FieldValue")).stream()
                    .collect(Collectors.groupingBy(TB_FieldValue::getTypeId));

            basicsMap.entrySet().forEach(basic -> {
                List<TB_FieldValue> fieldValues = fieldsMap.get(basic.getKey());

                //猪场里的基础数据存放成map
                Map<String, TB_FieldValue> fieldMap = Maps.newHashMap();
                fieldValues.forEach(f -> fieldMap.put(f.getFieldText(), f));

                //字段名字的list
                List<String> fieldNames = fieldValues.stream().map(TB_FieldValue::getFieldText).collect(Collectors.toList());

                //求差
                fieldNames.removeAll(basic.getValue().stream().map(DoctorBasic::getName).collect(Collectors.toList()));

                //把求差的结果放到doctor_basics里
                fieldNames.forEach(fn -> doctorBasicDao.create(getBasic(fieldMap.get(fn))));
            });
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("move basic failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.basic.fail");
        }
    }

    //拼接基础数据
    private DoctorBasic getBasic(TB_FieldValue field) {
        DoctorBasic.Type type = DoctorBasic.Type.from(field.getTypeId());
        DoctorBasic basic = new DoctorBasic();
        basic.setName(field.getFieldText());
        basic.setType(type == null ? null : type.getValue());
        basic.setTypeName(field.getTypeId());
        basic.setContext(field.getRemark());
        basic.setOutId(field.getOID());
        basic.setSrm(field.getSrm());
        return basic;
    }

    /**
     * 迁移Barn
     */
    @Transactional
    public Response<Boolean> moveBarn(Long moveId) {
        try {
            List<DoctorBarn> barns = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findAllData(moveId, View_PigLocationList.class, DoctorMoveTableEnum.view_PigLocationList)).stream()
                    .filter(loc -> isFarm(loc.getFarmOID(), mockFarm().getOutId()))     //这一步很重要, 如果一个公司有多个猪场, 猪场id必须匹配!
                    .map(location -> getBarn(mockOrg(), mockFarm(), mockUser(), location))
                    .collect(Collectors.toList());

            if (notEmpty(barns)) {
                doctorBarnDao.creates(barns);
            }
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
                    .map(cus -> getCustomer(mockFarm(), mockUser(), cus))
                    .collect(Collectors.toList());

            if (notEmpty(customers)) {
                doctorCustomerDao.creates(customers);
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move customer failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.customer.fail");
        }
    }

    /**
     * 迁移变动原因
     */
    @Transactional
    public Response<Boolean> moveChangeReason(Long moveId) {
        try {
            List<DoctorChangeReason> reasons = RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, B_ChangeReason.class, "changeReason")).stream()
                    .map(reason -> getReason(mockFarm(), mockUser(), reason))
                    .collect(Collectors.toList());

            if (notEmpty(reasons)) {
                doctorChangeReasonDao.creates(reasons);
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("move customer failed, moveId:{}, cause:{}", moveId, Throwables.getStackTraceAsString(e));
            return Response.fail("move.customer.fail");
        }
    }

    //拼接变动原因
    private DoctorChangeReason getReason(DoctorFarm farm, DoctorUser user, B_ChangeReason reason) {
        DoctorBasicEnums changeType = DoctorBasicEnums.from(reason.getChangeType());

        DoctorChangeReason changeReason = new DoctorChangeReason();
        changeReason.setChangeTypeId(changeType == null ? 0 : changeType.getId());
        changeReason.setReason(reason.getReasonName());
        changeReason.setOutId(reason.getOID());
        changeReason.setCreatorId(user.getId());
        changeReason.setCreatorName(user.getName());
        return changeReason;
    }

    //拼接客户
    private DoctorCustomer getCustomer(DoctorFarm farm, DoctorUser user, B_Customer cus) {
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
    private DoctorBarn getBarn(DoctorOrg org, DoctorFarm farm, DoctorUser user, View_PigLocationList location) {
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

    //判断猪场id是否相同
    private static boolean isFarm(String farmOID, String outId) {
        return Objects.equals(farmOID, outId);
    }

    private static DoctorFarm mockFarm() {
        DoctorFarm farm = new DoctorFarm();
        farm.setId(9999L);
        farm.setName("测试迁移猪场");
        return farm;
    }

    private static DoctorOrg mockOrg() {
        DoctorOrg org = new DoctorOrg();
        org.setId(9999L);
        org.setName("测试迁移公司");
        return org;
    }

    private static DoctorUser mockUser() {
        DoctorUser user = new DoctorUser();
        user.setId(9999L);
        user.setName("测试迁移管理员");
        return user;
    }

    @Override
    public void run(String... strings) throws Exception {
        // Just for test!

    }
}
