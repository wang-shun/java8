package io.terminus.doctor.web.util;

import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.parana.user.service.UserProfileReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Created by highway on 16/8/11.
 */
@Component
public class TransFromUtil {
    private final DoctorBasicReadService doctorBasicReadService;
    private final UserProfileReadService userProfileReadService;
    private final DoctorBarnReadService doctorBarnReadService;

    @Autowired
    public TransFromUtil(DoctorBasicReadService doctorBasicReadService, UserProfileReadService userProfileReadService, DoctorBarnReadService doctorBarnReadService) {
        this.doctorBasicReadService = doctorBasicReadService;
        this.userProfileReadService = userProfileReadService;
        this.doctorBarnReadService = doctorBarnReadService;
    }

    public void transFromExtraMap(List<DoctorPigEvent> doctorPigEvents) {
        for (DoctorPigEvent doctorPigEvent : doctorPigEvents) {
            Map<String,Object> extraMap = doctorPigEvent.getExtraMap();
            if (extraMap != null) {
                if (extraMap.get("matingType") != null) {
                    extraMap.put("matingType", MatingType.from((Integer) extraMap.get("matingType")).getDesc());
                }
                if (extraMap.get("checkResult") != null) {
                    extraMap.put("checkResult", PregCheckResult.from((Integer) extraMap.get("checkResult")).getDesc());
                }
                if (extraMap.get("farrowingType") != null) {
                    extraMap.put("farrowingType", FarrowingType.from((Integer) extraMap.get("farrowingType")).getDesc());
                }
                if (extraMap.get("farrowIsSingleManager") != null) {
                    extraMap.put("farrowIsSingleManager", ((Integer)extraMap.get("farrowIsSingleManager") == 1) ? IsOrNot.YES.getDesc() : IsOrNot.NO.getDesc());
                }
                if (extraMap.get("fosterReason") != null) {
                    extraMap.put("fosterReason", RespHelper.or500(doctorBasicReadService.findBasicById(Long.valueOf((Integer)extraMap.get("fosterReason")))).getName());
                }
                if (extraMap.get("vaccinationStaffId") != null) {
                    extraMap.put("vaccinationStaffName", RespHelper.or500(userProfileReadService.findProfileByUserId(Long.valueOf((Integer)extraMap.get("vaccinationStaffId")))).getRealName());
                }
                if (extraMap.get("toBarnId") != null) {
                    extraMap.put("toBarnId", RespHelper.or500(doctorBarnReadService.findBarnById(Long.valueOf((Integer) extraMap.get("toBarnId")))).getName());
                }
            }
        }
    }

    public void transFromGroupEvents(List<DoctorGroupEvent> doctorGroupEvents) {
        for (DoctorGroupEvent doctorGroupEvent : doctorGroupEvents) {
            Map<String,Object> extraMap = doctorGroupEvent.getExtraData();
            if (extraMap != null) {
                if (extraMap.get("sex") != null) {
                    extraMap.put("sex", DoctorGroupTrack.Sex.from((Integer) extraMap.get("sex")).getDesc());
                }
                if (extraMap.get("source") != null) {
                    extraMap.put("source", PigSource.from((Integer) extraMap.get("source")).getDesc());
                }
                if (extraMap.get("vaccinResult") != null) {
                    extraMap.put("vaccinResult", ((Integer) extraMap.get("vaccinResult") == 1) ? DoctorAntiepidemicGroupEvent.VaccinResult.POSITIVE : DoctorAntiepidemicGroupEvent.VaccinResult.NEGATIVE);
                }
            }
        }
    }
}
