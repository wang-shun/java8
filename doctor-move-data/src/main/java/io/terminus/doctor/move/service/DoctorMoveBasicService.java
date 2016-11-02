package io.terminus.doctor.move.service;

import com.google.common.collect.Maps;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.enums.WareHouseType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.handler.DoctorMoveTableEnum;
import io.terminus.doctor.move.model.B_ChangeReason;
import io.terminus.doctor.move.model.B_Customer;
import io.terminus.doctor.move.model.TB_FieldValue;
import io.terminus.doctor.move.model.View_PigLocationList;
import io.terminus.doctor.user.dao.DoctorStaffDao;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.parana.user.impl.dao.UserProfileDao;
import io.terminus.parana.user.model.UserProfile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 移动基础数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/8/4
 */
@Slf4j
@Service
public class DoctorMoveBasicService {

    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorBasicDao doctorBasicDao;
    private final DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    private final DoctorBasicMaterialDao doctorBasicMaterialDao;
    private final DoctorBarnDao doctorBarnDao;
    private final DoctorStaffDao doctorStaffDao;
    private final UserProfileDao userProfileDao;

    @Autowired
    public DoctorMoveBasicService(DoctorCustomerDao doctorCustomerDao,
                                  DoctorChangeReasonDao doctorChangeReasonDao,
                                  DoctorBasicDao doctorBasicDao,
                                  DoctorMoveDatasourceHandler doctorMoveDatasourceHandler,
                                  DoctorBasicMaterialDao doctorBasicMaterialDao,
                                  DoctorBarnDao doctorBarnDao,
                                  DoctorStaffDao doctorStaffDao,
                                  UserProfileDao userProfileDao) {
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorBasicDao = doctorBasicDao;
        this.doctorMoveDatasourceHandler = doctorMoveDatasourceHandler;
        this.doctorBasicMaterialDao = doctorBasicMaterialDao;
        this.doctorBarnDao = doctorBarnDao;
        this.doctorStaffDao = doctorStaffDao;
        this.userProfileDao = userProfileDao;
    }

    /**
     * 迁移全部基础数据
     */
    @Transactional
    public void moveAllBasic(Long moveId, DoctorFarm farm) {
        moveBasic(moveId);
        moveChangeReason(moveId);
        moveCustomer(moveId, farm);
        moveBarn(moveId, farm);
    }

