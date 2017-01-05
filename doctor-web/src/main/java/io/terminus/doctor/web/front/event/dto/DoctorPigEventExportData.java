package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 16/12/28.
 * 猪事件导出封装
 */
@Data
public class DoctorPigEventExportData implements Serializable{
    private static final long serialVersionUID = 4277730430060898049L;

    private String pigCode;
    private String barnName;
    private Date eventAt;
    private String name;
    private String desc;
}
