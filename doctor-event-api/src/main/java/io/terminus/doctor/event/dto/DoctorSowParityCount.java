package io.terminus.doctor.event.dto;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by yaoqijun.
 * Date:2016-07-08
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪胎次信息总结
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSowParityCount implements Serializable{

    private static final long serialVersionUID = -8894596972819863250L;

    private Integer parity;     // 当前胎次信息

    private Date farrowingDate; // 母猪分娩时间

    private Date matingDate;    //配种时间

    private String matingStaff; //配种人员

    private Integer pigLetCount;    //产仔数量

    private Double avgBirthWeight;  //出生均重

    private Integer healthCount;    //健崽数量

    private Integer weakCount;  // 弱仔数量

    private Integer deadCount;  //死亡数量

    private Integer mujiCount;  //畸形数量

    private Integer weanCount;  // 断奶数量

    private Double weanAvgWeight; //断奶均重

    private String boarCode;

    private Long boarId;

    public static DoctorSowParityCount doctorSowParityCountConvert(Integer parity, List<DoctorPigEvent> doctorPigEvents){

        Map<Integer, DoctorPigEvent> eventTypeMap = Maps.newHashMap();
        doctorPigEvents.forEach(s -> {
            if (!eventTypeMap.containsKey(s.getType()) || eventTypeMap.get(s.getType()).getId() < s.getId()){
                eventTypeMap.put(s.getType(), s);
            }
        });

        DoctorSowParityCount doctorSowParityCount = new DoctorSowParityCount();
        doctorSowParityCount.setParity(parity);

        if (eventTypeMap.containsKey(PigEvent.MATING.getKey())){
            Map<String,Object> extra = eventTypeMap.get(PigEvent.MATING.getKey()).getExtraMap();
            doctorSowParityCount.setMatingDate(new Date(Long.valueOf(extra.get("matingDate").toString())));
            doctorSowParityCount.setMatingStaff(extra.get("matingStaff").toString());
        }

        if(eventTypeMap.containsKey(PigEvent.FARROWING.getKey())){
            Map<String,Object> extra = eventTypeMap.get(PigEvent.FARROWING.getKey()).getExtraMap();
            doctorSowParityCount.setFarrowingDate(new Date(Long.valueOf(extra.get("farrowingDate").toString())));
            doctorSowParityCount.setPigLetCount(
                    DoctorSowParityCount.getCountFromMap(extra, "farrowingLiveCount"));
            doctorSowParityCount.setAvgBirthWeight(Double.valueOf(MoreObjects.firstNonNull(extra.get("birthNestAvg"), "0").toString()));
            doctorSowParityCount.setHealthCount(DoctorSowParityCount.getCountFromMap(extra, "healthCount"));
            doctorSowParityCount.setWeakCount(DoctorSowParityCount.getCountFromMap(extra, "weakCount"));
            doctorSowParityCount.setDeadCount(DoctorSowParityCount.getCountFromMap(extra, "deadCount"));
            doctorSowParityCount.setMujiCount(DoctorSowParityCount.getCountFromMap(extra,"mnyCount") +
                    DoctorSowParityCount.getCountFromMap(extra, "jxCount"));
        }

        if(eventTypeMap.containsKey(PigEvent.WEAN.getKey())){
            Map<String,Object> extra = eventTypeMap.get(PigEvent.WEAN.getKey()).getExtraMap();
            doctorSowParityCount.setWeanCount(getCountFromMap(extra, "partWeanPigletsCount"));
            doctorSowParityCount.setWeanAvgWeight(Double.valueOf(MoreObjects.firstNonNull(extra.get("partWeanAvgWeight"), "0").toString()));
        }

        Optional<DoctorPigEvent> optional = doctorPigEvents.stream()
                .filter(doctorPigEvent -> Objects.equals(doctorPigEvent.getType(), PigEvent.MATING.getKey()))
                .max(Comparator.comparing(DoctorPigEvent::getEventAt));
        if (optional.isPresent()){
            DoctorPigEvent matingEvent = optional.get();
            doctorSowParityCount.setBoarCode(matingEvent.getBoarCode());
        }
        return doctorSowParityCount;
    }

    private static Integer getCountFromMap(Map<String,Object> map, String key){
        if(! map.containsKey(key)){
            return 0;
        }
        return Params.getWithConvert(map, key, o->Integer.valueOf(o.toString()));
    }
}
