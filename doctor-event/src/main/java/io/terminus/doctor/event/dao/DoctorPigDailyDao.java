package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.springframework.stereotype.Repository;
import sun.font.SunFontManager;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-12 17:33:52
 * Created by [ your name ]
 */
@Repository
public class DoctorPigDailyDao extends MyBatisDao<DoctorPigDaily> {


    public DoctorPigDaily findByFarm(Long farmId) {
        List<DoctorPigDaily> reports = this.list(Collections.singletonMap("farmId", farmId));
        if (reports.isEmpty())
            return null;
        return reports.get(0);
    }


    public List<DoctorPigDaily> findByOrg(List<Long> farmIds) {
        return this.list(Collections.singletonMap("farmIds", farmIds));
    }


}
