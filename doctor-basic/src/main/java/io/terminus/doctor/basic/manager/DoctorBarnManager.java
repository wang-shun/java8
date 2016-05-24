package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorBarnDao;
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
public class DoctorBarnManager {

    private final DoctorBarnDao doctorBarnDao;

    @Autowired
    public DoctorBarnManager(DoctorBarnDao doctorBarnDao) {
        this.doctorBarnDao = doctorBarnDao;
    }

}
