package io.terminus.doctor.event.dto;

import com.google.common.collect.Maps;
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
            DoctorPigEvent event = eventTypeMap.get(PigEvent.MATING.getKey());
            doctorSowParityCount.setMatingDate(event.getMattingDate());
            doctorSowParityCount.setMatingStaff(event.getOperatorName());
        }

        if(eventTypeMap.containsKey(PigEvent.FARROWING.getKey())){
            DoctorPigEvent event = eventTypeMap.get(PigEvent.FARROWING.getKey());
            doctorSowParityCount.setFarrowingDate(event.getFarrowingDate());
            doctorSowParityCount.setPigLetCount(event.getLiveCount());
            if (event.getFarrowWeight() != null && event.getLiveCount() != null && event.getLiveCount() != 0) {
                doctorSowParityCount.setAvgBirthWeight(Double.valueOf(String.format("%.2f", event.getFarrowWeight() / event.getLiveCount())));
            } else {
                doctorSowParityCount.setAvgBirthWeight(0d);
            }
            doctorSowParityCount.setHealthCount(event.getHealthCount());
            doctorSowParityCount.setWeakCount(event.getWeakCount());
            doctorSowParityCount.setDeadCount(event.getDeadCount());
            doctorSowParityCount.setMujiCount((event.getMnyCount() == null ? 0 : event.getMnyCount()) + (event.getJxCount() == null ? 0 : event.getJxCount()));
        }

        if(eventTypeMap.containsKey(PigEvent.WEAN.getKey())){
            DoctorPigEvent event = eventTypeMap.get(PigEvent.WEAN.getKey());
            doctorSowParityCount.setWeanCount(event.getWeanCount());
            doctorSowParityCount.setWeanAvgWeight(event.getWeanAvgWeight());
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

}
