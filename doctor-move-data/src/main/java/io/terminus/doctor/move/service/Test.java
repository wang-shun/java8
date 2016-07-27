package io.terminus.doctor.move.service;

import io.terminus.doctor.basic.dao.DoctorBasicDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Service
public class Test implements CommandLineRunner {

    @Autowired
    private DoctorBasicDao doctorBasicDao;

    @Override
    public void run(String... strings) throws Exception {
        System.out.println("***********************");
        System.out.println(doctorBasicDao.findById(1L));
        System.out.println("***********************");
    }
}
