package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorBarn;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by chenzenghui on 16/10/20.
 */
public class DoctorBarnSelect extends DoctorBarn{

    /**
     * 是否令前台默认勾选此猪舍
     */
    @Getter
    @Setter
    private boolean select = true;

}
