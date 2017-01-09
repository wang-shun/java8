package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.enums.RollbackType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by xjn on 17/1/9.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPublishEventDto implements Serializable{
    private static final long serialVersionUID = -7842336168457484957L;

    private List<RollbackType> rollbackTypes;  //需要分发事件的类型

    private Long orgId;         //公司id

    private Long farmId;        //猪场id

    private Date eventAt;       //回滚事件的日期

    private Integer eventType;

    private Long businessId;    //需要处理的对象id(猪id、猪群id)

    /**
     * @see io.terminus.doctor.event.dto.event.DoctorEventInfo.Business_Type
     */
    private Integer businessType;


}
