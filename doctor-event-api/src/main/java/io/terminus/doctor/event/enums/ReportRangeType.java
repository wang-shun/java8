package io.terminus.doctor.event.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 22:51 2017/4/19
 */

public enum ReportRangeType {
    MONTH(1, "月"),
    WEEK(2, "周");

    @Getter
    private Integer value;

    @Getter
    private String desc;

    ReportRangeType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public static ReportRangeType from(Integer value){
        for(ReportRangeType reportRangeType:ReportRangeType.values()){
            if(Objects.equals(reportRangeType.getValue(), value)){
                return reportRangeType;
            }
        }
        return null;
    }
}
