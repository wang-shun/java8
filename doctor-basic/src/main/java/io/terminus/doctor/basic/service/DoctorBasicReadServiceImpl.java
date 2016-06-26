package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicDao;
import io.terminus.doctor.basic.dao.DoctorBreedDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorChangeTypeDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.dao.DoctorDiseaseDao;
import io.terminus.doctor.basic.dao.DoctorFosterReasonDao;
import io.terminus.doctor.basic.dao.DoctorGeneticDao;
import io.terminus.doctor.basic.dao.DoctorUnitDao;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBreed;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.basic.model.DoctorGenetic;
import io.terminus.doctor.basic.model.DoctorUnit;
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

    private final DoctorBreedDao doctorBreedDao;
    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorChangeTypeDao doctorChangeTypeDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorDiseaseDao doctorDiseaseDao;
    private final DoctorGeneticDao doctorGeneticDao;
    private final DoctorUnitDao doctorUnitDao;
    private final DoctorFosterReasonDao doctorFosterReasonDao;
    private final DoctorBasicDao doctorBasicDao;

    @Autowired
    public DoctorBasicReadServiceImpl(DoctorBreedDao doctorBreedDao,
                                      DoctorChangeReasonDao doctorChangeReasonDao,
                                      DoctorChangeTypeDao doctorChangeTypeDao,
                                      DoctorCustomerDao doctorCustomerDao,
                                      DoctorDiseaseDao doctorDiseaseDao,
                                      DoctorGeneticDao doctorGeneticDao,
                                      DoctorUnitDao doctorUnitDao,
                                      DoctorFosterReasonDao doctorFosterReasonDao,
                                      DoctorBasicDao doctorBasicDao) {
        this.doctorBreedDao = doctorBreedDao;
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorChangeTypeDao = doctorChangeTypeDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorDiseaseDao = doctorDiseaseDao;
        this.doctorGeneticDao = doctorGeneticDao;
        this.doctorUnitDao = doctorUnitDao;
        this.doctorFosterReasonDao = doctorFosterReasonDao;
        this.doctorBasicDao = doctorBasicDao;
    }

    @Override
    public Response<DoctorBreed> findBreedById(Long breedId) {
        try {
            return Response.ok(doctorBreedDao.findById(breedId));
        } catch (Exception e) {
            log.error("find breed by id failed, breedId:{}, cause:{}", breedId, Throwables.getStackTraceAsString(e));
            return Response.fail("breed.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBreed>> findAllBreeds() {
        try {
            return Response.ok(doctorBreedDao.findAll());
        } catch (Exception e) {
            log.error("find all breeds failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("breed.find.fail");
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

    @Override
    public Response<DoctorDisease> findDiseaseById(Long diseaseId) {
        try {
            return Response.ok(doctorDiseaseDao.findById(diseaseId));
        } catch (Exception e) {
            log.error("find disease by id failed, diseaseId:{}, cause:{}", diseaseId, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.find.fail");
        }
    }

    @Override
    public Response<List<DoctorDisease>> findDiseasesByFarmId(Long farmId) {
        try {
            return Response.ok(doctorDiseaseDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find disease by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.find.fail");
        }
    }

    @Override
    public Response<List<DoctorDisease>> findDiseasesByFarmIdAndSrm(Long farmId, String srm) {
        try {
            List<DoctorDisease> diseases = doctorDiseaseDao.findByFarmId(farmId);
            if (isEmpty(srm)) {
                return Response.ok(diseases);
            }
            return Response.ok(diseases.stream()
                    .filter(disease -> notEmpty(disease.getSrm()) && disease.getSrm().toLowerCase().contains(srm.toLowerCase()))
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("find disease by farm id and srm fail, farmId:{}, srm:{}, cause:{}", farmId, srm, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.find.fail");
        }
    }

    @Override
    public Response<DoctorGenetic> findGeneticById(Long geneticId) {
        try {
            return Response.ok(doctorGeneticDao.findById(geneticId));
        } catch (Exception e) {
            log.error("find genetic by id failed, geneticId:{}, cause:{}", geneticId, Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.find.fail");
        }
    }

    @Override
    public Response<List<DoctorGenetic>> findAllGenetics() {
        try {
            return Response.ok(doctorGeneticDao.findAll());
        } catch (Exception e) {
            log.error("find all genetics failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.find.fail");
        }
    }

    @Override
    public Response<DoctorUnit> findUnitById(Long unitId) {
        try {
            return Response.ok(doctorUnitDao.findById(unitId));
        } catch (Exception e) {
            log.error("find unit by id failed, unitId:{}, cause:{}", unitId, Throwables.getStackTraceAsString(e));
            return Response.fail("unit.find.fail");
        }
    }

    @Override
    public Response<List<DoctorUnit>> findAllUnits() {
        try {
            return Response.ok(doctorUnitDao.findAll());
        } catch (Exception e) {
            log.error("find all units failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("unit.find.fail");
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
}
