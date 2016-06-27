package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorChangeReasonDao;
import io.terminus.doctor.basic.dao.DoctorChangeTypeDao;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/23
 */
@Slf4j
@Component
public class DoctorBasicManager {

    private final DoctorChangeReasonDao doctorChangeReasonDao;
    private final DoctorChangeTypeDao doctorChangeTypeDao;

    /**
     * 模板猪场id
     */
    private static final long TEMPLATE_FARM_ID = 0L;

    @Autowired
    public DoctorBasicManager(DoctorChangeReasonDao doctorChangeReasonDao,
                              DoctorChangeTypeDao doctorChangeTypeDao) {
        this.doctorChangeReasonDao = doctorChangeReasonDao;
        this.doctorChangeTypeDao = doctorChangeTypeDao;
    }

    /**
     * 初始化基础数据
     * @param farmId  猪场id
     */
    @Transactional
    public void initFarmBasic(Long farmId) {
        //初始化猪群变动
        doctorChangeTypeDao.findByFarmId(TEMPLATE_FARM_ID).forEach(chgType -> {
            List<DoctorChangeReason> reasons = doctorChangeReasonDao.findByChangeTypeId(chgType.getId());
            
            chgType.setFarmId(farmId);
            doctorChangeTypeDao.create(chgType);
            
            //初始化变动原因
            reasons.forEach(reason -> {
                reason.setChangeTypeId(chgType.getId());
                doctorChangeReasonDao.create(reason);
            });
        });

        // TODO: 16/5/23 其他基础数据初始化
    }
}
