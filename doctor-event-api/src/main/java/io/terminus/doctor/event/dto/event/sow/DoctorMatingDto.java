package io.terminus.doctor.event.dto.event.sow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪配种信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMatingDto implements Serializable {

    private static final long serialVersionUID = 2732269011148894160L;

    private Long pigId;

    private Date matingDate;

    private String pigCode;

    private Date judgePregDate; //预产日期

    private Integer matingType;

    private String staff;

    private String mark;
}
