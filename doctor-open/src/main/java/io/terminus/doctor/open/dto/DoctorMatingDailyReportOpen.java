package io.terminus.doctor.open.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 16/12/23.
 * 配种日报对外封装
 */
@Data
public class DoctorMatingDailyReportOpen implements Serializable{
    private static final long serialVersionUID = 6366454451028075673L;

    /**
     * 后备
     */
    private int houbei;

    /**
     * 断奶
     */
    private int duannai;

    /**
     * 返情
     */
    private int fanqing;

    /**
     * 流产
     */
    private int liuchan;
}
