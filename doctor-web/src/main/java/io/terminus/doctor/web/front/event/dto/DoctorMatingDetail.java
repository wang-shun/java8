package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 配种预产时间跟配种次数
 * Created by highway on 16/8/2.
 */
@Data
public class DoctorMatingDetail implements Serializable{
    private static final long serialVersionUID = 1087087579421343064L;

    /**
     * 配种次数
     */
    private Integer matingCount;

    /**
     * 第一次配种时间
     */
    private Date firstMatingTime;

}
