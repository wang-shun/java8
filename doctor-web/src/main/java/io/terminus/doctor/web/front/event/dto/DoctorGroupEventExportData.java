package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 16/12/28.
 * 猪群事件导出数据
 */
@Data
public class DoctorGroupEventExportData implements Serializable{
    private static final long serialVersionUID = 3030888793288062637L;

    private String groupCode;
    private Date eventAt;
    private String name;
    private String desc;
}
