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
 * Date: 2017-12-27 16:19:39
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorReportFields implements Serializable {

    private static final long serialVersionUID = -797046689461636941L;

    /**
     * 自增主键
     */
    private Long id;
    
    /**
     * 父ID
     */
    private Long fId;
    
    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段英文名称，对应九张报表中字段名
     */
    private String reportField;

    /**
     * 数据格式处理类名
     */
    private String dataFormatter;
    
    /**
     * 类型
     * @see io.terminus.doctor.basic.enums.DoctorReportFieldType
     */
    private Integer type;
    
    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

}