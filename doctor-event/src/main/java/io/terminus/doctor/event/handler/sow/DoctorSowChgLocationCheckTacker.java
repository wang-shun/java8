package io.terminus.doctor.event.handler.sow;

import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.workflow.core.TackerExecution;
import io.terminus.doctor.workflow.event.ITacker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by xiao on 16/8/19.
 */
@Component
@Slf4j
public class DoctorSowChgLocationCheckTacker implements ITacker {
    private DoctorPigTrackDao doctorPigTrackDao;
    @Override
    public Boolean tacker(TackerExecution tackerExecution) {
        if (tackerExecution != null){
            DoctorPigTrack doctorPigTrack = doctorPigTrackDao.findByPigId(tackerExecution.getBusinessId());
            if (doctorPigTrack != null){
                Integer currentBarnTyp = doctorPigTrack.getCurrentBarnType();
                if (currentBarnTyp != null){
                    if (currentBarnTyp == PigType.PREG_SOW.getValue()){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
