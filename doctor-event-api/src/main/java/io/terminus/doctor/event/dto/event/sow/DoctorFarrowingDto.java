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
 * Descirbe: 母猪分娩事件Dto
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFarrowingDto implements Serializable{

    private static final long serialVersionUID = 7823378636552520021L;

    private Long pigId;

    private Date farrowingDate;

    private String nestCode; // 窝号

    private Long barnId;

    private Long barnName;

    private String bedCode;

    private Integer farrowingType;

    private Integer isHelp;

    private double birthNestAvg;

    private Integer liveSowCount;

    private Integer liveBoarCount;

    private Integer healthCount;    //健仔数量(断奶事件校验对应的母猪状态信息)

    private Integer weakCount;

    private Integer mnyCount;

    private Integer jxCount;

    private Integer deadCount;

    private Integer blackCount;

    private Long toBarnId;

    private String toBarnName;

    private Integer isSingleManager;

    private String staff1;

    private String staff2;

    private String remark;
}
