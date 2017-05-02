package io.terminus.doctor.move.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terminus on 2017/4/28.
 */
@Data
public class DoctorGroupEventTime implements Serializable{
    private static final long serialVersionUID = -6834541572125446187L;
    /**
     * 创建日期
     */
    private Date openAt;
    /**
     * 关闭日期
     */
    private Date closeAt;
}
