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
 * Date: 2017-12-27 17:11:01
 * Created by [ your name ]
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorReportFieldCustomizes implements Serializable {

    private static final long serialVersionUID = -4905638221651863615L;

    /**
     * 自增主键
     */
    private Long id;

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