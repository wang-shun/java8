package io.terminus.doctor.event.dto.event;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by chenzenghui on 16/8/30.
 * 所有与猪相关的事件, 用户录入的数据 dto 的抽象父类
 */
@Data
public abstract class AbstractPigEventInputDto implements Serializable{
    private static final long serialVersionUID = 8608840545820959951L;

    /**
     * 事件操作人, 不一定是录入者
     */
    private Long operatorId;
    /**
     * 事件操作人, 不一定是录入者
     */
    private String operatorName;

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
}
