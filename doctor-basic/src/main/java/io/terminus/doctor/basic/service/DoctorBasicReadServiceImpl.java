package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBreedDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorChangeTypeDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.dao.DoctorDiseaseDao;
import io.terminus.doctor.basic.dao.DoctorFosterReasonDao;
import io.terminus.doctor.basic.dao.DoctorGeneticDao;
import io.terminus.doctor.basic.dao.DoctorUnitDao;
import io.terminus.doctor.basic.model.DoctorBreed;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorChangeType;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.basic.model.DoctorDisease;
import io.terminus.doctor.basic.model.DoctorFosterReason;
import io.terminus.doctor.basic.model.DoctorGenetic;
import io.terminus.doctor.basic.model.DoctorUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Autowired
    public DoctorBasicReadServiceImpl(DoctorBreedDao doctorBreedDao,
                                      DoctorChangeReasonDao doctorChangeReasonDao,
                                      DoctorChangeTypeDao doctorChangeTypeDao,
                                      DoctorCustomerDao doctorCustomerDao,
                                      DoctorDiseaseDao doctorDiseaseDao,
                                      DoctorGeneticDao doctorGeneticDao,
                                      DoctorUnitDao doctorUnitDao,
                                      DoctorFosterReasonDao doctorFosterReasonDao) {
        this.doctorBreedDao = doctorBreedDao;
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorChangeTypeDao = doctorChangeTypeDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorDiseaseDao = doctorDiseaseDao;
        this.doctorGeneticDao = doctorGeneticDao;
        this.doctorUnitDao = doctorUnitDao;
        this.doctorFosterReasonDao = doctorFosterReasonDao;
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
    public Response<DoctorFosterReason> findFosterReasonById(Long fosterReasonId) {
        try {
            return Response.ok(doctorFosterReasonDao.findById(fosterReasonId));
        } catch (Exception e) {
            log.error("find fosterReason by id failed, fosterReasonId:{}, cause:{}", fosterReasonId, Throwables.getStackTraceAsString(e));
            return Response.fail("fosterReason.find.fail");
        }
    }

    @Override
    public Response<List<DoctorFosterReason>> findFosterReasonsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorFosterReasonDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find fosterReason by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("fosterReason.find.fail");
        }
    }
}