    /**
     * 迁移基础数据
     */
    @Transactional
    public void moveBasic(Long moveId) {
        //基础数据按照类型名称分组
        Map<String, List<DoctorBasic>> basicsMap = doctorBasicDao.listAll().stream().collect(Collectors.groupingBy(DoctorBasic::getTypeName));
        Map<String, List<TB_FieldValue>> fieldsMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, TB_FieldValue.class, "TB_FieldValue")).stream()
                .collect(Collectors.groupingBy(TB_FieldValue::getTypeId));

        //按照遍历doctor里的基础数据, 如果有缺失的, 就补充进来
        for (Map.Entry<String, List<DoctorBasic>> basic : basicsMap.entrySet()) {
            //取出基础字段名称
            List<String> basicNames = basic.getValue().stream().map(DoctorBasic::getName).collect(Collectors.toList());

            List<TB_FieldValue> fieldValues = fieldsMap.get(basic.getKey());
            if (!notEmpty(fieldValues)) {
                continue;
            }

            //把过滤的结果放到doctor_basics里,(过滤变动类型是转出的)
            fieldValues.stream()
                    .filter(field -> !basicNames.contains(field.getFieldText()) &&
                            !("变动类型".equals(field.getTypeId()) && "转出".equals(field.getFieldText())))
                    .forEach(fn -> doctorBasicDao.create(getBasic(fn)));
        }
    }

    /**
     * 迁移客户
     */
    @Transactional
    public void moveCustomer(Long moveId, DoctorFarm farm) {
        List<DoctorCustomer> customers = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, B_Customer.class, "B_Customer")).stream()
                .filter(loc -> isFarm(loc.getFarmOID(), farm.getOutId()))
                .map(cus -> getCustomer(farm, cus))
                .collect(Collectors.toList());

        if (notEmpty(customers)) {
            doctorCustomerDao.creates(customers);
        }
    }

    /**
     * 迁移变动原因
     */
    @Transactional
    public void moveChangeReason(Long moveId) {
        //查出所有的变动
        List<DoctorBasic> changeTypes = doctorBasicDao.findByType(DoctorBasic.Type.CHANGE_TYPE.getValue());

        //查出每个变动下的变动原因, 组装成map
        Map<DoctorBasic, List<DoctorChangeReason>> changeTypeMap = Maps.newHashMap();
        changeTypes.forEach(type -> changeTypeMap.put(type, doctorChangeReasonDao.findByChangeTypeIdAndSrm(type.getId(), null)));

        //查出猪场软件里的所有变动原因, 并按照变动类型 group by
        Map<String, List<B_ChangeReason>> reasonMap = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, B_ChangeReason.class, "changeReason")).stream()
                .collect(Collectors.groupingBy(B_ChangeReason::getChangeType));

        //遍历每个变动类型的变动原因, 过滤掉重复的插入
        for (Map.Entry<DoctorBasic, List<DoctorChangeReason>> changeType : changeTypeMap.entrySet()) {
            //当前doctor里存在的reason名称
            List<String> changeReasons = changeType.getValue().stream().map(DoctorChangeReason::getReason).collect(Collectors.toList());
            List<B_ChangeReason> reasons = reasonMap.get(changeType.getKey().getName());

            if (!notEmpty(reasons)) {
                continue;
            }

            //过滤掉重复的原因, 插入doctor_change_reasons 表
            reasons.stream()
                    .filter(r -> !changeReasons.contains(r.getReasonName()))
                    .forEach(reason -> doctorChangeReasonDao.create(getReason(reason, changeType.getKey().getId())));
        }
    }

    /**
     * 迁移Barn
     */
    @Transactional
    public void moveBarn(Long moveId, DoctorFarm farm) {
        Map<String, Long> subMap = getSubMap(farm.getOrgId());
        List<DoctorBarn> barns = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findAllData(moveId, View_PigLocationList.class, DoctorMoveTableEnum.view_PigLocationList)).stream()
                .filter(loc -> isFarm(loc.getFarmOID(), farm.getOutId()))     //这一步很重要, 如果一个公司有多个猪场, 猪场id必须匹配!
                .map(location -> getBarn(farm, subMap, location))
                .collect(Collectors.toList());

        if (notEmpty(barns)) {
            doctorBarnDao.creates(barns);
        }
    }

    //拼接barn
    private DoctorBarn getBarn(DoctorFarm farm, Map<String, Long> subMap, View_PigLocationList location) {
        //转换pigtype
        PigType pigType = PigType.from(location.getTypeName());
        if(pigType == null && location.getTypeName() != null && location.getTypeName().contains("后备")){
            pigType = PigType.RESERVE;
        }
        if(pigType == null){
            log.error("barn type is null, because source type name is {}", location.getTypeName());
            throw new JsonResponseException("barn.type.null");
        }

        DoctorBarn barn = new DoctorBarn();
        barn.setName(location.getBarn());
        barn.setOrgId(farm.getOrgId());
        barn.setOrgName(farm.getOrgName());
        barn.setFarmId(farm.getId());
        barn.setFarmName(farm.getName());
        barn.setPigType(pigType.getValue());
        barn.setCanOpenGroup("可以".equals(location.getCanOpenGroupText()) ? 1 : -1);
        barn.setStatus("在用".equals(location.getIsStopUseText()) ? 1 : 0);
        barn.setStaffId(subMap.get(location.getStaffName()));
        barn.setStaffName(location.getStaffName());
        barn.setOutId(location.getOID());
        return barn;
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
        basic.setIsValid(IsOrNot.YES.getValue());
        return basic;
    }

    //拼接猪舍 Map<barnOutId, DoctorBarn>
    public Map<String, DoctorBarn> getBarnMap(Long farmId) {
        return doctorBarnDao.findByFarmId(farmId).stream().collect(Collectors.toMap(DoctorBarn::getOutId, v -> v));
    }

    //拼接猪舍 Map<barnName, DoctorBarn>
    public Map<String, DoctorBarn> getBarnMap2(Long farmId) {
        return doctorBarnDao.findByFarmId(farmId).stream().collect(Collectors.toMap(DoctorBarn::getName, v -> v));
    }

    public Map<Long, Integer> getBarnIdMap(Long farmId) {
        return doctorBarnDao.findByFarmId(farmId).stream().collect(Collectors.toMap(DoctorBarn::getId, DoctorBarn::getPigType));
    }

    public Map<String, Long> getBarnNameMap(Long farmId) {
        return doctorBarnDao.findByFarmId(farmId).stream().collect(Collectors.toMap(DoctorBarn::getName, DoctorBarn::getId));
    }

    //分别是 Map<DoctorBasic.TypeEnum, Map<DoctorBasic.name, DoctorBasic>>
    public Map<Integer, Map<String, DoctorBasic>> getBasicMap() {
        Map<Integer, Map<String, DoctorBasic>> basicMap = Maps.newHashMap();
        doctorBasicDao.listAll().stream()
                .filter(basic -> !Objects.equals(basic.getIsValid(), -1))
                .collect(Collectors.groupingBy(DoctorBasic::getType)).entrySet()
                .forEach(basic -> basicMap.put(basic.getKey(),
                        basic.getValue().stream().collect(Collectors.toMap(DoctorBasic::getName, v -> v))));
        return basicMap;
    }

    public Map<String, Long> getBreedMap() {
        return doctorBasicDao.findByType(DoctorBasic.Type.BREED.getValue()).stream().collect(Collectors.toMap(DoctorBasic::getName, DoctorBasic::getId));
    }

    //拼接疫苗, Map<疫苗名称, 疫苗id>
    public Map<String, DoctorBasicMaterial> getVaccMap() {
        Map<String, DoctorBasicMaterial> vaccMap = Maps.newHashMap();
        doctorBasicMaterialDao.findByType(WareHouseType.VACCINATION.getKey()).forEach(vacc -> vaccMap.put(vacc.getName(), vacc));
        return vaccMap;
    }

    //拼接变动原因map
    public Map<String, DoctorChangeReason> getReasonMap() {
        Map<String, DoctorChangeReason> reasonMap = Maps.newHashMap();
        doctorChangeReasonDao.listAll().forEach(reason -> reasonMap.put(reason.getReason(), reason));
        return reasonMap;
    }

    //拼接客户map
    public Map<String, DoctorCustomer> getCustomerMap(Long farmId) {
        Map<String, DoctorCustomer> customerMap = Maps.newHashMap();
        doctorCustomerDao.findByFarmId(farmId).forEach(customer -> customerMap.put(customer.getName(), customer));
        return customerMap;
    }

    //拼接staff,  Map<真实姓名, userId>
    public Map<String, Long> getSubMap(Long orgId) {
        Map<String, Long> staffMap = Maps.newHashMap();
        doctorStaffDao.findByOrgId(orgId).forEach(staff -> {
            UserProfile profile = userProfileDao.findByUserId(staff.getUserId());
            if (profile != null && notEmpty(profile.getRealName())) {
                staffMap.put(profile.getRealName(), staff.getUserId());
            }
        });
        return staffMap;
    }

    //拼接变动原因
    private DoctorChangeReason getReason(B_ChangeReason reason, Long changeTypeId) {
        DoctorChangeReason changeReason = new DoctorChangeReason();
        changeReason.setChangeTypeId(changeTypeId);
        changeReason.setReason(reason.getReasonName());
        changeReason.setOutId(reason.getOID());
        return changeReason;
    }

    //拼接客户
    private DoctorCustomer getCustomer(DoctorFarm farm, B_Customer cus) {
        DoctorCustomer customer = new DoctorCustomer();
        customer.setName(cus.getCustomerName());
        customer.setFarmId(farm.getId());
        customer.setFarmName(farm.getName());
        customer.setMobile(cus.getMobilePhone());
        customer.setEmail(cus.getEMail());
        customer.setOutId(cus.getOID());
        return customer;
    }

    //判断猪场id是否相同
    private static boolean isFarm(String farmOID, String outId) {
        return Objects.equals(farmOID, outId);
    }

}
