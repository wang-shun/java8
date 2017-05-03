package io.terminus.doctor.move.controller;

import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 16:57 2017/3/30
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/flush/data")
public class DoctorFlushDataController {

    private final DoctorGroupTrackDao doctorGroupTrackDao;


    @Autowired
    public DoctorFlushDataController(DoctorGroupTrackDao doctorGroupTrackDao){
        this.doctorGroupTrackDao = doctorGroupTrackDao;
    }
}
