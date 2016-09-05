package io.terminus.doctor.event.dao;

import io.terminus.common.utils.Splitters;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xiao on 16/9/2.
 */
public class DoctorEventDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Test
    public void findByIds(){
        List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.findByIds(Splitters.COMMA.splitToList(doctorPigTrackDao.findById(191).getRelEventIds()).stream().filter(id -> StringUtils.isNotBlank(id)).map(id -> Long.parseLong(id)).collect(Collectors.toList()));
    }
}
