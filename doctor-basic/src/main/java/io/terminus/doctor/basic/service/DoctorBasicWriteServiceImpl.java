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
import io.terminus.doctor.basic.manager.DoctorBasicManager;
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

/**
 * Desc: 基础数据写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@Service
public class DoctorBasicWriteServiceImpl implements DoctorBasicWriteService {

    private final DoctorBreedDao doctorBreedDao;
    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorChangeTypeDao doctorChangeTypeDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorDiseaseDao doctorDiseaseDao;
    private final DoctorGeneticDao doctorGeneticDao;
    private final DoctorUnitDao doctorUnitDao;
    private final DoctorBasicManager doctorBasicManager;
    private final DoctorFosterReasonDao doctorFosterReasonDao;

    @Autowired
    public DoctorBasicWriteServiceImpl(DoctorBreedDao doctorBreedDao,
                                       DoctorChangeReasonDao doctorChangeReasonDao,
                                       DoctorChangeTypeDao doctorChangeTypeDao,
                                       DoctorCustomerDao doctorCustomerDao,
                                       DoctorDiseaseDao doctorDiseaseDao,
                                       DoctorGeneticDao doctorGeneticDao,
                                       DoctorUnitDao doctorUnitDao,
                                       DoctorBasicManager doctorBasicManager,
                                       DoctorFosterReasonDao doctorFosterReasonDao) {
        this.doctorBreedDao = doctorBreedDao;
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorChangeTypeDao = doctorChangeTypeDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorDiseaseDao = doctorDiseaseDao;
        this.doctorGeneticDao = doctorGeneticDao;
        this.doctorUnitDao = doctorUnitDao;
        this.doctorBasicManager = doctorBasicManager;
        this.doctorFosterReasonDao = doctorFosterReasonDao;
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
    public Response<Long> createBreed(DoctorBreed breed) {
        try {
            doctorBreedDao.create(breed);
            return Response.ok(breed.getId());
        } catch (Exception e) {
            log.error("create breed failed, breed:{}, cause:{}", breed, Throwables.getStackTraceAsString(e));
            return Response.fail("breed.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateBreed(DoctorBreed breed) {
        try {
            return Response.ok(doctorBreedDao.update(breed));
        } catch (Exception e) {
            log.error("update breed failed, breed:{}, cause:{}", breed, Throwables.getStackTraceAsString(e));
            return Response.fail("breed.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteBreedById(Long breedId) {
        try {
            return Response.ok(doctorBreedDao.delete(breedId));
        } catch (Exception e) {
            log.error("delete breed failed, breedId:{}, cause:{}", breedId, Throwables.getStackTraceAsString(e));
            return Response.fail("breed.delete.fail");
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

    @Override
    public Response<Long> createDisease(DoctorDisease disease) {
        try {
            doctorDiseaseDao.create(disease);
            return Response.ok(disease.getId());
        } catch (Exception e) {
            log.error("create disease failed, disease:{}, cause:{}", disease, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateDisease(DoctorDisease disease) {
        try {
            return Response.ok(doctorDiseaseDao.update(disease));
        } catch (Exception e) {
            log.error("update disease failed, disease:{}, cause:{}", disease, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteDiseaseById(Long diseaseId) {
        try {
            return Response.ok(doctorDiseaseDao.delete(diseaseId));
        } catch (Exception e) {
            log.error("delete disease failed, diseaseId:{}, cause:{}", diseaseId, Throwables.getStackTraceAsString(e));
            return Response.fail("disease.delete.fail");
        }
    }

    @Override
    public Response<Long> createGenetic(DoctorGenetic genetic) {
        try {
            doctorGeneticDao.create(genetic);
            return Response.ok(genetic.getId());
        } catch (Exception e) {
            log.error("create genetic failed, genetic:{}, cause:{}", genetic, Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateGenetic(DoctorGenetic genetic) {
        try {
            return Response.ok(doctorGeneticDao.update(genetic));
        } catch (Exception e) {
            log.error("update genetic failed, genetic:{}, cause:{}", genetic, Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteGeneticById(Long geneticId) {
        try {
            return Response.ok(doctorGeneticDao.delete(geneticId));
        } catch (Exception e) {
            log.error("delete genetic failed, geneticId:{}, cause:{}", geneticId, Throwables.getStackTraceAsString(e));
            return Response.fail("genetic.delete.fail");
        }
    }

    @Override
    public Response<Long> createUnit(DoctorUnit unit) {
        try {
            doctorUnitDao.create(unit);
            return Response.ok(unit.getId());
        } catch (Exception e) {
            log.error("create unit failed, unit:{}, cause:{}", unit, Throwables.getStackTraceAsString(e));
            return Response.fail("unit.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateUnit(DoctorUnit unit) {
        try {
            return Response.ok(doctorUnitDao.update(unit));
        } catch (Exception e) {
            log.error("update unit failed, unit:{}, cause:{}", unit, Throwables.getStackTraceAsString(e));
            return Response.fail("unit.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteUnitById(Long unitId) {
        try {
            return Response.ok(doctorUnitDao.delete(unitId));
        } catch (Exception e) {
            log.error("delete unit failed, unitId:{}, cause:{}", unitId, Throwables.getStackTraceAsString(e));
            return Response.fail("unit.delete.fail");
        }
    }

    @Override
    public Response<Long> createFosterReason(DoctorFosterReason fosterReason) {
        try {
            doctorFosterReasonDao.create(fosterReason);
            return Response.ok(fosterReason.getId());
        } catch (Exception e) {
            log.error("create fosterReason failed, fosterReason:{}, cause:{}", fosterReason, Throwables.getStackTraceAsString(e));
            return Response.fail("fosterReason.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateFosterReason(DoctorFosterReason fosterReason) {
        try {
            return Response.ok(doctorFosterReasonDao.update(fosterReason));
        } catch (Exception e) {
            log.error("update fosterReason failed, fosterReason:{}, cause:{}", fosterReason, Throwables.getStackTraceAsString(e));
            return Response.fail("fosterReason.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteFosterReasonById(Long fosterReasonId) {
        try {
            return Response.ok(doctorFosterReasonDao.delete(fosterReasonId));
        } catch (Exception e) {
            log.error("delete fosterReason failed, fosterReasonId:{}, cause:{}", fosterReasonId, Throwables.getStackTraceAsString(e));
            return Response.fail("fosterReason.delete.fail");
        }
    }
}
