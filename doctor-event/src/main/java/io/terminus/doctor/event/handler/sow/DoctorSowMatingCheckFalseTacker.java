package io.terminus.doctor.event.handler.sow;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.TackerExecution;
import io.terminus.doctor.workflow.event.ITacker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by xiao on 16/8/19.
 */
@Component
public class DoctorSowMatingCheckFalseTacker implements ITacker{
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;

    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Override
    public Boolean tacker(TackerExecution tackerExecution) {
        if (tackerExecution != null){
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(tackerExecution.getBusinessId());
            if (doctorPigTrack != null){
                Long barnId = doctorPigTrack.getCurrentBarnId();
                DoctorBarn barn = doctorBarnDao.findById(barnId);
                if (barn != null && barn.getPigType() != null){
                    // 猪类型是妊娠舍
                    if (barn.getPigType() == PigType.PREG_SOW.getValue() || barn.getPigType() == PigType.MATE_SOW.getValue()){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
