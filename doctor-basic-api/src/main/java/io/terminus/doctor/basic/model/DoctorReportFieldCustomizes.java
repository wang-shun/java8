package io.terminus.doctor.basic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-03 15:17:40
 * Created by [ your name ]
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorReportFieldCustomizes implements Serializable {

    private static final long serialVersionUID = 4073199883949485307L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 猪场ID
     */
    private Long farmId;
    
    /**
     * 类型ID
     */
    private Long typeId;
    
    /**
     * 字段ID
     */
    private Long fieldId;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}