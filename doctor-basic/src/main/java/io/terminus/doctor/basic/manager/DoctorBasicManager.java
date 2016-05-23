package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorBreedDao;
import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorChangeTypeDao;
import io.terminus.doctor.basic.dao.DoctorCustomerDao;
import io.terminus.doctor.basic.dao.DoctorDiseaseDao;
import io.terminus.doctor.basic.dao.DoctorGeneticDao;
import io.terminus.doctor.basic.dao.DoctorUnitDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@Component
public class DoctorBasicManager {

    private final DoctorBreedDao doctorBreedDao;
    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorChangeTypeDao doctorChangeTypeDao;
    private final DoctorCustomerDao doctorCustomerDao;
    private final DoctorDiseaseDao doctorDiseaseDao;
    private final DoctorGeneticDao doctorGeneticDao;
    private final DoctorUnitDao doctorUnitDao;

    /**
     * 模板猪场id
     */
    private static final long TEMPLATE_FARM_ID = 0L;

    @Autowired
    public DoctorBasicManager(DoctorBreedDao doctorBreedDao,
                                      DoctorChangeReasonDao doctorChangeReasonDao,
                                      DoctorChangeTypeDao doctorChangeTypeDao,
                                      DoctorCustomerDao doctorCustomerDao,
                                      DoctorDiseaseDao doctorDiseaseDao,
                                      DoctorGeneticDao doctorGeneticDao,
                                      DoctorUnitDao doctorUnitDao) {
        this.doctorBreedDao = doctorBreedDao;
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorChangeTypeDao = doctorChangeTypeDao;
        this.doctorCustomerDao = doctorCustomerDao;
        this.doctorDiseaseDao = doctorDiseaseDao;
        this.doctorGeneticDao = doctorGeneticDao;
        this.doctorUnitDao = doctorUnitDao;
    }


    public void initFarmBasic(Long farmId) {

    }
}
