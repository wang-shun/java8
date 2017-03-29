package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.cache.DoctorBasicCacher;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.dao.DoctorFarmBasicDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.DoctorFarmBasic;
import io.terminus.doctor.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
@RpcProvider
public class DoctorBasicReadServiceImpl implements DoctorBasicReadService {

    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorBasicDao doctorBasicDao;
    private final DoctorBasicCacher doctorBasicCacher;
    private final DoctorFarmBasicDao doctorFarmBasicDao;

    @Autowired
    public DoctorBasicReadServiceImpl(DoctorChangeReasonDao doctorChangeReasonDao,
                                      DoctorCustomerDao doctorCustomerDao,
                                      DoctorBasicDao doctorBasicDao,
                                      DoctorBasicCacher doctorBasicCacher,
                                      DoctorFarmBasicDao doctorFarmBasicDao) {
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorBasicDao = doctorBasicDao;
        this.doctorBasicCacher = doctorBasicCacher;
        this.doctorFarmBasicDao = doctorFarmBasicDao;
    }

    @Override
    public Response<List<DoctorBasic>> findAllBasics() {
        try {
            return Response.ok(doctorBasicDao.listAll());
        } catch (Exception e) {
            log.error("find all basics failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("basic.find.fail");
        }
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
    public Response<List<DoctorBasic>> findBasicByIds(List<Long> basicIds) {
        try {
            return Response.ok(doctorBasicDao.findByIds(basicIds));
        } catch (Exception e) {
            log.error("find basic by ids failed, basicId:{}, cause:{}", basicIds, Throwables.getStackTraceAsString(e));
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
    public Response<List<DoctorBasic>> findValidBasicByTypeAndSrm(Integer type, String srm) {
        try {
            List<DoctorBasic> basics = doctorBasicDao.findValidByType(type);
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
    public Response<DoctorBasic> findBasicByIdFilterByFarmId(Long farmId, Long basicId) {
        try {
            if (!checkFarmBasicAuth(farmId, basicId)) {
                log.error("this basic not auth, farmId:{}, basicId:{}", farmId, basicId);
                return Response.fail("basic.not.auth");
            }
            return findBasicById(basicId);
        } catch (Exception e) {
            log.error("find basic by id failed, farmId:{}, basicId:{}, cause:{}", farmId, basicId, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBasic>> findBasicByTypeAndSrmFilterByFarmId(Long farmId, Integer type, String srm) {
        try {
            List<DoctorBasic> basics = RespHelper.orServEx(findValidBasicByTypeAndSrm(type, srm));
            return Response.ok(filterBasicByFarmAuth(farmId, basics));
        } catch (Exception e) {
            log.error("find basic by type and srm failed, farmId:{}, type:{}, srm:{}, cause:{}", farmId, type, srm, Throwables.getStackTraceAsString(e));
            return Response.fail("basic.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBasic>> findBasicByTypeAndSrmWithCacheFilterByFarmId(Long farmId, Integer type, String srm) {
        try {
            List<DoctorBasic> basics = RespHelper.orServEx(findBasicByTypeAndSrmWithCache(type, srm));
            return Response.ok(filterBasicByFarmAuth(farmId, basics));
        } catch (Exception e) {
            log.error("find basic by type and srm withcache failed, farmId:{}, type:{}, srm:{}, cause:{}", farmId, type, srm, Throwables.getStackTraceAsString(e));
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
    public Response<List<DoctorChangeReason>> findChangeReasonByIds(List<Long> changeReasonIds) {
        try {
            return Response.ok(doctorChangeReasonDao.findByIds(changeReasonIds));
        } catch (Exception e) {
            log.error("find changeReason by ids failed, changeReasonIds:{}, cause:{}", changeReasonIds, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.find.fail");
        }
    }

    @Override
    public Response<List<DoctorChangeReason>> findAllChangeReasons() {
        try {
            return Response.ok(doctorChangeReasonDao.listAll());
        } catch (Exception e) {
            log.error("find all changeReasons failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.find.fail");
        }
    }

    @Override
    public Response<List<DoctorChangeReason>> findChangeReasonByChangeTypeIdAndSrm(Long changeTypeId, String srm) {
        try {
            return Response.ok(doctorChangeReasonDao.findByChangeTypeIdAndSrm(changeTypeId, srm));
        } catch (Exception e) {
            log.error("find changeReason by farmId and changeTypeId failed, changeTypeId:{}, cause:{}",
                    changeTypeId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.find.fail");
        }
    }

    @Override
    public Response<DoctorChangeReason> findChangeReasonByIdFilterByFarmId(Long farmId, Long changeReasonId) {
        try {
            if (!checkFarmBasicReasonAuth(farmId, changeReasonId)) {
                log.error("this changeReason not auth, farmId:{}, changeReasonId:{}", farmId, changeReasonId);
                return Response.fail("changeReason.not.auth");
            }
            return Response.ok(doctorChangeReasonDao.findById(changeReasonId));
        } catch (Exception e) {
            log.error("find changeReason by id failed, farmId:{}, changeReasonId:{}, cause:{}", farmId, changeReasonId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.find.fail");
        }
    }

    @Override
    public Response<List<DoctorChangeReason>> findChangeReasonByChangeTypeIdAndSrmFilterByFarmId(Long farmId, Long changeTypeId, String srm) {
        try {
            List<DoctorChangeReason> reasons = doctorChangeReasonDao.findByChangeTypeIdAndSrm(changeTypeId, srm);
            return Response.ok(filterReasonByFarmAuth(farmId, reasons));
        } catch (Exception e) {
            log.error("find changeReason by farmId and changeTypeId failed, farmId:{}, changeTypeId:{}, cause:{}",
                    farmId, changeTypeId, Throwables.getStackTraceAsString(e));
            return Response.fail("changeReason.find.fail");
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

    @Override
    public Response<Paging<DoctorChangeReason>> pagingChangeReason(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
            return Response.ok(doctorChangeReasonDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("paging.change.reason.failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("paging.change.reason.failed");
        }
    }

    /**
     * 校验此猪场是否有查看基础数据权限
     * @param farmId    猪场id
     * @param basicId   基础数据id
     * @return true 有，false 没有
     */
    private boolean checkFarmBasicAuth(Long farmId, Long basicId) {
        DoctorFarmBasic farmBasic = doctorFarmBasicDao.findByFarmId(farmId);
        return canBasic(farmBasic, basicId);
    }

    private static boolean canBasic(DoctorFarmBasic farmBasic, Long basicId) {
        return !(farmBasic == null || !notEmpty(farmBasic.getBasicIdList()))
                && farmBasic.getBasicIdList().contains(basicId);
    }

    /**
     * 校验此猪场是否有查看变动原因权限
     * @param farmId    猪场id
     * @param reasonId  变动原因id
     * @return true 有，false 没有
     */
    private boolean checkFarmBasicReasonAuth(Long farmId, Long reasonId) {
        DoctorFarmBasic farmBasic = doctorFarmBasicDao.findByFarmId(farmId);
        return canReason(farmBasic, reasonId);
    }

    private static boolean canReason(DoctorFarmBasic farmBasic, Long reasonId) {
        return !(farmBasic == null || !notEmpty(farmBasic.getReasonIdList()))
                && farmBasic.getReasonIdList().contains(reasonId);
    }

    /**
     * 根据权限过滤一把基础数据
     * @param farmId    猪场id
     * @param basics    基础数据
     * @return 过滤后的结果
     */
    private List<DoctorBasic> filterBasicByFarmAuth(Long farmId, List<DoctorBasic> basics) {
        if (!notEmpty(basics)) {
            return Collections.emptyList();
        }
        DoctorFarmBasic farmBasic = doctorFarmBasicDao.findByFarmId(farmId);
        return basics.stream()
                .filter(basic -> canBasic(farmBasic, basic.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 根据权限过滤一把基础数据
     * @param farmId    猪场id
     * @param reasons   基础数据
     * @return 过滤后的结果
     */
    private List<DoctorChangeReason> filterReasonByFarmAuth(Long farmId, List<DoctorChangeReason> reasons) {
        if (!notEmpty(reasons)) {
            return Collections.emptyList();
        }
        DoctorFarmBasic farmBasic = doctorFarmBasicDao.findByFarmId(farmId);
        return reasons.stream()
                .filter(reason -> canReason(farmBasic, reason.getId()))
                .collect(Collectors.toList());
    }
}
