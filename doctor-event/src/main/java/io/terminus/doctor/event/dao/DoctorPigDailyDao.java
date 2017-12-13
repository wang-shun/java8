package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.model.DoctorPigDaily;
import org.springframework.stereotype.Repository;
import sun.font.SunFontManager;

import java.util.*;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-12 17:33:52
 * Created by [ your name ]
 */
@Repository
public class DoctorPigDailyDao extends MyBatisDao<DoctorPigDaily> {


    public DoctorPigDaily findByFarm(Long farmId, Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmId", farmId);
        params.put("start", start);
        params.put("end", end);

        List<DoctorPigDaily> reports = this.list(params);
        if (reports.isEmpty())
            return null;
        return reports.get(0);
    }


    public List<DoctorPigDaily> findByOrg(List<Long> farmIds, Date start, Date end) {
        Map<String, Object> params = new HashMap<>();
        params.put("farmIds", farmIds);
        params.put("start", start);
        params.put("end", end);
        
        return this.list(params);
    }


}
