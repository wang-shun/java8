package io.terminus.doctor.web.util;

import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.enums.FarrowingType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.enums.MatingType;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.enums.PigSource;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.enums.PregCheckResult;
import io.terminus.doctor.event.enums.VaccinResult;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.parana.user.model.UserProfile;
import io.terminus.parana.user.service.UserProfileReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.terminus.common.utils.Arguments.*;

/**
 * Created by highway on 16/8/11.
 */
@Component
public class TransFromUtil {
    private final DoctorBasicReadService doctorBasicReadService;
    private final UserProfileReadService userProfileReadService;
    private final DoctorBarnReadService doctorBarnReadService;
    @RpcConsumer
    private DoctorGroupReadService doctorGroupReadService;
    @RpcConsumer
    private DoctorPigEventReadService doctorPigEventReadService;
    @RpcConsumer
    private DoctorPigReadService doctorPigReadService;

    @Autowired
    public TransFromUtil(DoctorBasicReadService doctorBasicReadService, UserProfileReadService userProfileReadService, DoctorBarnReadService doctorBarnReadService) {
        this.doctorBasicReadService = doctorBasicReadService;
        this.userProfileReadService = userProfileReadService;
        this.doctorBarnReadService = doctorBarnReadService;
    }

    public void transFromExtraMap(List<DoctorPigEvent> doctorPigEvents) {
        for (DoctorPigEvent display : doctorPigEvents) {
            codeToName(display);

            if (Objects.equals(display.getType(), PigEvent.MATING.getKey())
                    || PigEvent.CHANGE_LOCATION.contains(display.getType())) {
                DoctorPigTrack doctorPigTrack = RespHelper.orServEx(doctorPigReadService.findPigTrackByPigId(display.getPigId()));
                display.setPigStatus(PigStatus.from(doctorPigTrack.getStatus()).getName());
            }

            if (Objects.equals(display.getType(), PigEvent.PREG_CHECK.getKey())) {
                display.setMatingDay(getMatingDay(display));
            }
            Boolean isRollback = false;
            Response<Boolean> booleanResponse = doctorPigEventReadService.eventCanRollback(display.getId());
            if (booleanResponse.isSuccess()) {
                isRollback = booleanResponse.getResult();
            }
            display.setIsRollback(isRollback);

            DoctorPig doctorPig = RespHelper.orServEx(doctorPigReadService.findPigById(display.getPigId()));
            Map<String, Object> extraMap = display.getExtraMap();
            if (isNull(extraMap)) {
                extraMap = Maps.newHashMap();
                display.setExtraMap(extraMap);
            }
            extraMap.put("rfid", doctorPig.getRfid());
        }
    }

    /**
     * 枚举值转换中文
     */
    private void codeToName(DoctorPigEvent display) {
        Map<String, Object> extraMap = display.getExtraMap();
        if (isNull(extraMap)) {
            return;
        }
        if (getInteger(extraMap, "matingType") != null) {
            extraMap.put("matingType", MatingType.from(toInteger(extraMap.get("matingType"))).getDesc());
        }
        if (getInteger(extraMap, "checkResult") != null) {
            extraMap.put("checkResult", PregCheckResult.from(toInteger(extraMap.get("checkResult"))).getDesc());
        }
        if (getInteger(extraMap, "farrowingType") != null) {
            extraMap.put("farrowType", FarrowingType.from(toInteger(extraMap.get("farrowingType"))).getDesc());
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
        if (Objects.equals(display.getType(), PigEvent.ENTRY.getKey())
                && notNull(display.getExtraMap())
                && display.getExtraMap().containsKey("boarType")) {
            extraMap.put("boarTypeName", BoarEntryType.from(Integer.valueOf(extraMap.get("boarType").toString())).getDesc());
        }

        if (Objects.equals(display.getType(), PigEvent.WEAN.getKey())
                && notNull(display.getExtraMap())
                && display.getExtraMap().containsKey("chgLocationToBarnId")) {
            DoctorBarn chgToBarn = RespHelper.or500(doctorBarnReadService.findBarnById(toLong(display.getExtraMap().get("chgLocationToBarnId"))));
            extraMap.put("chgLocationToBarnName", chgToBarn.getName());
        }
    }

    public List<DoctorGroupEvent> transFromGroupEvents(List<DoctorGroupEvent> doctorGroupEvents) {
        doctorGroupEvents.forEach(groupEvent -> {
            Map<String, Object> extraMap = groupEvent.getExtraData();
            if (extraMap != null) {
                if (getInteger(extraMap, "sex") != null) {
                    extraMap.put("sex", DoctorGroupTrack.Sex.from(toInteger(extraMap.get("sex"))).getDesc());
                }
                if (getInteger(extraMap, "source") != null) {
                    extraMap.put("source", PigSource.from(toInteger(extraMap.get("source"))).getDesc());
                }
                if (getInteger(extraMap, "vaccinResult") != null) {
                    extraMap.put("vaccinResult", (toInteger(extraMap.get("vaccinResult")) == 1) ? VaccinResult.POSITIVE : VaccinResult.NEGATIVE);
                }
            }

            Boolean isRollback = false;
            Response<Boolean> booleanResponse = doctorGroupReadService.eventCanRollback(groupEvent.getId());
            if (booleanResponse.isSuccess()) {
                isRollback = booleanResponse.getResult();
            }
            groupEvent.setIsRollback(isRollback);
        });
        return doctorGroupEvents;
    }

    /**
     * 获取已配种天数
     * @param pregCheckEvent 妊娠检查事件
     * @return 已配种天数
     */
    private Integer getMatingDay(DoctorPigEvent pregCheckEvent) {
        DoctorPigEvent firstMatingEvent = RespHelper.orServEx(doctorPigEventReadService
                .findFirstMatingBeforePregCheck(pregCheckEvent.getPigId(), pregCheckEvent.getParity(), pregCheckEvent.getId()));
        if (isNull(firstMatingEvent)) {
            return 0;
        }
        return DateUtil.getDeltaDays(firstMatingEvent.getEventAt(), pregCheckEvent.getEventAt());
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
