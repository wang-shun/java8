package io.terminus.doctor.event.dto;

import com.google.common.base.MoreObjects;
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

    private int parity;     // 当前胎次信息

    private Date farrowingDate; // 母猪分娩时间

    private Date matingDate;    //配种时间

    private String matingStaff; //配种人员

    private int pigLetCount;    //产仔数量

    private double avgBirthWeight;  //出生均重

    private int healthCount;    //健崽数量

    private int weakCount;  // 弱仔数量

    private int deadCount;  //死亡数量

    private int mujiCount;  //畸形数量

    private int weanCount;  // 断奶数量

    private double weanAvgWeight; //断奶均重

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
            doctorSowParityCount.setPigLetCount(MoreObjects.firstNonNull(event.getLiveCount(), 0));
            if (event.getFarrowWeight() != null && event.getLiveCount() != null && event.getLiveCount() != 0) {
                doctorSowParityCount.setAvgBirthWeight(Double.valueOf(String.format("%.2f", event.getFarrowWeight() / event.getLiveCount())));
            } else {
                doctorSowParityCount.setAvgBirthWeight(0d);
            }
            doctorSowParityCount.setHealthCount(MoreObjects.firstNonNull(event.getHealthCount(), 0));
            doctorSowParityCount.setWeakCount(MoreObjects.firstNonNull(event.getWeakCount(), 0));
            doctorSowParityCount.setDeadCount(MoreObjects.firstNonNull(event.getDeadCount(), 0));
            doctorSowParityCount.setMujiCount(MoreObjects.firstNonNull(event.getMnyCount(), 0) + MoreObjects.firstNonNull(event.getJxCount(), 0));
        }

        if(eventTypeMap.containsKey(PigEvent.WEAN.getKey())){
            DoctorPigEvent event = eventTypeMap.get(PigEvent.WEAN.getKey());
            doctorSowParityCount.setWeanCount(MoreObjects.firstNonNull(event.getWeanCount(), 0));
            doctorSowParityCount.setWeanAvgWeight(MoreObjects.firstNonNull(event.getWeanAvgWeight(), 0D));
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
