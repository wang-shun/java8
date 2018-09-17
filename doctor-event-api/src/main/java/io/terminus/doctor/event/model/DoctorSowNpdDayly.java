package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @program: doctor
 * @description: ${description}
 * @author: YuSQ
 * @create: 2018-09-04 15:54
 **/
@Data
public class DoctorSowNpdDayly implements Serializable {
    private  Long id;

    private Long orgId;//公司ID:

    private Long farmId;//猪场ID:

    private Integer fiscalYear;//会计年度:

    private Integer fiscalPeriod;//会计期间:

    private Integer fiscalQuarter;//季度:

    private Long barnId;//:

    private Long eventId;//:

    private Long pigId;//母猪ID:

    private Integer lastEventType;//前一事件的标识，[1] - 进场，[2] - 配种，[3] - 妊检阳性，[4] - 妊检阴性，[5] - 妊检流产，[6] - 妊检返情，[7] - 分娩，[8] - 离场，[9] - 断奶，[10] - 转场，[11] - 转场转入:

    private String lastEventName;//前一事件名称:

    private Date lastEventDate;//前一事件日期:

    private Integer currentEventType;//当前事件的标识，[1] - 进场，[2] - 配种，[3] - 妊检阳性，[4] - 妊检阴性，[5] - 妊检流产，[6] - 妊检返情，[7] - 分娩，[8] - 离场，[9] - 断奶，[10] - 转场，[11] - 转场转入:

    private String currentEventName;//当前事件名称:

    private Date currentEventDate;//当前事件日期:

    private Integer gestationDate;//孕期天数:

    private Integer lactationDate;//哺乳期天数:

    private Integer npdDate;//总非生产天数:

    private Integer jcNpd;//进场非生产天数

    private Integer dnpzNpd;//断奶配种非生产天数

    private Integer lcNpd;//流产非生产天数

    private Integer fqNpd;//返情非生产天数

    private Integer swNpd;//死亡非生产天数

    private Integer ttNpd;//淘汰非生产天数

    private Integer parity;//胎次:

    private Long creatorId;//创建人ID

    private String creatorName;//创建人姓名

    private Long updatorId;//修改人ID

    private String updatorName;//修改人姓名

    private Date createdAt;//创建时间

    private Date updatedAt;//最近修改时间

}
