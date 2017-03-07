package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-06-16
 * Email:yaoqj@terminus.io
 * Descirbe: 公猪详情界面信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBoarDetailDto implements Serializable{

    private static final long serialVersionUID = -4732947162931613533L;

    private String pigBoarCode;  // 母猪Code

    private String breedName; // 品种名称

    private String barnCode; // barn Code

    private Integer pigStatus; // 猪状态

    private Date entryDate; // 进厂日期

    private Date birthDate; //出生日期

    private Integer dayAge; //日龄

    private Double weight; //重量

    private Integer boarType; //公猪类型

    private Long canRollback;

    private List<DoctorPigEvent> doctorPigEvents;

}
