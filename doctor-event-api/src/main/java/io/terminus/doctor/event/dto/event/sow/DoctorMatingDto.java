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

    private Date matingDate; // 配种日期

    private Long matingBoarPigId;   //配种公猪Id

    private String matingBoarPigCode; //配种公猪号

    private Date judgePregDate; //预产日期

    /**
     * @see io.terminus.doctor.event.enums.MatingType
     */
    private Integer matingType; // 配种类型

    private String matingStaff; // 配种人员

    private String mattingMark; // 配种mark
}
