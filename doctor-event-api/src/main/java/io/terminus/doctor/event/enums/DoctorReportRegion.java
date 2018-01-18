package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by xjn on 18/1/18.
 * email:xiaojiannan@terminus.io
 */
public enum DoctorReportRegion {
    RESERVE(1, "后备区"),
    SOW(2, "母猪区"),
    MATING(3, "配怀区"),
    DELIVER(4, "产房区"),
    NURSERY(5, "保育区"),
    FATTEN(6, "育肥区"),
    BOAR(7, "公猪区"),
    MATERIAL(8, "物料消耗"),
    EFFICIENCY(9, "效率指标");

    @Getter
    private Integer value;
    @Getter
    private String name;

    DoctorReportRegion(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    public static DoctorReportRegion from(String name){
        for(DoctorReportRegion reportRegion : DoctorReportRegion.values()){
            if(Objects.equals(reportRegion.getName(), name)){
                return reportRegion;
            }
        }
        return null;
    }
}
