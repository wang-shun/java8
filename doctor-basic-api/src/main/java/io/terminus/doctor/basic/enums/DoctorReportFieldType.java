package io.terminus.doctor.basic.enums;

import lombok.Getter;

/**
 * Created by sunbo@terminus.io on 2017/12/27.
 */
public enum DoctorReportFieldType {

    TYPE(1), FIELD(2);

    @Getter
    private int value;

    DoctorReportFieldType(int value) {
        this.value = value;
    }



}
