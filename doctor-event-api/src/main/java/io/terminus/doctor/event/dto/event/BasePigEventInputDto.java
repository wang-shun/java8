package io.terminus.doctor.event.dto.event;


import io.terminus.doctor.common.util.JsonMapperUtil;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by chenzenghui on 16/8/30.
 * 所有与猪相关的事件, 用户录入的数据 dto 的抽象父类
 */
@Data
public abstract class BasePigEventInputDto implements Serializable{
    private static final long serialVersionUID = 8608840545820959951L;

    /**
     * 事件操作人, 不一定是录入者
     */
    private Long operatorId;
    /**
     * 事件操作人, 不一定是录入者
     */
    private String operatorName;

    // basic exist pig info（进厂事件信息，不用录入）
    private Long pigId;

    private Long relGroupEventId; //由哪个猪群事件触发

    private Long relPigEventId; //由哪个猪事件触发

    private Integer isAuto;

    /**
     * 对应的母猪类型（公猪，母猪）
     * @see DoctorPig.PigSex
     */
    private Integer pigType;

    private String pigCode;

    private Long barnId;

    private String barnName;

    /**
     * @see io.terminus.doctor.event.enums.PigEvent
     */
    private Integer eventType;

    private String eventName;

    private String eventDesc;

    /**
     * 对事件的描述, 由一系列键值对组成, 只需要子类实现返回 Map 即可
     * @return
     */

    public abstract Map<String, String> descMap();

    /**
     * 事件发生的时间
     * @return
     */
    public abstract Date eventAt();

    public Map<String, Object> toMap(){
        return JsonMapperUtil.nonEmptyMapper().getMapper()
                .convertValue(this, Map.class);
    }
}
