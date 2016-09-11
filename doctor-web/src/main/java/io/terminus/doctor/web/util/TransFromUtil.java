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
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserProfileReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static io.terminus.common.utils.Arguments.notEmpty;

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
                if (getInteger(extraMap, "matingType") != null) {
                    extraMap.put("matingType", MatingType.from(toInteger(extraMap.get("matingType"))).getDesc());
                }
                if (getInteger(extraMap, "checkResult") != null) {
                    extraMap.put("checkResult", PregCheckResult.from(toInteger(extraMap.get("checkResult"))).getDesc());
                }
                if (getInteger(extraMap, "farrowingType") != null) {
                    extraMap.put("farrowingType", FarrowingType.from(toInteger(extraMap.get("farrowingType"))).getDesc());
                }
                if (getInteger(extraMap, "farrowIsSingleManager") != null) {
                    extraMap.put("farrowIsSingleManager", (toInteger(extraMap.get("farrowIsSingleManager")) == 1) ? IsOrNot.YES.getDesc() : IsOrNot.NO.getDesc());
                }
                if (getLong(extraMap, "fosterReason") != null) {
                    extraMap.put("fosterReason", RespHelper.or500(doctorBasicReadService.findBasicById(toLong(extraMap.get("fosterReason")))).getName());
                }
                if (getLong(extraMap, "vaccinationStaffId") != null) {
                    UserProfile userProfile = RespHelper.or500(userProfileReadService.findProfileByUserId(toLong(extraMap.get("vaccinationStaffId"))));
                    if (userProfile != null && notEmpty(userProfile.getRealName())) {
                        extraMap.put("vaccinationStaffName", userProfile.getRealName());
                    }
                }
                if (getLong(extraMap, "toBarnId") != null) {
                    extraMap.put("toBarnId", RespHelper.or500(doctorBarnReadService.findBarnById(toLong(extraMap.get("toBarnId")))).getName());
                }
            }
        }
    }

    public void transFromGroupEvents(List<DoctorGroupEvent> doctorGroupEvents) {
        for (DoctorGroupEvent doctorGroupEvent : doctorGroupEvents) {
            Map<String,Object> extraMap = doctorGroupEvent.getExtraData();
            if (extraMap != null) {
                if (getInteger(extraMap, "sex") != null) {
                    extraMap.put("sex", DoctorGroupTrack.Sex.from(toInteger(extraMap.get("sex"))).getDesc());
                }
                if (getInteger(extraMap, "source") != null) {
                    extraMap.put("source", PigSource.from(toInteger(extraMap.get("source"))).getDesc());
                }
                if (getInteger(extraMap, "vaccinResult") != null) {
                    extraMap.put("vaccinResult", (toInteger(extraMap.get("vaccinResult")) == 1) ? DoctorAntiepidemicGroupEvent.VaccinResult.POSITIVE : DoctorAntiepidemicGroupEvent.VaccinResult.NEGATIVE);
                }
            }
        }
    }

    private static Integer toInteger(Object o) {
        return Integer.valueOf(String.valueOf(o));
    }

    private static Long toLong(Object o) {
        return Long.valueOf(String.valueOf(o));
    }

    private static Integer getInteger(Map<String, Object> params, String key) {
        Object o = params.get(key);
        if (o == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }

    private static Long getLong(Map<String, Object> params, String key) {
        Object o = params.get(key);
        if (o == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(o));
        } catch (Exception e) {
            return null;
        }
    }
}
