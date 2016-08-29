package io.terminus.doctor.event.dto.event.group.input;

import com.google.common.base.Joiner;
import io.terminus.doctor.event.enums.IsOrNot;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Desc: 猪群时间录入信息基类(公用字段)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@Data
public abstract class BaseGroupInput implements Serializable {
    private static final long serialVersionUID = 3142495945186975856L;

    /**
     * 事件发生时间 yyyy-MM-dd
     */
    @NotEmpty(message = "date.not.null")
    private String eventAt;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否是自动生成的事件(用于区分是触发事件还是手工录入事件) 0 不是, 1 是
     * @see io.terminus.doctor.event.enums.IsOrNot
     */
    @NotNull(message = "isAuto.not.null")
    private Integer isAuto;

    @NotNull(message = "creatorId.not.null")
    private Long creatorId;

    private String creatorName;

    public final String generateEventDesc(){
        String desc = Joiner.on("#").withKeyValueSeparator("：").join(this.descMap());
        if (Objects.equals(isAuto, IsOrNot.YES.getValue())) {
            return "【系统自动】" + desc;
        }else if (Objects.equals(isAuto, IsOrNot.NO.getValue())) {
            return "【手工录入】" + desc;
        }else{
            return desc;
        }
    }

    public abstract Map<String, String> descMap();
}
