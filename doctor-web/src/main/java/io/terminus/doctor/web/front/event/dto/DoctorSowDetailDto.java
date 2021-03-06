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
 * Descirbe: 公猪详情信息解释
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSowDetailDto implements Serializable {

    private static final long serialVersionUID = 2679500831809795880L;

    private String pigSowCode;  // 母猪Code

    private String warnMessage; // 事件信息提醒方式

    private String breedName; // 品种名称

    private Long barnId; //猪舍ID

    private String barnCode; // barn Code

    private Integer pigStatus; // 猪状态

    private Integer pregCheckResult; // 母猪空怀详细状态

    private Integer dayAge; // 日龄

    private Integer parity; //胎次

    private Date entryDate; //进厂日期

    private Date removalDate;   //离场日期

    private Date birthDate; //出生日期

    private Long canRollback;

    private List<DoctorPigEvent> doctorPigEvents;

    /**
     * 猪rfid
     */
    private String rfid;

    /**
     * 状态天数,母猪出入当前状态的天数
     */
    private Integer statusDay;

    /**
     * 母猪体重
     */
    private Double pigWeight;
}
